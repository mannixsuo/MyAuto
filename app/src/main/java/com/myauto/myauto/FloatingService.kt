package com.myauto.myauto

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Button
import androidx.annotation.RequiresApi


class FloatingService : Service() {
    private var x: Int = 0
    private var y: Int = 0

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            // 获取WindowManager服务
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

            // 新建悬浮窗控件
            val button = Button(applicationContext)
            button.text = "Floating"
            button.setBackgroundColor(Color.BLUE)

            // 设置LayoutParam
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
            layoutParams.format = PixelFormat.RGBA_8888
            layoutParams.width = 100
            layoutParams.height = 100
            layoutParams.x = 100
            layoutParams.y = 100

            // 将悬浮窗控件添加到WindowManager
            windowManager.addView(button, layoutParams)

            button.setOnTouchListener { view, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = motionEvent.rawX.toInt()
                        y = motionEvent.rawY.toInt()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val nowX = motionEvent.rawX.toInt()
                        val nowY = motionEvent.rawY.toInt()
                        val movedX = nowX - x
                        val movedY = nowY - y
                        x = nowX
                        y = nowY
                        layoutParams.x = layoutParams.x + movedX
                        layoutParams.y = layoutParams.y + movedY
                        windowManager.updateViewLayout(view, layoutParams)
                    }
                }
                false
            }
        }
    }
}