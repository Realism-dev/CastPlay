package dev.realism.castplay

import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener

interface CastContextInterface {
    val sessionManager:SessionManager
    fun addSessionManagerListener(listener: SessionManagerListener<CastSession>, java: Class<CastSession>)
}
