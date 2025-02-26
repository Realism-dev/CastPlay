package dev.realism.castplay

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastStateListener
import com.google.android.gms.cast.framework.SessionManager
import dev.realism.castplay.ui.theme.CastPlayTheme

/**Главная Activity приложения, инициализирует компоненты MediaRouter, CastContext, ViewModel
 * и отображает CastPlayScreen*/
class MainActivity : AppCompatActivity(), CastContextInterface {
    private lateinit var mediaRouter: MediaRouter
    private lateinit var castContext: CastContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        castContext = CastContext.getSharedInstance(this)
        mediaRouter = MediaRouter.getInstance(this)
        val castUseCase = CastUseCase(this,  lifecycleScope)

        // Создаем ViewModel с использованием кастомной фабрики
        val factory = CastPlayViewModelFactory(castUseCase)
        val viewModel = ViewModelProvider(this, factory)[CastPlayViewModel::class.java]

        setContent {
            CastPlayTheme {
                Surface (modifier = Modifier
                    .fillMaxSize()) {
                    CastPlayScreen(viewModel)
                }
            }
        }
    }

    override val sessionManager: SessionManager
        get() = castContext.sessionManager

    override fun addCastStateListener(listener: CastStateListener) {
        castContext.addCastStateListener(listener)
    }
}