package dev.realism.castplay

import android.util.Log
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.api.PendingResult
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.DEFAULT_MANIFEST_NAME)
@ExperimentalCoroutinesApi
class CastUseCaseTest {
    private lateinit var castUseCase: CastUseCase
    private lateinit var castUseCaseMocked: CastUseCase
    private lateinit var castUseCaseSpyk: CastUseCase
    private lateinit var viewModel: CastPlayViewModel

    private val _toastMessage = MutableStateFlow<String?>(null)
    private val _status = MutableStateFlow("")

    private val castContextMocked: CastContextInterface = mockk(relaxed = true)
    private val sessionManagerMocked: SessionManager = mockk(relaxed = true)
    private val castSessionMocked: CastSession = mockk(relaxed = true)
    private val mediaClientMocked: RemoteMediaClient = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { castContextMocked.sessionManager } returns sessionManagerMocked
        every { sessionManagerMocked.currentCastSession } returns castSessionMocked
        every { castSessionMocked.remoteMediaClient } returns mediaClientMocked

        // Создаем объект CastUseCase с мокированными зависимостями
        castUseCase = CastUseCase(castContextMocked, testScope)
        castUseCaseMocked = mockk(relaxed = true)
        castUseCaseSpyk = spyk(castUseCase, recordPrivateCalls = true)
        every { castUseCaseMocked.toastMessage } returns _toastMessage
        every { castUseCaseMocked.status } returns _status

