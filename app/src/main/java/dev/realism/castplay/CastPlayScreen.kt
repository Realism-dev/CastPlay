package dev.realism.castplay

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.realism.castplay.ui.theme.black
import dev.realism.castplay.ui.theme.purple
import dev.realism.castplay.ui.theme.white

/**Главный экран приложения, рисует простой фон и кнопку по центру, под кнопкой отображает статус подключения и выводит тосты.*/
@Composable
fun CastPlayScreen(viewModel: CastPlayViewModel) {
    val status by viewModel.status.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState(null)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { viewModel.startCasting() },
            colors = ButtonDefaults.buttonColors(
                    containerColor = purple, // Цвет фона кнопки
                    contentColor = white   // Цвет текста кнопки
           )
        ) {
            Text("Отправить ссылку")
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
            .background(black)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {  },
            colors = ButtonDefaults.buttonColors(
                containerColor = purple, // Цвет фона кнопки
                contentColor = Color.White   // Цвет текста кнопки
            )

        ) {
            Text("Отправить ссылку")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Статус отправки")
    }
}




