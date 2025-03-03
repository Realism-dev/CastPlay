package dev.realism.castplay

import android.util.Log
import com.google.android.gms.cast.CastStatusCodes
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastSession
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
    private val lifeCycleScope: CoroutineScope
) {
    private val _status = MutableStateFlow(STATUS_DEFAULT)
    val status: StateFlow<String> = _status.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var isConnecting = false

    init {
        setCastSessionListener()
    }

    /**Метод отправи ссылки на видео на устройство-приемник Google Cast.*/
    private fun sendLinkToDevice(castContext: CastContextInterface): Boolean {
        Log.d(TAG_SEND, "Send started")
        val castSession = castContext.sessionManager.currentCastSession
        // Проверяем, есть ли активная сессия и устройство подключено
        if (castSession != null && castSession.isConnected) {
            val mName = castSession.castDevice?.modelName
            val fName = castSession.castDevice?.friendlyName
            Log.d(TAG_SEND, "$mName , $fName")

            val mediaMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
            mediaMetadata.putString(MediaMetadata.KEY_TITLE, "Горячее видео")

            val mediaInfo = MediaInfo.Builder(VIDEO_LINK)
                .setMetadata(mediaMetadata)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("video/mp4")
                .build()

            val mediaLoadOptions = MediaLoadOptions.Builder()
                .setAutoplay(true)  // Включить автоматическое воспроизведение
                .setPlayPosition(0) // Устанавливаем позицию воспроизведения с 0
                .build()

            val remoteMediaClient = castSession.remoteMediaClient
            if (remoteMediaClient != null) {
                try {
                    // Загружаем медиа на устройство
                    remoteMediaClient.load(mediaInfo, mediaLoadOptions)
                    Log.d(TAG_SEND, "Видео успешно отправлено")
                    Log.d(TAG_SEND, "Send ended:Success")
                    return true // Видео успешно отправлено
                } catch (e: Exception) {
                    Log.d(TAG_SEND, "Ошибка при отправке видео на устройство: ${e.message}")
                    Log.d(TAG_SEND, "Send ended:Failed")
                    return false // Ошибка при отправке
                }
            }
        } else {
            // Если сессия не активна или устройство не подключено
            Log.d(TAG_SEND, "Сессия не активна или устройство не подключено.")
        }
        Log.d(TAG_SEND, "Send ended:Failed")
        return false
    }

    /**Установка слушателя CastSession*/
    private fun setCastSessionListener() {
        val sessionListener = object : SessionManagerListener<CastSession> {
            override fun onSessionEnded(p0: CastSession, p1: Int) {
                isConnecting = false
                setStatusMessage(STATUS_DEFAULT)
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
                setStatusMessage(STATUS_CONNECTING_FAILED)
                setToastMessage(STATUS_CONNECTING_FAILED)
                setDefaultStatusWithDelay()
                val error = CastStatusCodes.getStatusCodeString(p1)
                Log.d(TAG_CAST_SESSION, "onSessionStartFailed: error:$error")
            }

            override fun onSessionStarted(p0: CastSession, p1: String) {
                isConnecting = false
                val deviceName = p0.castDevice?.friendlyName

                val isSent = sendLinkToDevice(castContext)
                if (isSent) {
                    setStatusMessage(STATUS_SENDED)
                    setToastMessage("Видео успешно отправлено на ${deviceName}!")
                } else {
                    setStatusMessage(STATUS_SEND_FAILED)
                    setToastMessage("Не удалось отправить видео на ${deviceName}!")
                }
                Log.d(TAG_CAST_SESSION, "onSessionStarted")
            }

            override fun onSessionStarting(p0: CastSession) {
                setConnectingStatusWithDelay(p0)
                Log.d(TAG_CAST_SESSION, "onSessionStarting")
            }

            override fun onSessionSuspended(p0: CastSession, p1: Int) {
                isConnecting = false
                Log.d(TAG_CAST_SESSION, "onSessionSuspended")
            }

        }
        castContext.addSessionManagerListener(sessionListener, CastSession::class.java)
        Log.d(TAG_CAST_SESSION, "addedCastSessionManagerListener")
    }

    /**Метод эмиттит сообщение в статусе с задержкой*/
    private fun setDefaultStatusWithDelay() {
        lifeCycleScope.launch {
            delay(2000)
            setStatusMessage(STATUS_DEFAULT)
        }
    }

    /**Метод эмиттит сообщение о подключении к устройству в статусе с задержкой*/
    private fun setConnectingStatusWithDelay(castSession: CastSession) {
        isConnecting = true
        val device = castSession.castDevice
        val sendMessage = "Подключение к ${device!!.friendlyName}"
        lifeCycleScope.launch {
            while (isConnecting) {
                setStatusMessage(sendMessage)
                delay(300)
                setStatusMessage(sendMessage.plus("."))
                delay(300)
                setStatusMessage(sendMessage.plus(".."))
                delay(300)
                setStatusMessage(sendMessage.plus("..."))
                delay(300)
            }
            setStatusMessage("Подключено к ${device.friendlyName}")
        }
    }

    /**Метод эмиттит сообщение в статусе*/
    private fun setStatusMessage(status: String) {
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
        private const val STATUS_DEFAULT = "Ожидание нажатия кнопки"
        private const val STATUS_CONNECTING_FAILED = "Ошибка при подключении"
        private const val STATUS_SEND_FAILED = "Ошибка передачи!"
        private const val STATUS_SENDED = "Видео отправлено!"
        private const val VIDEO_LINK =
            "https://videolink-test.mycdn.me/?pct=1&sig=6QNOvp0y3BE&ct=0&clientType=45&mid=193241622673&type=5"
    }
}