        // Создаем ViewModel с реальным CastUseCase
        viewModel = CastPlayViewModel(castUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**sendLinkToDevice*/

    // Хак для вызова private метода sendLinkToDevice
    private fun CastUseCase.invokeSendLinkToDevice(castContext: CastContextInterface): Boolean {
        // Получаем доступ к private методу
        val method = CastUseCase::class.java.getDeclaredMethod(
            "sendLinkToDevice",
            CastContextInterface::class.java
        )
        method.isAccessible = true
        return method.invoke(this, castContext) as Boolean
    }

    private fun CastUseCase.invokeSetCastSessionListener() {
        try {
            val method = CastUseCase::class.java.getDeclaredMethod("setCastSessionListener")
            method.isAccessible = true
            method.invoke(this)
            Log.d("TEST", "setCastSessionListener invoked successfully")
        } catch (e: Exception) {
            Log.e("TEST", "Error invoking setCastSessionListener: ${e.message}")
            throw e // или обработайте ошибку по-другому
        }
    }


    private fun CastUseCase.invokeSetStatusMessage(status: String) {
        // Получаем доступ к private методу
        val method = CastUseCase::class.java.getDeclaredMethod(
            "setStatusMessage",
            String::class.java
        )
        method.isAccessible = true
        method.invoke(this, status)
    }

    private fun CastUseCase.invokeSetDefaultStatusWithDelay() {
        // Получаем доступ к private методу
        val method = CastUseCase::class.java.getDeclaredMethod("setDefaultStatusWithDelay")
        method.isAccessible = true
        method.invoke(this)
    }

    private fun CastUseCase.invokeSetToastMessage(message: String) {
        // Получаем доступ к private методу
        val method = CastUseCase::class.java.getDeclaredMethod(
            "setToastMessage",
            String::class.java
        )
        method.isAccessible = true
        method.invoke(this, message)
    }

    private fun CastUseCase.getCompanionObjectConstants(constName: String): String {
        val field = this::class.java.getDeclaredField(constName)
        field.isAccessible = true
        return field.get(null) as String
    }

    private fun CastUseCase.getCastContext(): CastContextInterface {
        val field = this::class.java.getDeclaredField("castContext")
        field.isAccessible = true
        return field.get(this) as CastContextInterface
    }

    private fun CastUseCase.getIsConnecting(): Boolean {
        val field = this::class.java.getDeclaredField("isConnecting")
        field.isAccessible = true
        return field.get(this) as Boolean
    }

    private fun CastUseCase.setIsConnecting(value: Boolean) {
        val field = this::class.java.getDeclaredField("isConnecting")
        field.isAccessible = true
        field.setBoolean(this, value)
    }

//    private fun CastUseCase.getSessionListener(): SessionManagerListener<CastSession> {
//        val field = this::class.java.getDeclaredField("sessionListener")
//        field.isAccessible = true
//        return field.get(this) as SessionManagerListener<CastSession>
//    }


    @Test
    fun `sendLinkToDevice should return true when load is successful`() {
        // Мокаем возвращаемый объект PendingResult
        val pendingResult = mockk<PendingResult<RemoteMediaClient.MediaChannelResult>>()
        val mediaChannelResult = mockk<RemoteMediaClient.MediaChannelResult>()

        // Настроим, чтобы load возвращал наш pendingResult
        every {
            mediaClientMocked.load(
                ofType(MediaInfo::class),
                ofType(MediaLoadOptions::class)
            )
        } returns pendingResult
        every { castSessionMocked.isConnected } returns true
        every { pendingResult.await() } returns mediaChannelResult // Симулируем успешное завершение

        // Проверка, что результат равен true
        val result = castUseCase.invokeSendLinkToDevice(castContextMocked)
        assertTrue(result)
    }

    @Test
    fun `sendLinkToDevice should return false when castSession is not connected`() {
        // Мокаем возвращаемый объект PendingResult
        val pendingResult = mockk<PendingResult<RemoteMediaClient.MediaChannelResult>>()
        val mediaChannelResult = mockk<RemoteMediaClient.MediaChannelResult>()

        // Настроим, чтобы load возвращал наш pendingResult
        every {
            mediaClientMocked.load(
                ofType(MediaInfo::class),
                ofType(MediaLoadOptions::class)
            )
        } returns pendingResult
        every { castSessionMocked.isConnected } returns false
        every { pendingResult.await() } returns mediaChannelResult // Симулируем успешное завершение

        // Проверка, что результат равен false
        val result = castUseCase.invokeSendLinkToDevice(castContextMocked)
        assertFalse(result)
    }

    @Test
    fun `sendLinkToDevice should return false when castSession is null`() {
        // Мокаем возвращаемый объект PendingResult
        val pendingResult = mockk<PendingResult<RemoteMediaClient.MediaChannelResult>>()
        val mediaChannelResult = mockk<RemoteMediaClient.MediaChannelResult>()

        // Настроим, чтобы load возвращал наш pendingResult
        every {
            mediaClientMocked.load(
                ofType(MediaInfo::class),
                ofType(MediaLoadOptions::class)
            )
        } returns pendingResult
        every { castSessionMocked.isConnected } returns false
        every { sessionManagerMocked.currentCastSession } returns null
        every { pendingResult.await() } returns mediaChannelResult // Симулируем успешное завершение

        // Проверка, что результат равен false
        val result = castUseCase.invokeSendLinkToDevice(castContextMocked)
        assertFalse(result)
    }


    @Test
    fun `sendLinkToDevice should return true when session is active and media loads successfully`() {
        val pendingResult = mockk<PendingResult<RemoteMediaClient.MediaChannelResult>>()
        every { pendingResult.await() } returns mockk() // Если вы используете Deferred или аналогичный подход для ожидания результата
        every { castSessionMocked.isConnected } returns true
        every {
            mediaClientMocked.load(
                ofType(MediaInfo::class),
                ofType(MediaLoadOptions::class)
            )
        } returns pendingResult
        val result = castUseCase.invokeSendLinkToDevice(castContextMocked)
        assertTrue(result)
    }

    @Test
    fun `sendLinkToDevice should return false when session is active and media loads failed`() {
        val pendingResult = mockk<PendingResult<RemoteMediaClient.MediaChannelResult>>()
        every { pendingResult.await() } returns mockk() // Если вы используете Deferred или аналогичный подход для ожидания результата
        every { castSessionMocked.isConnected } returns true
        every {
            mediaClientMocked.load(
                ofType(MediaInfo::class),
                ofType(MediaLoadOptions::class)
            )
        } throws Exception("Failed to load media")
        val result = castUseCase.invokeSendLinkToDevice(castContextMocked)
        assertFalse(result)
    }

    @Test
    fun `sendLinkToDevice should return false when mediaClient is null`() {
        every { castSessionMocked.isConnected } returns true
        every { castSessionMocked.remoteMediaClient } returns null

        val result = castUseCase.invokeSendLinkToDevice(castContextMocked)

        assertFalse(result)
    }

    /**SetSessionListener*/

    @Test
    fun `setCastSessionListener should add session manager listener`() {
        every {
            castContextMocked.addSessionManagerListener(
                any(),
                CastSession::class.java
            )
        } just Runs
        castUseCase.invokeSetCastSessionListener()
        verify { castContextMocked.addSessionManagerListener(any(), CastSession::class.java) }
    }

    /**Проверяем позитивный сценарий при отработке метода onSessionStarted*/
    @Test
    fun `onSessionStarted should call sendLinkToDevice, return true and set toast and status success message`() {
        // Статусы и тосты
        val statusSended = castUseCase.getCompanionObjectConstants("STATUS_SENDED")
        val toastSended = "Видео успешно отправлено на Test Device!"

        //Мокаем поведение
        every { castUseCaseSpyk.invokeSendLinkToDevice(castContextMocked) } returns true
        every { sessionManagerMocked.currentCastSession } returns castSessionMocked
        every { castSessionMocked.castDevice?.friendlyName } returns "Test Device"

        // Захватываем слушателя
        val sessionListenerSlot = slot<SessionManagerListener<CastSession>>()
        every {
            castContextMocked.addSessionManagerListener(
                capture(sessionListenerSlot),
                CastSession::class.java
            )
        } just Runs

        // Вызываем метод для установки слушателя
        castUseCaseSpyk.invokeSetCastSessionListener()

        // Проверяем, что слушатель захвачен
        assertNotNull(sessionListenerSlot.captured)

        // Вручную вызываем onSessionStarted через захваченный слушатель
        sessionListenerSlot.captured.onSessionStarted(castSessionMocked, "session_id")

        // Проверяем результат выполнения onSessionStarted
        verify { castUseCaseSpyk.invokeSendLinkToDevice(castContextMocked) } // проверяем наличие вызова
        assertTrue(castUseCaseSpyk.invokeSendLinkToDevice(castContextMocked)) // проверяем что видео отправлено
        assertEquals(
            "Test Device",
            castSessionMocked.castDevice?.friendlyName
        ) // проверяем имя устройства
        verify { castUseCaseSpyk.invokeSetStatusMessage(statusSended) }// проверяем вызов метода установки статуса
        assertEquals(statusSended, castUseCaseSpyk.status.value) // проверяем статус
        verify { castUseCaseSpyk.invokeSetToastMessage(toastSended) }// проверяем вызов метода установки тоста
        assertEquals(toastSended, castUseCaseSpyk.toastMessage.value) // проверяем тост
    }

    /**Проверяем негативный сценарий при отработке метода onSessionStarted*/
    @Test
    fun `onSessionStarted should not call sendLinkToDevice, return false and set toast and status failed message`() {
        // Статусы и тосты
        val statusFailed = castUseCase.getCompanionObjectConstants("STATUS_SEND_FAILED")
        val toastFailed = "Не удалось отправить видео на Test Device!"

        //Мокаем поведение
        every { castUseCase.invokeSendLinkToDevice(castContextMocked) } returns false
        every { sessionManagerMocked.currentCastSession } returns castSessionMocked
        every { castSessionMocked.castDevice?.friendlyName } returns "Test Device"

        // Захватываем слушателя
        val sessionListenerSlot = slot<SessionManagerListener<CastSession>>()
        every {
            castContextMocked.addSessionManagerListener(
                capture(sessionListenerSlot),
                CastSession::class.java
            )
        } just Runs

        // Вызываем метод для установки слушателя
        castUseCaseSpyk.invokeSetCastSessionListener()

        // Проверяем, что слушатель захвачен
        assertNotNull(sessionListenerSlot.captured)

        // Вручную вызываем onSessionStarted через захваченный слушатель
        sessionListenerSlot.captured.onSessionStarted(castSessionMocked, "session_id")

        // Проверяем результат выполнения onSessionStarted
        verify { castUseCaseSpyk.invokeSendLinkToDevice(castContextMocked) } // проверяем наличие вызова
        assertFalse(castUseCaseSpyk.invokeSendLinkToDevice(castContextMocked)) // проверяем что видео НЕ отправлено
        assertEquals(
            "Test Device",
            castSessionMocked.castDevice?.friendlyName
        ) // проверяем имя устройства
        verify { castUseCaseSpyk.invokeSetStatusMessage(statusFailed) }// проверяем вызов метода установки статуса
        assertEquals(statusFailed, castUseCaseSpyk.status.value) // проверяем статус
        verify { castUseCaseSpyk.invokeSetToastMessage(toastFailed) }// проверяем вызов метода установки тоста
        assertEquals(toastFailed, castUseCaseSpyk.toastMessage.value) // проверяем тост
    }

    /**Проверяем, что ДО отработки метода onSessionStarted isConnecting = true,
     * а ПОСЛЕ  переменная isConnecting = false*/
    @Test
    fun `onSessionStarted checking isConnecting field`() {
        // Захватываем слушателя
        val sessionListenerSlot = slot<SessionManagerListener<CastSession>>()
        every {
            castContextMocked.addSessionManagerListener(
                capture(sessionListenerSlot),
                CastSession::class.java
            )
        } just Runs

        // Вызываем метод для установки слушателя
        castUseCaseSpyk.invokeSetCastSessionListener()

        // Проверяем, что слушатель захвачен
        assertNotNull(sessionListenerSlot.captured)

        // Вручную вызываем onSessionStarted через захваченный слушатель
        sessionListenerSlot.captured.onSessionStarting(castSessionMocked)

        // isConnecting = true
        assertTrue(castUseCaseSpyk.getIsConnecting())

        // Вручную вызываем onSessionStarted через захваченный слушатель
        sessionListenerSlot.captured.onSessionStarted(castSessionMocked, "session_id")

        // isConnecting = false
        assertFalse(castUseCaseSpyk.getIsConnecting())
    }

    @Test
    fun `onSessionStartFailed should set toast and status failed message`() {
        // Статусы и тосты
        val statusFailed = castUseCase.getCompanionObjectConstants("STATUS_CONNECTING_FAILED")
        val toastFailed = castUseCase.getCompanionObjectConstants("STATUS_CONNECTING_FAILED")

        // Захватываем слушателя
        val sessionListenerSlot = slot<SessionManagerListener<CastSession>>()
        every {
            castContextMocked.addSessionManagerListener(
                capture(sessionListenerSlot),
                CastSession::class.java
            )
        } just Runs

        // Вызываем метод для установки слушателя
        castUseCaseSpyk.invokeSetCastSessionListener()

        // Проверяем, что слушатель захвачен
        assertNotNull(sessionListenerSlot.captured)

        // Вручную вызываем onSessionStartFailed через захваченный слушатель
        val errorCode = 0
        sessionListenerSlot.captured.onSessionStartFailed(castSessionMocked, errorCode)

        // Проверяем результат выполнения onSessionStarted
        assertEquals(toastFailed, castUseCaseSpyk.toastMessage.value) // проверяем тост
        assertEquals(statusFailed, castUseCaseSpyk.status.value) // проверяем статус
    }

    @Test
    fun `onSessionStartFailed should call setDefaultStatusWithDelay with 2 seconds delay`() {
        // Статусы и тосты
        val defaultStatus = castUseCase.getCompanionObjectConstants("STATUS_DEFAULT")
        val statusFailed = castUseCase.getCompanionObjectConstants("STATUS_CONNECTING_FAILED")
        val toastFailed = castUseCase.getCompanionObjectConstants("STATUS_CONNECTING_FAILED")

        // Мокаем зависимости и захватываем слушателя
        val sessionListenerSlot = slot<SessionManagerListener<CastSession>>()
        every {
            castContextMocked.addSessionManagerListener(
                capture(sessionListenerSlot),
                CastSession::class.java
            )
        } just Runs

        // Устанавливаем слушатель
        castUseCaseSpyk.invokeSetCastSessionListener()

        // Проверяем, что слушатель был захвачен
        assertNotNull(sessionListenerSlot.captured)

        // Вручную вызываем onSessionStartFailed через захваченный слушатель
        sessionListenerSlot.captured.onSessionStartFailed(castSessionMocked, 0)

        // Проверяем, что STATUS_CONNECTING_FAILED был установлен в тосте и статусе
        assertEquals(statusFailed, castUseCaseSpyk.status.value)
        assertEquals(toastFailed, castUseCaseSpyk.toastMessage.value)

        // Прокачиваем время, чтобы завершить setDefaultStatusWithDelay
        testScope.advanceTimeBy(3000)

        // Проверяем вызов метода установки слушателя
        verify { castUseCaseSpyk["setCastSessionListener"]() }

        // Проверяем, что метод был вызван
        coVerify { castUseCaseSpyk["setDefaultStatusWithDelay"]() }

        // Проверяем что после задерки в 2 сек ставится дефолтный статус
        assertEquals(defaultStatus, castUseCaseSpyk.status.value)
        // isConnecting = false
        assertFalse(castUseCaseSpyk.getIsConnecting())
    }

    @Test
    fun `onSessionStarting should call setConnectingStatusWithDelay and set isConnecting = true`() {
        // Мокаем зависимости и захватываем слушателя
        val sessionListenerSlot = slot<SessionManagerListener<CastSession>>()
        every {
            castContextMocked.addSessionManagerListener(
                capture(sessionListenerSlot),
                CastSession::class.java
            )
        } just Runs
        // Устанавливаем слушатель
        castUseCaseSpyk.invokeSetCastSessionListener()
        // Мокаем поведение
        every { sessionManagerMocked.currentCastSession } returns castSessionMocked
        every { castSessionMocked.castDevice?.friendlyName } returns "Test Device"
        val connectingStatusMessage = "Подключение к Test Device"
        val connectedStatusMessage = "Подключено к Test Device"

        // Проверяем, что слушатель был захвачен
        assertNotNull(sessionListenerSlot.captured)

        // Вручную вызываем onSessionStarting через захваченный слушатель
        sessionListenerSlot.captured.onSessionStarting(castSessionMocked)

        // isConnecting = true
        assertTrue(castUseCaseSpyk.getIsConnecting())
        testScope.advanceTimeBy(1) // нужно для смены статуса
        assertEquals(connectingStatusMessage, castUseCaseSpyk.status.value) // проверяем статус

        // Прокачиваем время, чтобы завершить setConnectingStatusWithDelay
        testScope.advanceTimeBy(3000)
        // устанавливаем isConnecting = false, чтобы прервать цикл
        castUseCaseSpyk.setIsConnecting(false)
        assertFalse(castUseCaseSpyk.getIsConnecting())

        // Суммарное время всех delay в методе setConnectingStatusWithDelay 1200
        testScope.advanceTimeBy(1200) // нужно для смены статуса
        assertEquals(connectedStatusMessage, castUseCaseSpyk.status.value) // проверяем статус
    }

    @Test
    fun `onSessionEnded should set default status and set isConnecting = false`() {
        // Статусы и тосты
        val defaultStatus = castUseCase.getCompanionObjectConstants("STATUS_DEFAULT")

        // Мокаем зависимости и захватываем слушателя
        val sessionListenerSlot = slot<SessionManagerListener<CastSession>>()
        every {
            castContextMocked.addSessionManagerListener(
                capture(sessionListenerSlot),
                CastSession::class.java
            )
        } just Runs
        // Устанавливаем слушатель
        castUseCaseSpyk.invokeSetCastSessionListener()

        // Проверяем, что слушатель был захвачен
        assertNotNull(sessionListenerSlot.captured)

        // Вручную вызываем onSessionEnded через захваченный слушатель
        sessionListenerSlot.captured.onSessionEnded(castSessionMocked, 0)

        // isConnecting = false
        assertFalse(castUseCaseSpyk.getIsConnecting())
        testScope.advanceTimeBy(1) // нужно для смены статуса
        assertEquals(defaultStatus, castUseCaseSpyk.status.value) // проверяем статус
    }

    /**Проверяем остальные колбэки, у всех поведение одинаковое - выставить isConnecting = false*/
    @Test
    fun `others session callbacks should set isConnecting = false`() {
        // Мокаем зависимости и захватываем слушателя
        val sessionListenerSlot = slot<SessionManagerListener<CastSession>>()
        every {
            castContextMocked.addSessionManagerListener(
                capture(sessionListenerSlot),
                CastSession::class.java
            )
        } just Runs
        // Устанавливаем слушатель
        castUseCaseSpyk.invokeSetCastSessionListener()

        // Проверяем, что слушатель был захвачен
        assertNotNull(sessionListenerSlot.captured)

        // Проверяем onSessionEnding
        castUseCaseSpyk.setIsConnecting(true)
        sessionListenerSlot.captured.onSessionEnding(castSessionMocked)
        assertFalse(castUseCaseSpyk.getIsConnecting())// isConnecting = false

        // Проверяем onSessionResumeFailed
        castUseCaseSpyk.setIsConnecting(true)
        sessionListenerSlot.captured.onSessionResumeFailed(castSessionMocked, 0)
        assertFalse(castUseCaseSpyk.getIsConnecting())// isConnecting = false

        // Проверяем onSessionResumed
        castUseCaseSpyk.setIsConnecting(true)
        sessionListenerSlot.captured.onSessionResumed(castSessionMocked, true)
        assertFalse(castUseCaseSpyk.getIsConnecting())// isConnecting = false

        // Проверяем onSessionResuming
        castUseCaseSpyk.setIsConnecting(true)
        sessionListenerSlot.captured.onSessionResuming(castSessionMocked, "true")
        assertFalse(castUseCaseSpyk.getIsConnecting())// isConnecting = false

        // Проверяем onSessionSuspended
        castUseCaseSpyk.setIsConnecting(true)
        sessionListenerSlot.captured.onSessionSuspended(castSessionMocked, 0)
        assertFalse(castUseCaseSpyk.getIsConnecting())// isConnecting = false
    }
}








