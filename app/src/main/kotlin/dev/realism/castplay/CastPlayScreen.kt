package dev.realism.castplay

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import dev.realism.castplay.ui.theme.purple
import dev.realism.castplay.ui.theme.white

/**Главный экран приложения, рисует простой фон и кнопку по центру, под кнопкой отображает статус подключения и выводит тосты.*/
@Composable
fun CastPlayScreen(viewModel: CastPlayViewModel) {
    val status by viewModel.status.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState(null)
    val context = LocalContext.current
    // Сохраняем MediaRouteButton
    val mediaRouteButton = remember {
        MediaRouteButton(context).apply {
            CastButtonFactory.setUpMediaRouteButton(context, this)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
            contentAlignment = Alignment.Center
        ){
            Button(
                onClick = { mediaRouteButton.performClick() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = purple, // Цвет фона кнопки
                    contentColor = white   // Цвет текста кнопки
                )
            ) {
                Text("Отправить ссылку")
            }

            // Используем AndroidView для скрытого отображения MediaRouteButton
            AndroidView(
                modifier = Modifier.alpha(0f),  // Скрываем кнопку
                factory = {
                    mediaRouteButton.apply {
                        CastButtonFactory.setUpMediaRouteButton(context, this)
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = status)
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()  // Очистка после показа
        }
    }
}

/**Preview главного экрана*/
@Preview(showBackground = true)
@Composable
fun CastPlayScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
            contentAlignment = Alignment.Center
        ){
            Button(
                onClick = {  },
                colors = ButtonDefaults.buttonColors(
                    containerColor = purple, // Цвет фона кнопки
                    contentColor = white   // Цвет текста кнопки
                )
            ) {
                Text("Отправить ссылку")
            }

        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Статус подключения")
    }
}




