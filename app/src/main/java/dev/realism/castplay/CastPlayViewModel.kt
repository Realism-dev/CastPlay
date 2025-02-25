package dev.realism.castplay

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Основная viewmodel приложения, подключает слушатели, подписывается на обновления статуса и запускает Cast по нажатию кнопки на главном экране*/
class CastPlayViewModel(
    private val castUseCase: CastUseCase
) : ViewModel() {
    val status: StateFlow<String> = castUseCase.status
    val toastMessage: StateFlow<String?> = castUseCase.toastMessage

    fun startCasting() {
        viewModelScope.launch {
            try{
                castUseCase.startCasting()
            }
            catch (e:Exception){
                Log.d(TAG, e.toString())
            }
        }
    }

    fun clearToastMessage() {
        castUseCase.clearToastMessage()
    }

    companion object {
        private const val TAG= "CAST STARTING"
    }
}
