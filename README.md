# CastPlay - простое Android приложение
Содержит одну Activity с кнопкой по центру, по нажатию на кнопку приложение находит устройство Google Cast в локальной сети и запускает на устройстве воспроизведение видео по ссылке.
Приложение разрабатывалось в качестве тестового задания в VK.

## Стек технологий:
- **Kotlin** — основной язык разработки
- **Kotlin Coroutines** — для работы с многопоточностью
- **MVVM** — архитектурные паттерны для разделения данных, логики и UI
- **Jetpack Compose** - для работы с View- 
- **StateFlow / ViewModel** — для реактивного обновления UI
- **JUnit4, Robolectric, Mockk** - unit-тесты
- **Jacoco** - процент покрытия кода unit-тестами
- **Kaspresso, Espresso** - UI-тесты

## Требования:
- **Android Studio** последней версии.
- **JDK 8+** для компиляции проекта.
- **Минимальная версия Android** — **API 26 (Android 8.0)**.
  
## Установка и запуск:

### 1. Клонируйте репозиторий:
```bash
git clone https://github.com/realism-dev/castplay.git
```

### 2. Откройте проект в Android Studio:
В меню выберите File -> Open и выберите папку с клонированным проектом.

### 3. Синхронизируйте проект с Gradle:
После того как проект откроется, Android Studio предложит вам синхронизировать Gradle. Нажмите Sync Now.

### 4. Установите необходимые зависимости:
Проект использует несколько сторонних библиотек, указанных в файле build.gradle. Все они будут автоматически загружены при синхронизации Gradle.

### 5. Запуск приложения:
Подключите Android-устройство или запустите эмулятор.
Нажмите Run в Android Studio.

## Проблемы/вопросы, возникшие в процессе разработки

### Тестирование приложения
К сожалению, не удалось найти подходящее устройство-приемник Google Cast, а ценник на рынке ChromeCast MediaPlayer в 10000р совсем не располагает к приобретению.
Пробовал решить вопрос через приложения, имитирующие приемники:
#### Windows 11: X-Mirage, AirServer
устройство обнаруживается, но подключение невозможно
#### Android 10: CastReceiver, AirReceiverLite
Решили вопрос с тестированием приложения.

### Нерелеватная/устаревшая документация
В разработке использовал несколько источников:
- [StackOverFlow](https://stackoverflow.com/questions/46632109/start-cast-session-for-a-cast-device)
- [ExoPlayer](https://github.com/google/ExoPlayer/blob/release-v2/extensions/cast/src/main/java/com/google/android/exoplayer2/ext/cast/DefaultCastOptionsProvider.java)
- [Habr](https://habr.com/ru/companies/mobileup/articles/442300/)
- [Официальная документация и кодлабы Google](https://developers.google.com/cast?hl=ru)

## [MIT License](https://github.com/Realism-dev/CastPlay/blob/master/LICENSE)
