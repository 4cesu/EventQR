package com.thedavelopers.eventqr.features.auth.forgotpassword

import android.content.Context
import com.thedavelopers.eventqr.core.api.NetworkResult
import com.thedavelopers.eventqr.core.util.Validators
import com.thedavelopers.eventqr.features.auth.AuthRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ForgotPasswordPresenter() {
    private var view: ForgotPasswordContract.View? = null
    private var job: kotlinx.coroutines.Job? = null
    private var repository: AuthRepository? = null

    fun attach(view: ForgotPasswordContract.View, context: Context) {
        this.view = view
        this.repository = AuthRepository(context)
    }

    fun detach() {
        job?.cancel()
        view = null
        repository = null
    }

    fun submitRequest(email: String) {
        val emailValue = email.trim()
        if (!Validators.isValidEmail(emailValue)) {
            view?.showEmailError("Enter a valid email address")
            return
        }

        view?.showEmailError(null)
        view?.showLoading(true)
        job = MainScope().launch {
            when (val result = repository?.forgotPassword(emailValue)) {
                is NetworkResult.Success -> {
                    view?.showLoading(false)
                    view?.showConfirmation()
                }
                is NetworkResult.Error -> {
                    view?.showLoading(false)
                    view?.showConfirmation()
                }
                NetworkResult.Loading -> Unit
                null -> {
                    view?.showLoading(false)
                    view?.showConfirmation()
                }
            }
        }
    }

    fun backToSignIn() {
        view?.navigateBackToSignIn()
    }
}
