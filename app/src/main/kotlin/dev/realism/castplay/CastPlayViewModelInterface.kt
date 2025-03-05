package dev.realism.castplay

import kotlinx.coroutines.flow.StateFlow

interface CastPlayViewModelInterface {
    val status: StateFlow<String>
    val toastMessage: StateFlow<String?>
    fun clearToastMessage()
}