package com.thedavelopers.eventqr.core.session

/**
 * Lets low-level networking code announce that the session can no longer be recovered (the
 * refresh token was rejected) without depending on any Activity. The Application registers
 * the handler and sends the user back to the login screen.
 */
object SessionEvents {
    @Volatile
    var onSessionExpired: (() -> Unit)? = null

    fun notifySessionExpired() {
        onSessionExpired?.invoke()
    }
}
