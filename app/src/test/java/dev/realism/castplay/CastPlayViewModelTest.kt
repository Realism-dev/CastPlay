package dev.realism.castplay

import com.google.android.gms.cast.framework.SessionManager
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@ExperimentalCoroutinesApi
class CastPlayViewModelTest {
    private lateinit var castUseCase: CastUseCase
    private lateinit var viewModel: CastPlayViewModel

    private val _toastMessage = MutableStateFlow<String?>(null)
    private val _status = MutableStateFlow("")

    @Before
    fun setUp() {
        // Мокируем зависимости для castContext
        val castContext: CastContextInterface = mockk()
        val sessionManager = mockk<SessionManager> {
            every { addSessionManagerListener(any()) } just Runs
        }
        every { castContext.sessionManager } returns sessionManager

        // Создаем объект CastUseCase с мокированными зависимостями
        castUseCase = mockk<CastUseCase>()
        every { castUseCase.toastMessage } returns _toastMessage
        every { castUseCase.status } returns _status

        // Создаем ViewModel с мокированным CastUseCase
        viewModel = CastPlayViewModel(castUseCase)
    }

    @Test
    fun `test toastMessage should be updated correctly`() = runTest {
        // Эмитируем новое сообщение
        _toastMessage.emit("New toast message")

        // Проверяем, что в ViewModel toastMessage теперь это значение
        assert(viewModel.toastMessage.value == "New toast message")
    }

    @Test
    fun `test startCasting should call clearToastMessage from useCase`() = runTest {
        // Мокируем поведение метода clearToastMessage в castUseCase
        every { castUseCase.clearToastMessage() } just Runs

        // Вызываем метод вьюмодели
        viewModel.clearToastMessage()

        // Проверяем, что метод clearToastMessage был вызван
        verify { castUseCase.clearToastMessage() }
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
        // Мокируем startCasting, чтобы он выбрасывал исключение
        every { castUseCase.clearToastMessage() } throws Exception("Test Exception")

        // Вызываем метод, он должен ловить исключение
        viewModel.clearToastMessage()

        // Проверяем, что метод был вызван
        verify { castUseCase.clearToastMessage() }
    }
}