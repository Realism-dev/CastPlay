package dev.realism.castplay
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.cast.framework.CastStateListener
import com.google.android.gms.cast.framework.Session
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.kotlin.isA
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.DEFAULT_MANIFEST_NAME)
@ExperimentalCoroutinesApi
class CastPlayViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule() // Для синхронной работы корутин

    private lateinit var castUseCase: CastUseCase
    private lateinit var viewModel: CastPlayViewModel
    private lateinit var viewModelScope:CoroutineScope

    private val testDispatcher = StandardTestDispatcher() // Новый диспетчер для тестирования корутин
    private val _toastMessage = MutableStateFlow<String?>(null)
    private val _status = MutableStateFlow("")

    @Before
    fun setUp() {
        // Устанавливаем новый тестовый диспетчер для главного потока
        Dispatchers.setMain(testDispatcher)
        // Используем TestCoroutineScope для тестирования корутин
        viewModelScope = TestScope()

        // Мокируем зависимости для CastUseCase
        val sessionManager:SessionManager = mockk(relaxed = true)
        val castContext: CastContextInterface = mockk(relaxed = true)

        every { castContext.addCastStateListener(isA<CastStateListener>()) } just Runs
        every { castContext.sessionManager } returns mockk()
        every { castContext.addCastStateListener(any()) } just Runs
        every { sessionManager.addSessionManagerListener(isA<SessionManagerListener<Session>>()) } just Runs
        every { sessionManager.addSessionManagerListener(isA<SessionManagerListener<Session>>(), any()) } just Runs



//        every { mediaRouter.routes } returns emptyList()
//        every { mediaRouter.addCallback(any(), any()) } just Runs
//        every { mediaRouter.removeCallback(any()) } just Runs

        // Создаем объект CastUseCase с мокированными зависимостями
        castUseCase = CastUseCase(castContext,viewModelScope)

        // Мокируем CastUseCase для имитации данных
        every { castUseCase.toastMessage } returns _toastMessage
        every { castUseCase.status } returns _status

        // Создаем ViewModel с мокированным CastUseCase
        viewModel = CastPlayViewModel(castUseCase)
    }

/*
    @Test
    fun testSessionManagerListener() {
        // Проверяем вызов addSessionManagerListener
        castUseCase.addSessionManagerListener()

        // Проверяем, что addSessionManagerListener был вызван на mock-объекте sessionManager
        verify { sessionManager.addSessionManagerListener(any()) }
    }

    @Test
    fun `test startCasting should call startCasting from useCase`() = runTest {
        // Пример успешного сценария: проверяем, что метод startCasting вызывается
        Mockito.doNothing().`when`(castUseCase).startCasting()

        // Вызываем метод вьюмодели
        viewModel.startCasting()

        // Проверяем, что startCasting был вызван на castUseCase
        Mockito.verify(castUseCase).startCasting()
    }

    @Test
    fun `test clearToastMessage should call clearToastMessage from useCase`() {
        // Пример теста для clearToastMessage: проверяем, что метод вызывается
        viewModel.clearToastMessage()

        // Проверяем, что clearToastMessage был вызван на castUseCase
        Mockito.verify(castUseCase).clearToastMessage()
    }

    @Test
    fun `test toastMessage should be emitted`() = runTest {
        // Устанавливаем значение для _toastMessage
        _toastMessage.value = "Test message"

        // Проверяем, что значение toastMessage передается правильно
        assertEquals("Test message", viewModel.toastMessage.value)
    }

    @Test
    fun `test status should be emitted correctly`() = runTest {
        // Устанавливаем значение для _status
        _status.value = "Casting in progress"

        // Проверяем, что статус отображается правильно
        assertEquals("Casting in progress", viewModel.status.value)
    }

    @Test
    fun `test exception handling in startCasting`() = runTest {
        // Пример: при ошибке в startCasting
        Mockito.doThrow(Exception("Test Exception")).`when`(castUseCase).startCasting()

        // Вызываем метод, он должен ловить исключение
        viewModel.startCasting()

        // Проверяем, что исключение не привело к сбою, просто логируется
        Mockito.verify(castUseCase).startCasting() // Убедились, что метод был вызван
    }
*/

}