package dev.realism.castplay

import android.util.Log
import com.google.android.gms.cast.CastStatusCodes
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.cast.framework.CastStateListener
import com.google.android.gms.cast.framework.SessionManagerListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**Класс для управления подключением и передачи ссылки на видео устройству-приемнику Google Cast */
class CastUseCase(
    private val castContext: CastContextInterface,
    private val viewModelScope: CoroutineScope
) {
    private val _status = MutableStateFlow(STATUS_DEFAULT)
    val status: StateFlow<String> = _status.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var isConnecting = false

    init {
        setCastStateListener()
        setCastSessionListener()
    }

    /**Метод отправи ссылки на видео на устройство-приемник Google Cast.*/
    private fun sendLinkToDevice(): Boolean {
        Log.e(TAG_SEND, "Send started")

        val castSession = castContext.sessionManager.currentCastSession
        val mName = castSession?.castDevice?.modelName
        val fName = castSession?.castDevice?.friendlyName
        Log.d(TAG_SEND, "$mName , $fName")

        // Проверяем, есть ли активная сессия и устройство подключено
        if (castSession != null && castSession.isConnected) {
            val mediaMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
            mediaMetadata.putString(MediaMetadata.KEY_TITLE, "Видео по ссылке")

            val mediaInfo = MediaInfo.Builder(VIDEO_LINK)
                .setMetadata(mediaMetadata)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("video/mp4")
                .build()

            // Создаем объект MediaLoadOptions
            val mediaLoadOptions = MediaLoadOptions.Builder()
                .setAutoplay(true)  // Включить автоматическое воспроизведение
                .setPlayPosition(0) // Устанавливаем позицию воспроизведения с 0
                .build()

            val remoteMediaClient = castSession.remoteMediaClient
            remoteMediaClient?.let {
                try {
                    // Загружаем медиа на устройство
                    it.load(mediaInfo, mediaLoadOptions)
                    Log.e(TAG_SEND, "Видео успешно отправлено")
                    Log.e(TAG_SEND, "Send ended:Success")
                    return true // Видео успешно отправлено
                } catch (e: Exception) {
                    Log.e(TAG_SEND, "Ошибка при отправке видео на устройство: ${e.message}")
                    Log.e(TAG_SEND, "Send ended:Failed")
                    return false // Ошибка при отправке
                }
            }
        } else {
            // Если сессия не активна или устройство не подключено
            Log.e(TAG_SEND, "Сессия не активна или устройство не подключено.")
        }
        Log.e(TAG_SEND, "Send ended:Failed")
        return false
    }

    /**Установка слушателя состояний передачи данных CastState*/
    private fun setCastStateListener() {
        val csListener = CastStateListener { castState ->
            if (castState == CastState.CONNECTED) {
                Log.d(TAG_STATE,"STATE CONNECTED")
            }
        }
        castContext.addCastStateListener(csListener)
    }

    /**Установка слушателя CastSession*/
    private fun setCastSessionListener() {
        val listener = object :SessionManagerListener<CastSession>{
            override fun onSessionEnded(p0: CastSession, p1: Int) {
                isConnecting = false
                setStatus(STATUS_DEFAULT)
                Log.d(TAG_CAST_SESSION, "onSessionEnded")
            }

            override fun onSessionEnding(p0: CastSession) {
                isConnecting = false
                Log.d(TAG_CAST_SESSION, "onSessionEnding")
            }

            override fun onSessionResumeFailed(p0: CastSession, p1: Int) {
                isConnecting = false
                Log.d(TAG_CAST_SESSION, "onSessionResumeFailed")
            }

            override fun onSessionResumed(p0: CastSession, p1: Boolean) {
                isConnecting = false
                Log.d(TAG_CAST_SESSION, "onSessionResumed")
            }

            override fun onSessionResuming(p0: CastSession, p1: String) {
                isConnecting = false
                Log.d(TAG_CAST_SESSION, "onSessionResuming")
            }

            override fun onSessionStartFailed(p0: CastSession, p1: Int) {
                isConnecting = false
                setStatus(STATUS_CONNECTING_FAILED)
                setToastMessage("Произошла ошибка при подключении")
                setDefaultStatusWithDelay()
                val error = CastStatusCodes.getStatusCodeString(p1)
                Log.d(TAG_CAST_SESSION, "onSessionStartFailed: error:$error")
            }

            override fun onSessionStarted(p0: CastSession, p1: String) {
                isConnecting = false
                val device = p0.castDevice
                val isSent = sendLinkToDevice()
                if (isSent) {
                    setStatus(STATUS_SENDED)
                    setToastMessage("Видео успешно отправлено на ${device?.friendlyName}!")
                } else {
                    setStatus(STATUS_SEND_FAILED)
                    setToastMessage("Не удалось отправить видео на ${device?.friendlyName}!")
                }
                Log.d(TAG_CAST_SESSION, "onSessionStarted")
            }

            override fun onSessionStarting(p0: CastSession) {
                setConnectingStatusWithDelay(p0)
                Log.d(TAG_CAST_SESSION, "onSessionStarting")
            }

            override fun onSessionSuspended(p0: CastSession, p1: Int) {
                Log.d(TAG_CAST_SESSION, "onSessionSuspended")
            }

        }
        castContext.addSessionManagerListener(listener,CastSession::class.java)
        Log.d(TAG_CAST_SESSION, "addedCastSessionManagerListener")
    }

    /**Метод эмиттит сообщение в статусе с задержкой*/
    private fun setDefaultStatusWithDelay() {
        viewModelScope.launch {
            delay(2000)
            setStatus(STATUS_DEFAULT)
        }
    }

    /**Метод эмиттит сообщение о подключении к устройству в статусе с задержкой*/
    private fun setConnectingStatusWithDelay(castSession: CastSession) {
        isConnecting = true
        val device = castSession.castDevice
        val sendMessage = "Подключение к ${device!!.friendlyName}"
        viewModelScope.launch {
            while(isConnecting){
                setStatus(sendMessage)
                delay(300)
                setStatus(sendMessage.plus("."))
                delay(300)
                setStatus(sendMessage.plus(".."))
                delay(300)
                setStatus(sendMessage.plus("..."))
                delay(300)
            }
            setStatus("Подключено к ${device.friendlyName}")
        }
    }

    /**Метод эмиттит сообщение в статусе*/
    private fun setStatus(status: String) {
        _status.value = status
    }

    /**Метод эмиттит сообщение для тоста*/
    private fun setToastMessage(message: String) {
        _toastMessage.value = message
    }

    /**Метод обнуляет StateFlow для тоста, чтобы не показывать одно и то же при рекомпозиции CastPlayScreen*/
    fun clearToastMessage() {
        _toastMessage.value = null
    }

    companion object {
        private const val TAG_CAST_SESSION = "CAST CAST_SESSION"
        private const val TAG_SEND = "CAST SENDING"
        private const val TAG_STATE = "CAST STATE"
        private const val STATUS_DEFAULT = "Ожидание нажатия кнопки"
        private const val STATUS_CONNECTING_FAILED = "Ошибка при подключении"
        private const val STATUS_SEND_FAILED = "Ошибка передачи!"
        private const val STATUS_SENDED = "Видео отправлено!"
        const val VIDEO_LINK =
            "https://videolink-test.mycdn.me/?pct=1&sig=6QNOvp0y3BE&ct=0&clientType=45&mid=193241622673&type=5"
    }
}

