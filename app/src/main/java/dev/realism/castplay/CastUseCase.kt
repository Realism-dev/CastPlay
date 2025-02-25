package dev.realism.castplay

import android.content.Intent
import android.util.Log
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.CastStatusCodes
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.cast.framework.CastStateListener
import com.google.android.gms.cast.framework.Session
import com.google.android.gms.cast.framework.SessionManagerListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**Класс для управления подключением и передачи ссылки на видео устройству-приемнику Google Cast */
class CastUseCase(
    private val mediaRouter: MediaRouter,
    private val castContext: CastContext,
    private val viewModelScope: CoroutineScope
) {
    private val _status = MutableStateFlow(STATUS_DEFAULT)
    val status: StateFlow<String> = _status.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        setCastStateListener()
        setSessionManagerListener()
        setCastSessionListener()
    }

    /**Метод отслеживает состояние подключения и передает ссылку на видео подходящему устройству*/
    suspend fun startCasting() {
        _status.value = STATUS_SEARCHING
        var device: MediaRouter.RouteInfo? = null
        try {
            device = searchDevicesInNetwork()
            if (device == null) {
                _status.value = STATUS_SEARCHING_NOT_FOUND
                setDefaultStatusWithDelay()
                setToastMessage("Не удалось найти устройства")
                return
            }
            _status.value = STATUS_CONNECTING
        } catch (e: Exception) {
            _status.value = STATUS_CONNECTING_FAILED
            setToastMessage("Произошла ошибка при подключении")
        }
        _status.value = "Подключение к ${device?.name}"

        val isSent = sendLinkToDevice()
        if (isSent) {
            setStatus(STATUS_SENDED)
            setToastMessage("Видео успешно отправлено на ${device?.name}!")
        } else {
            setStatus(STATUS_SEND_FAILED)
            setToastMessage("Не удалось отправить видео на ${device?.name}!")
        }
        setDefaultStatusWithDelay()
    }

    /** Метод для поиска устройств в сети, ищет первое подходящее устройство и возвращает его*/
    private suspend fun searchDevicesInNetwork(): MediaRouter.RouteInfo? {
        Log.d(TAG_SEARCH, "Searching started")
        val devices = mutableSetOf<MediaRouter.RouteInfo>()
        addCachedRoutes(devices)
        Log.d(TAG_SEARCH, "CashedRoutes Added: ${devices.map { it.id to it.name }}")
        // Получаем mediaRouteSelector
        val mediaRouteSelector = MediaRouteSelector.Builder()
            .addControlCategory(CastMediaControlIntent.categoryForCast(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID))
//            .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            .build()

        // Создаем callback для отслеживания доступных устройств
        val callback = object : MediaRouter.Callback() {
            override fun onRouteAdded(router: MediaRouter, route: MediaRouter.RouteInfo) {
                // Проверяем маршрут
                if (!devices.any { it.id == route.id } // его нет в списке
                    && route.isEnabled // маршрут включен
                    && !route.id.contains("DEFAULT_ROUTE")) // исключаем маршрут на устойство по умолчанию (обычно телефон)
                    devices.add(route)
                Log.d(TAG_SEARCH, "onRouteAdded: $route")
            }

            override fun onRouteRemoved(router: MediaRouter, route: MediaRouter.RouteInfo) {
                val removedRoute = devices.find { it.id == route.id }
                devices.remove(removedRoute)
                Log.d(TAG_SEARCH, "onRouteRemoved:${removedRoute?.name}")
            }
        }
        // Регистрируем callback с MediaRouter и выполняем сканирование
        mediaRouter.addCallback(mediaRouteSelector,callback,MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN)

        //Если устройства не найдены, ждем
        if (devices.isEmpty()) {
            delay(1000)
            addCachedRoutes(devices)
        }
        // Вывод найденных устройств в лог
        Log.d(TAG_SEARCH, "Найденные устройства:" + devices.filter {it.isEnabled}.map { it.name }.toString())

        // Отменяем callback после завершения поиска
        mediaRouter.removeCallback(callback)

        // Выбираем нужное устройство
        val castRoute = devices.firstOrNull()

        // Запускаем сессию с нужным устройством
        if (castRoute != null) {
            // Создаем Intent для начала сессии с выбранным устройством
            val intent = Intent(Intent.ACTION_MAIN)
            intent.putExtra("CAST_INTENT_TO_CAST_ROUTE_ID_KEY", castRoute.id)
            intent.putExtra("CAST_INTENT_TO_CAST_DEVICE_NAME_KEY", castRoute.name)
            intent.putExtra("CAST_INTENT_TO_CAST_NO_TOAST_KEY", true)

            try {
                //Если открыта предыдущая сессия - завершаем ее
                if(castContext.sessionManager.currentSession?.isConnected == true)
                    castContext.sessionManager.endCurrentSession(true)
                //Открываем новую сессию с выбранным устройством
                castContext.sessionManager.startSession(intent) //вариант 1
//                mediaRouter.selectRoute(castRoute)//вариант 2
                Log.d(TAG_SEARCH, "Selected CAST Route:${castRoute.name}")
            } catch (e: Exception) {
                Log.d(TAG_SEARCH, e.toString())
                e.printStackTrace()
                if (castContext.sessionManager.currentCastSession == null)
                    Log.d(TAG_SEARCH, "currentCastSession == null")
                if (castContext.sessionManager.currentSession == null)
                    Log.d(TAG_SEARCH, "currentSession == null")
                Log.d(
                    TAG_SEARCH,
                    "currentCastSession activeInputState == ${castContext.sessionManager.currentCastSession?.activeInputState}"
                )
            }
        }
        Log.d(TAG_SEARCH, "Searching ended")
        // После того как устройства были найдены, можно вернуть первое подходящее
        return castRoute
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
                .setStreamDuration(600000)// 10 мин
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
//                    it.play()
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

    /**Установка слушателя состояний передачи данных CastState*/
    private fun setCastSessionListener() {
        val listener = object :SessionManagerListener<CastSession>{
            override fun onSessionEnded(p0: CastSession, p1: Int) {
                Log.d(TAG_CAST_SESSION, "onSessionEnded")
            }

            override fun onSessionEnding(p0: CastSession) {
                Log.d(TAG_CAST_SESSION, "onSessionEnding")
            }

            override fun onSessionResumeFailed(p0: CastSession, p1: Int) {
                Log.d(TAG_CAST_SESSION, "onSessionResumeFailed")
            }

            override fun onSessionResumed(p0: CastSession, p1: Boolean) {
                Log.d(TAG_CAST_SESSION, "onSessionResumed")
            }

            override fun onSessionResuming(p0: CastSession, p1: String) {
                Log.d(TAG_CAST_SESSION, "onSessionResuming")
            }

            override fun onSessionStartFailed(p0: CastSession, p1: Int) {
                val error = CastStatusCodes.getStatusCodeString(p1)
                Log.d(TAG_CAST_SESSION, "onSessionStartFailed: error:$error")
            }

            override fun onSessionStarted(p0: CastSession, p1: String) {
                setStatus("onSessionStarted")
                Log.d(TAG_CAST_SESSION, "onSessionStarted")

            }

            override fun onSessionStarting(p0: CastSession) {
                Log.d(TAG_CAST_SESSION, "onSessionStarting")
            }

            override fun onSessionSuspended(p0: CastSession, p1: Int) {
                Log.d(TAG_CAST_SESSION, "onSessionSuspended")
            }

        }
        castContext.sessionManager.addSessionManagerListener(listener,CastSession::class.java)
        Log.d(TAG_CAST_SESSION, "addedCastSessionManagerListener")
    }

    /** Добавляем слушатель сессий*/
    private fun setSessionManagerListener() {
        // Реализация SessionManagerListener
        val sessionManagerListener = object : SessionManagerListener<Session> {
            override fun onSessionEnded(p0: Session, p1: Int) {
                Log.d(TAG_SESSION, "onSessionEnded")
            }

            override fun onSessionEnding(p0: Session) {
                Log.d(TAG_SESSION, "onSessionEnding")
            }

            override fun onSessionResumeFailed(p0: Session, p1: Int) {
                Log.d(TAG_SESSION, "onSessionResumeFailed")
            }

            override fun onSessionResumed(p0: Session, p1: Boolean) {
                Log.d(TAG_SESSION, "onSessionResumed")
            }

            override fun onSessionResuming(p0: Session, p1: String) {
                Log.d(TAG_SESSION, "onSessionResuming")
            }

            override fun onSessionStartFailed(p0: Session, p1: Int) {
                val error = CastStatusCodes.getStatusCodeString(p1)
                Log.d(TAG_SESSION, "onSessionStartFailed: error:$error")
            }

            override fun onSessionStarted(p0: Session, p1: String) {
                setStatus("onSessionStarted")
                Log.d(TAG_SESSION, "onSessionStarted")

            }

            override fun onSessionStarting(p0: Session) {
                Log.d(TAG_SESSION, "onSessionStarting")
            }

            override fun onSessionSuspended(p0: Session, p1: Int) {
                Log.d(TAG_SESSION, "onSessionSuspended")
            }

        }
        castContext.sessionManager
            .addSessionManagerListener(sessionManagerListener)

        Log.d(TAG_SESSION, "addedSessionManagerListener")
    }

    /**Метод добавляет в список кэшированные маршруты*/
    private fun addCachedRoutes(devices:MutableSet<MediaRouter.RouteInfo>){
        val cachedRoutes = mediaRouter.routes.filter { it.isEnabled && !it.id.contains("DEFAULT_ROUTE") }
        devices.addAll(cachedRoutes)
    }

    /**Метод эмиттит сообщение в статусе с задержкой*/
    private fun setDefaultStatusWithDelay() {
        viewModelScope.launch {
            delay(2000)
            setStatus(STATUS_DEFAULT)
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
        private const val TAG_SESSION = "CAST SESSION"
        private const val TAG_CAST_SESSION = "CAST CAST_SESSION"
        private const val TAG_SEARCH = "CAST SEARCHING"
        private const val TAG_SEND = "CAST SENDING"
        private const val TAG_STATE = "CAST STATE"
        private const val STATUS_DEFAULT = "Ожидание нажатия кнопки"
        private const val STATUS_CONNECTING = "Подключение к устройству..."
        private const val STATUS_CONNECTING_FAILED = "Ошибка при подключении"
        private const val STATUS_SEND_FAILED = "Ошибка передачи!"
        //private const val STATUS_SENDING = "Отправка видео..."
        private const val STATUS_SENDED = "Видео отправлено!"
        private const val STATUS_SEARCHING = "Поиск устройств..."
        private const val STATUS_SEARCHING_NOT_FOUND = "Устройства не найдены"
        const val VIDEO_LINK =
            "https://videolink-test.mycdn.me/?pct=1&sig=6QNOvp0y3BE&ct=0&clientType=45&mid=193241622673&type=5"
    }
}

