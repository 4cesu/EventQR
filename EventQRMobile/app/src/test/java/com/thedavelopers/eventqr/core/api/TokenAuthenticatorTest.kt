package com.thedavelopers.eventqr.core.api

import com.thedavelopers.eventqr.core.session.TokenStore
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TokenAuthenticatorTest {

    private class FakeStore(var access: String?, var refresh: String?) : TokenStore {
        override fun getAccessToken() = access
        override fun getRefreshToken() = refresh
        override fun saveTokens(accessToken: String, refreshToken: String?) {
            access = accessToken
            if (refreshToken != null) refresh = refreshToken
        }
        override fun clear() {
            access = null
            refresh = null
        }
    }

    private lateinit var server: MockWebServer
    private var refreshCalls = 0
    private var expiredNotified = 0
    private var outcome: RefreshOutcome = RefreshOutcome.Success("new-access", "new-refresh")

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        refreshCalls = 0
        expiredNotified = 0
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun clientFor(store: FakeStore): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val token = store.access
            val builder = chain.request().newBuilder()
            if (!token.isNullOrBlank()) builder.addHeader("Authorization", "Bearer $token")
            chain.proceed(builder.build())
        }
        .authenticator(
            TokenAuthenticator(
                store,
                { refreshCalls++; outcome },
                { expiredNotified++ },
            )
        )
        .build()

    private fun get(client: OkHttpClient, path: String = "/api/v1/users/me") =
        client.newCall(Request.Builder().url(server.url(path)).build()).execute()

    @Test
    fun expiredAccessTokenIsRefreshedAndTheRequestRetriedTransparently() {
        val store = FakeStore("old-access", "old-refresh")
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))

        val response = get(clientFor(store))

        assertEquals(200, response.code)
        assertEquals(1, refreshCalls)
        assertEquals("new-access", store.access)
        assertEquals("new-refresh", store.refresh)
        server.takeRequest()
        assertEquals("Bearer new-access", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun aRequestThatLostTheRaceReusesTheTokenAnotherRequestAlreadyRefreshed() {
        val store = FakeStore("old-access", "old-refresh")
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(200))
        // The store changes after this request was sent but before its 401 is handled.
        val client = clientFor(store).newBuilder()
            .addNetworkInterceptor { chain ->
                val result = chain.proceed(chain.request())
                if (result.code == 401) store.access = "already-refreshed"
                result
            }
            .build()

        val response = get(client)

        assertEquals(200, response.code)
        assertEquals("a single refresh must not be repeated", 0, refreshCalls)
        server.takeRequest()
        assertEquals("Bearer already-refreshed", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun aRejectedRefreshTokenEndsTheSessionAndTellsTheApp() {
        val store = FakeStore("old-access", "old-refresh")
        outcome = RefreshOutcome.Rejected
        server.enqueue(MockResponse().setResponseCode(401))

        val response = get(clientFor(store))

        assertEquals(401, response.code)
        assertNull(store.access)
        assertNull(store.refresh)
        assertEquals(1, expiredNotified)
    }

    @Test
    fun beingOfflineDuringRefreshNeverLogsTheUserOut() {
        val store = FakeStore("old-access", "old-refresh")
        outcome = RefreshOutcome.Unavailable
        server.enqueue(MockResponse().setResponseCode(401))

        val response = get(clientFor(store))

        assertEquals(401, response.code)
        assertEquals("old-access", store.access)
        assertEquals("old-refresh", store.refresh)
        assertEquals(0, expiredNotified)
    }

    @Test
    fun aWrongPasswordOnLoginDoesNotTriggerARefresh() {
        val store = FakeStore("stale-access", "stale-refresh")
        server.enqueue(MockResponse().setResponseCode(401))

        val response = get(clientFor(store), "/api/v1/auth/login")

        assertEquals(401, response.code)
        assertEquals(0, refreshCalls)
        assertEquals("stale-access", store.access)
    }

    @Test
    fun anOldSessionWithoutARefreshTokenJustFails() {
        val store = FakeStore("old-access", null)
        server.enqueue(MockResponse().setResponseCode(401))

        val response = get(clientFor(store))

        assertEquals(401, response.code)
        assertEquals(0, refreshCalls)
        assertEquals(0, expiredNotified)
    }

    @Test
    fun aRetryThatIsAlsoRejectedStopsInsteadOfLooping() {
        val store = FakeStore("old-access", "old-refresh")
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(401))

        val response = get(clientFor(store))

        assertEquals(401, response.code)
        assertEquals(1, refreshCalls)
        assertTrue(server.requestCount <= 2)
    }
}
