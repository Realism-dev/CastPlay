package dev.realism.castplay

import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.CastStateListener
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener

interface CastContextInterface {
    val sessionManager:SessionManager
    fun addCastStateListener(listener: CastStateListener)
    fun addSessionManagerListener(listener: SessionManagerListener<CastSession>, java: Class<CastSession>)
}
