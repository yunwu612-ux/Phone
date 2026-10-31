package com.yunwu.phone

import android.telecom.Call
import android.telecom.InCallService
import android.content.Intent
import android.os.Build
import android.util.Log

class YunWuInCallService : InCallService() {
    companion object {
        @Volatile var activeCall: Call? = null
            private set
        private const val TAG = "YunWuCall"
        @Volatile private var instance: YunWuInCallService? = null

        fun toggleMute() {
            instance?.let { service ->
                val current = service.callAudioState?.isMuted ?: false
                service.setMuted(!current)
            }
        }
        fun enableSpeaker() {
            instance?.setAudioRoute(android.telecom.CallAudioState.ROUTE_SPEAKER)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        if (instance == this) instance = null
        super.onDestroy()
    }

    private var callback: Call.Callback? = null

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        activeCall = call
        callback = object : Call.Callback() {
            override fun onStateChanged(c: Call, state: Int) {
                if (state == Call.STATE_ACTIVE) {
                    AutoCallRecorder.start(this@YunWuInCallService)
                } else if (state == Call.STATE_DISCONNECTED) {
                    AutoCallRecorder.stop()
                }
                showCallUi()
            }
        }
        call.registerCallback(callback!!)
        if (call.state == Call.STATE_ACTIVE) AutoCallRecorder.start(this)
        showCallUi()
    }

    private fun showCallUi() {
        try {
            val i = Intent(this, InCallActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(i)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to show call UI", e)
        }
    }

    override fun onCallRemoved(call: Call) {
        call.unregisterCallback(callback)
        if (activeCall == call) activeCall = null
        AutoCallRecorder.stop()
        super.onCallRemoved(call)
    }
}
