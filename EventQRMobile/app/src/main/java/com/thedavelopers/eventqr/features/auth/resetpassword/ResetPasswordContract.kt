package com.thedavelopers.eventqr.features.auth.resetpassword

interface ResetPasswordContract {
    interface View {
        fun showLoading(isLoading: Boolean)
        fun showTokenInvalid()
        fun showForm()
        fun showPasswordError(message: String?)
        fun showConfirmPasswordError(message: String?)
        fun showMessage(message: String)
        fun showSuccess()
        fun navigateToLogin()
    }
}
