package com.yunwu.phone

import android.app.Activity
import android.os.Bundle
import android.telecom.Call
import android.view.Gravity
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import android.content.Context

class InCallActivity : Activity() {
    private val blue = Color.rgb(57, 120, 232)
    private val ink = Color.rgb(35, 48, 67)
    private val muted = Color.rgb(125, 139, 158)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val call = YunWuInCallService.activeCall
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 36)
            setBackgroundColor(Color.rgb(247, 249, 252))
        }
        root.addView(TextView(this).apply {
            text = "YunWu Phone"; textSize = 19f; setTextColor(blue)
        })
        val details = call?.details
        val handle = details?.handle?.schemeSpecificPart ?: "未知号码"
        root.addView(TextView(this).apply {
            text = handle; textSize = 29f; typeface = Typeface.DEFAULT_BOLD
            setTextColor(ink); setPadding(0, 35, 0, 10)
        })
        root.addView(TextView(this).apply {
            text = when (call?.state) {
                Call.STATE_RINGING -> "来电中"
                Call.STATE_DIALING -> "正在呼叫…"
                Call.STATE_ACTIVE -> "通话中"
                Call.STATE_HOLDING -> "已保持"
                else -> "通话"
            }
            textSize = 16f; setTextColor(muted)
        })
        root.addView(TextView(this).apply {
            text = "自动录音：${AutoCallRecorder.lastStatus}"
            textSize = 13f; setTextColor(muted); setPadding(0, 14, 0, 28)
        })
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER }
        row.addView(action("静音") { YunWuInCallService.toggleMute() })
        row.addView(action("免提") { YunWuInCallService.enableSpeaker() })
        row.addView(action("保持") { call?.hold() })
        root.addView(row)
        val primary = when (call?.state) {
            Call.STATE_RINGING -> action("接听", blue) { call.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY) }
            else -> action("挂断", Color.rgb(220, 65, 75)) { call?.disconnect(); finish() }
        }
        val lp = LinearLayout.LayoutParams(-2, -2)
        lp.topMargin = 40
        root.addView(primary, lp)
        setContentView(root)
    }

    private fun action(text: String, color: Int = Color.WHITE, click: () -> Unit): TextView =
        TextView(this).apply {
            this.text = text; textSize = 16f; gravity = Gravity.CENTER
            setTextColor(if (color == Color.WHITE) ink else Color.WHITE)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(if (color == Color.WHITE) Color.WHITE else color)
                cornerRadius = 48f
            }
            setPadding(22, 16, 22, 16)
            setOnClickListener { click(); render() }
            val p = LinearLayout.LayoutParams(-2, -2)
            p.setMargins(7, 5, 7, 5)
            layoutParams = p
        }
}
