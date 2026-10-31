package com.yunwu.phone

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface

class MainActivity : Activity() {
    private val blue = Color.rgb(57, 120, 232)
    private val ink = Color.rgb(35, 48, 67)
    private val muted = Color.rgb(125, 139, 158)
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var number: EditText
    private var tab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNeededPermissions()
        val incoming = intent?.data
        buildShell()
        if (incoming?.scheme == "tel") number.setText(incoming.schemeSpecificPart ?: "")
        showDialer()
    }

    private fun requestNeededPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.RECORD_AUDIO
        )
        val missing = permissions.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) requestPermissions(missing.toTypedArray(), 100)
    }

    private fun buildShell() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(247, 249, 252))
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 12)
        }
        header.addView(TextView(this).apply {
            text = "YunWu Phone"
            textSize = 25f
            setTextColor(ink)
            typeface = Typeface.DEFAULT_BOLD
        })
        header.addView(TextView(this).apply {
            text = "简单、清爽的通话体验"
            textSize = 13f
            setTextColor(muted)
            setPadding(0, 5, 0, 0)
        })
        root.addView(header)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 12)
        }
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        val dock = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding(12, 10, 12, 14)
            elevation = 12f
        }
        dock.addView(dockButton("⌕\n拨号", 0), LinearLayout.LayoutParams(0, -2, 1f))
        dock.addView(dockButton("☷\n通讯录", 1), LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(dock)
        setContentView(root)
    }

    private fun dockButton(label: String, index: Int): View =
        TextView(this).apply {
            text = label
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(12, 8, 12, 8)
            setTextColor(if (tab == index) blue else muted)
            setOnClickListener {
                tab = index
                if (index == 0) showDialer() else showContacts()
                updateDock()
            }
            tag = "dock$index"
        }

    private fun updateDock() {
        for (i in 0..1) {
            val v = root.findViewWithTag<TextView>("dock$i")
            v?.setTextColor(if (tab == i) blue else muted)
        }
    }

    private fun showDialer() {
        tab = 0
        content.removeAllViews()
        content.addView(sectionTitle("拨号"))
        number = EditText(this).apply {
            textSize = 27f
            gravity = Gravity.CENTER
            hint = "输入电话号码"
            setSingleLine(true)
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setTextColor(ink)
            setHintTextColor(Color.LTGRAY)
            background = rounded(Color.WHITE, 18)
            setPadding(14, 18, 14, 18)
        }
        content.addView(number, LinearLayout.LayoutParams(-1, 76).apply { bottomMargin = 14 })
        val grid = arrayOf(
            arrayOf("1", "2", "3"), arrayOf("4", "5", "6"),
            arrayOf("7", "8", "9"), arrayOf("*", "0", "#")
        )
        grid.forEach { row ->
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.forEach { digit ->
                val b = TextView(this).apply {
                    text = digit
                    textSize = 25f
                    gravity = Gravity.CENTER
                    setTextColor(ink)
                    background = rounded(Color.WHITE, 40)
                    setOnClickListener { number.append(digit) }
                }
                line.addView(b, LinearLayout.LayoutParams(0, 58, 1f).apply {
                    setMargins(5, 5, 5, 5)
                })
            }
            content.addView(line)
        }
        val actions = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
        }
        val call = TextView(this).apply {
            text = "☎  拨打电话"
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = rounded(blue, 28)
            setPadding(30, 15, 30, 15)
            setOnClickListener { placeCall() }
        }
        val del = TextView(this).apply {
            text = "⌫"
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(muted)
            setPadding(24, 10, 24, 10)
            setOnClickListener {
                val s = number.text
                if (s.isNotEmpty()) s.delete(s.length - 1, s.length)
            }
        }
        actions.addView(del)
        actions.addView(call)
        content.addView(actions)
        content.addView(defaultDialerCard())
        updateDock()
    }

    private fun defaultDialerCard(): View {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 16)
            setPadding(16, 13, 16, 13)
        }
        box.addView(TextView(this).apply {
            text = "设为默认电话应用"
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ink)
        })
        box.addView(TextView(this).apply {
            text = "设为默认应用后，YunWu Phone 才能接管系统来电界面。"
            textSize = 13f
            setTextColor(muted)
            setPadding(0, 5, 0, 8)
        })
        box.addView(TextView(this).apply {
            text = "设置为默认电话应用  →"
            textSize = 14f
            setTextColor(blue)
            setOnClickListener { requestDialerRole() }
        })
        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.topMargin = 16
        box.layoutParams = lp
        return box
    }

    private fun requestDialerRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = getSystemService(RoleManager::class.java)
            if (rm.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                !rm.isRoleHeld(RoleManager.ROLE_DIALER)) {
                startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_DIALER), 200)
            } else Toast.makeText(this, "YunWu Phone 已是默认电话应用", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            startActivity(intent)
        }
    }

    private fun placeCall() {
        val value = number.text.toString().trim()
        if (value.isBlank()) {
            Toast.makeText(this, "请先输入电话号码", Toast.LENGTH_SHORT).show()
            return
        }
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 101)
            return
        }
        try {
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(value)}")))
        } catch (e: Exception) {
            Toast.makeText(this, "无法拨打电话：${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showContacts() {
        tab = 1
        content.removeAllViews()
        content.addView(sectionTitle("通讯录"))
        val search = EditText(this).apply {
            hint = "搜索联系人"
            textSize = 15f
            background = rounded(Color.WHITE, 14)
            setPadding(14, 12, 14, 12)
        }
        content.addView(search, LinearLayout.LayoutParams(-1, 52))
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this)
        scroll.addView(list)
        content.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        fun load(filter: String = "") {
            list.removeAllViews()
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                list.addView(label("请允许读取通讯录权限后查看联系人"))
                return
            }
            val seen = HashSet<String>()
            val cursor: Cursor? = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val name = it.getString(0) ?: "未知联系人"
                    val phone = it.getString(1) ?: ""
                    val key = "$name|$phone"
                    if (key in seen || !(name.contains(filter, true) || phone.contains(filter))) continue
                    seen.add(key)
                    val row = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        background = rounded(Color.WHITE, 14)
                        setPadding(15, 12, 15, 12)
                        setOnClickListener { startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(phone)}"))) }
                    }
                    row.addView(label(name, 16f, ink, true))
                    row.addView(label(phone, 13f, muted))
                    val lp = LinearLayout.LayoutParams(-1, -2)
                    lp.topMargin = 8
                    list.addView(row, lp)
                }
            }
            if (list.childCount == 0) list.addView(label("没有找到联系人"))
        }
        search.addTextChangedListener(SimpleWatcher { load(it) })
        load()
        updateDock()
    }

    private fun sectionTitle(s: String) = TextView(this).apply {
        text = s; textSize = 21f; typeface = Typeface.DEFAULT_BOLD
        setTextColor(ink); setPadding(2, 4, 0, 12)
    }
    private fun label(s: String, size: Float = 14f, color: Int = muted, bold: Boolean = false) =
        TextView(this).apply {
            text = s; textSize = size; setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    private fun rounded(color: Int, radius: Int) =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(color); cornerRadius = radius.toFloat()
        }
}

class SimpleWatcher(val changed: (String) -> Unit) : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { changed(s?.toString() ?: "") }
    override fun afterTextChanged(s: android.text.Editable?) {}
}
