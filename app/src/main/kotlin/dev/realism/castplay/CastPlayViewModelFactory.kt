package dev.realism.castplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**Фабрика для создания viewmodel на перспективу*/
class CastPlayViewModelFactory(
    private val castUseCase: CastUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CastPlayViewModel::class.java)) {
            return CastPlayViewModel(castUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
