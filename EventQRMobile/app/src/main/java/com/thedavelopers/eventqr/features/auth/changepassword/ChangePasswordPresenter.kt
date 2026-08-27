package com.thedavelopers.eventqr.features.auth.changepassword

import android.content.Context
import com.thedavelopers.eventqr.core.api.NetworkResult
import com.thedavelopers.eventqr.core.util.Validators
import com.thedavelopers.eventqr.features.auth.AuthRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ChangePasswordPresenter() {
    private var view: ChangePasswordContract.View? = null
    private var job: kotlinx.coroutines.Job? = null
    private var repository: AuthRepository? = null

    fun attach(view: ChangePasswordContract.View, context: Context) {
        this.view = view
        this.repository = AuthRepository(context)
    }

    fun detach() {
        job?.cancel()
        view = null
        repository = null
    }

    fun submitChange(currentPassword: String, newPassword: String, confirmPassword: String) {
        view?.showCurrentPasswordError(null)
        view?.showNewPasswordError(null)
        view?.showConfirmPasswordError(null)

        if (currentPassword.isBlank()) {
            view?.showCurrentPasswordError("Enter your current password")
            return
        }

        val requirements = Validators.passwordRequirements(newPassword)
        if (!requirements.isValid) {
            view?.showNewPasswordError("Password must be at least 8 characters and include an uppercase letter, a number, and a special character")
            return
        }

        if (newPassword != confirmPassword) {
            view?.showConfirmPasswordError("Passwords do not match")
            return
        }

        view?.showLoading(true)
        job = MainScope().launch {
            when (val result = repository?.changePassword(currentPassword, newPassword, confirmPassword)) {
                is NetworkResult.Success -> {
                    view?.showLoading(false)
                    view?.showSuccess()
                }
                is NetworkResult.Error -> {
                    view?.showLoading(false)
                    view?.showMessage(result.message ?: "Change password failed")
                }
                NetworkResult.Loading -> Unit
                null -> {
                    view?.showLoading(false)
                    view?.showMessage("Change password failed")
                }
            }
        }
    }

    fun navigateBack() {
        view?.navigateBack()
    }
}
