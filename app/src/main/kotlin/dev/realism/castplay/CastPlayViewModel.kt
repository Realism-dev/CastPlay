package dev.realism.castplay

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

/** Основная viewmodel приложения, подписывается на обновления статуса и транслирует сообщения для тоста*/
class CastPlayViewModel(
    private val castUseCase: CastUseCase
) : ViewModel() {
    val status: StateFlow<String> = castUseCase.status
    val toastMessage: StateFlow<String?> = castUseCase.toastMessage

    fun clearToastMessage() {
        castUseCase.clearToastMessage()
        Log.d(TAG,"Toast message cleared")
    }

    companion object {
        private const val TAG= "CAST VIEWMODEL"
    }
}
