package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.ui.SaveClipboardActivity

class FloatingBubbleService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID_BUBBLE, createNotification())
        createFloatingBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun createFloatingBubble() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val windowLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        windowLayoutParams.gravity = Gravity.TOP or Gravity.START
        windowLayoutParams.x = 0
        windowLayoutParams.y = 200

        val wrapperSize = (72 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(this)
        
        val bubbleSize = (52 * resources.displayMetrics.density).toInt()
        val bubbleLayout = FrameLayout(this).apply {
            this.layoutParams = FrameLayout.LayoutParams(bubbleSize, bubbleSize).apply {
                gravity = Gravity.CENTER
            }
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(Color.parseColor("#FFFFFF")) // Clean white
                setStroke((1f * resources.displayMetrics.density).toInt(), Color.parseColor("#EADDFF"))
            }
            elevation = 16f * resources.displayMetrics.density
            outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
            clipToOutline = true
        }

        val textView = android.widget.TextView(this).apply {
            text = "📋"
            textSize = 24f
            gravity = Gravity.CENTER
            this.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        bubbleLayout.addView(textView)
        container.addView(bubbleLayout)
        
        floatingView = container

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        floatingView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = windowLayoutParams.x
                    initialY = windowLayoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        saveClipboard()
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dX = event.rawX - initialTouchX
                    val dY = event.rawY - initialTouchY
                    if (Math.abs(dX) > 10 || Math.abs(dY) > 10) {
                        isClick = false
                    }
                    windowLayoutParams.x = initialX + dX.toInt()
                    windowLayoutParams.y = initialY + dY.toInt()
                    windowManager.updateViewLayout(floatingView, windowLayoutParams)
                    true
                }
                else -> false
            }
        }

        windowManager.addView(floatingView, windowLayoutParams)
    }

    private fun saveClipboard() {
        val intent = Intent(this, SaveClipboardActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        val channelId = "floating_bubble_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Floating Bubble",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Clipboard Floating Bubble")
            .setContentText("Tap to save clipboard content")
            .setSmallIcon(android.R.drawable.ic_menu_add)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID_BUBBLE = 2
    }
}
