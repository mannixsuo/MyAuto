package com.myauto.myauto

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.myauto.myauto.databinding.ActivityStartBinding


class StartActivity : AppCompatActivity() {
    lateinit var binding: ActivityStartBinding
    lateinit var appBinder: MyService.AppServiceBinder

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            appBinder = service as MyService.AppServiceBinder
            appBinder.start()
        }

        override fun onServiceDisconnected(name: ComponentName) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStartBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.startServiceBtn.setOnClickListener {
            val intent = Intent(this, MyService::class.java)
            startService(intent)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        binding.stopServiceBtn.setOnClickListener {
            val intent = Intent(this, MyService::class.java)
            stopService(intent)
        }
    }


}