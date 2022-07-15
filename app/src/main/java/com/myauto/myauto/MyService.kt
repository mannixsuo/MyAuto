package com.myauto.myauto

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.myauto.myauto.coc.base.Bot
import com.myauto.myauto.core.Shell

class MyService : Service() {

    private val mBinder: AppServiceBinder = AppServiceBinder()
    private lateinit var bot: Bot

    class AppServiceBinder : Binder() {
        fun start() {
            Log.d("AppServiceBinder", "Start")
        }

        fun stop() {
            Log.d("AppServiceBinder", "Stop")
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MyService", "onCreate executed")
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "my_service", "前台Service通知",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, 0)
        val notification = NotificationCompat.Builder(this, "my_service")
            .setContentTitle("This is content title")
            .setContentText("This is content text")
            .setSmallIcon(R.drawable.ic_stat_service_notification_icon)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_stat_service_notification_icon))
            .setContentIntent(pi)
            .build()
        startForeground(1, notification)

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("MyService", "onStartCommand executed")
        val shell = Shell()
        shell.initShell("su")
        val bot = Bot(this.application)
        bot.start()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyService", "onDestroy executed")
    }
}