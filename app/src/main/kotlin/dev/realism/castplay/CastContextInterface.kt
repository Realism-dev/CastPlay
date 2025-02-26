package dev.realism.castplay

import com.google.android.gms.cast.framework.CastStateListener
import com.google.android.gms.cast.framework.SessionManager

interface CastContextInterface {
    val sessionManager:SessionManager
    fun addCastStateListener(listener: CastStateListener)
}
