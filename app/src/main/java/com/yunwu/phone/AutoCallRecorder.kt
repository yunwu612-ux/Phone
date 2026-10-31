package com.yunwu.phone

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AutoCallRecorder {
    private const val TAG = "YunWuRecorder"
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    @Volatile var lastStatus: String = "等待通话"
        private set

    @Synchronized fun start(context: Context) {
        if (recorder != null) return
        if (context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED) {
            lastStatus = "未获得麦克风权限，无法录音"
            return
        }
        val dir = File(context.getExternalFilesDir(null), "CallRecordings")
        if (!dir.exists()) dir.mkdirs()
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(dir, "Call_$stamp.m4a")
        try {
            val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else MediaRecorder()
            // VOICE_CALL is restricted on many stock Android builds. Failure is reported;
            // do not silently fall back to microphone and label it as a two-way call recording.
            r.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioSamplingRate(44100)
            r.setAudioEncodingBitRate(128000)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            currentFile = file
            lastStatus = "正在尝试录制通话双方声音"
        } catch (e: Exception) {
            Log.e(TAG, "Call recording unavailable on this device", e)
            try { recorder?.release() } catch (_: Exception) {}
            recorder = null
            file.delete()
            lastStatus = "系统限制通话录音：${e.javaClass.simpleName}"
        }
    }

    @Synchronized fun stop() {
        val r = recorder ?: return
        try {
            r.stop()
            lastStatus = "录音已保存"
        } catch (e: Exception) {
            lastStatus = "录音失败或录音时长不足"
            currentFile?.delete()
        } finally {
            try { r.reset(); r.release() } catch (_: Exception) {}
            recorder = null
            currentFile = null
        }
    }

    fun recordingsDir(context: Context): File =
        File(context.getExternalFilesDir(null), "CallRecordings")
}
