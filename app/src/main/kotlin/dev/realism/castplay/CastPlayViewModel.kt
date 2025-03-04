package dev.realism.castplay

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

/** Основная viewmodel приложения, подписывается на обновления статуса и транслирует сообщения для тоста*/
class CastPlayViewModel(
    private val castUseCase: CastUseCase
) : ViewModel(),CastPlayViewModelInterface {
    override val status: StateFlow<String> = castUseCase.status
    override val toastMessage: StateFlow<String?> = castUseCase.toastMessage

    override fun clearToastMessage() {
        try {
            castUseCase.clearToastMessage()
            Log.d(TAG,"Toast message cleared")
        }
        catch (e:Exception){
            Log.d(TAG,e.toString())
        }
    }

    companion object {
        private const val TAG= "CAST VIEWMODEL"
    }
}
