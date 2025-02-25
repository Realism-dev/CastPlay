package dev.realism.castplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.framework.CastContext
import dev.realism.castplay.ui.theme.CastPlayTheme

/**Главная Activity приложения, инициализирует компоненты MediaRouter, CastContext, ViewModel
 * и отображает CastPlayScreen*/
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val castContext = CastContext.getSharedInstance(this)
        val mediaRouter = MediaRouter.getInstance(this)
        val castUseCase = CastUseCase(mediaRouter,castContext,lifecycleScope)

        // Создаем ViewModel с использованием кастомной фабрики
        val factory = CastPlayViewModelFactory(castUseCase)
        val viewModel = ViewModelProvider(this, factory)[CastPlayViewModel::class.java]

        setContent {
            CastPlayTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CastPlayScreen(viewModel)
                }
            }
        }
    }
}

