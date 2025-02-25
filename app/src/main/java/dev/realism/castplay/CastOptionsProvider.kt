package dev.realism.castplay

import android.content.Context
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.LaunchOptions
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions

/** CastOptionsProvider предоставляет настройки для Google Cast SDK,
 * такие как ID приложения-приемника,
 * поведение при подключении, управление сеансами и отладка.*/
class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions {
        val castMediaOptions = CastMediaOptions.Builder()
            .setMediaSessionEnabled(true)
            .build()

        val launchOptions = LaunchOptions.Builder()
            .setRelaunchIfRunning(true)
            .setAndroidReceiverCompatible(true)
            .build()

        val nameSpaces = listOf("urn:x-cast:com.google.cast.receiver","urn:x-cast:com.google.cast.media")

        return CastOptions.Builder()
            .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID) // Используем стандартный кастинг
            .setCastMediaOptions(castMediaOptions)
            .setLaunchOptions(launchOptions)
            .setSupportedNamespaces(nameSpaces)
            .setEnableReconnectionService(true)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }
}
