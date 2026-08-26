package com.thedavelopers.eventqr.features.auth.resetpassword

import android.content.Context
import com.thedavelopers.eventqr.core.api.NetworkResult
import com.thedavelopers.eventqr.core.util.Validators
import com.thedavelopers.eventqr.features.auth.AuthRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ResetPasswordPresenter() {
    private var view: ResetPasswordContract.View? = null
    private var job: kotlinx.coroutines.Job? = null
    private var repository: AuthRepository? = null
    private var token: String? = null

    fun attach(view: ResetPasswordContract.View, context: Context) {
        this.view = view
        this.repository = AuthRepository(context)
    }

    fun detach() {
        job?.cancel()
        view = null
        repository = null
    }

    fun validateToken(token: String?) {
        if (token.isNullOrBlank()) {
            view?.showTokenInvalid()
            return
        }
        this.token = token
        view?.showLoading(true)
        job = MainScope().launch {
            when (val result = repository?.validateResetToken(token)) {
                is NetworkResult.Success -> {
                    view?.showLoading(false)
                    val data = result.data
                    if (data != null && data.valid) {
                        view?.showForm()
                    } else {
                        view?.showTokenInvalid()
                    }
                }
                is NetworkResult.Error -> {
                    view?.showLoading(false)
                    view?.showTokenInvalid()
                }
                NetworkResult.Loading -> Unit
                null -> {
                    view?.showLoading(false)
                    view?.showTokenInvalid()
                }
            }
        }
    }

    fun submitReset(newPassword: String, confirmPassword: String) {
        val currentToken = token
        if (currentToken.isNullOrBlank()) {
            view?.showMessage("Reset token is missing")
            return
        }

        view?.showPasswordError(null)
        view?.showConfirmPasswordError(null)

        val requirements = Validators.passwordRequirements(newPassword)
        if (!requirements.isValid) {
            view?.showPasswordError("Password must be at least 8 characters and include an uppercase letter, a number, and a special character")
            return
        }

        if (newPassword != confirmPassword) {
            view?.showConfirmPasswordError("Passwords do not match")
            return
        }

        view?.showLoading(true)
        job = MainScope().launch {
            when (val result = repository?.resetPassword(currentToken, newPassword, confirmPassword)) {
                is NetworkResult.Success -> {
                    view?.showLoading(false)
                    view?.showSuccess()
                }
                is NetworkResult.Error -> {
                    view?.showLoading(false)
                    view?.showMessage(result.message ?: "Reset failed")
                }
                NetworkResult.Loading -> Unit
                null -> {
                    view?.showLoading(false)
                    view?.showMessage("Reset failed")
                }
            }
        }
    }

    fun navigateToLogin() {
        view?.navigateToLogin()
    }
}
