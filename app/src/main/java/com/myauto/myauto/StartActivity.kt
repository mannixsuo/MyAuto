package com.myauto.myauto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.myauto.myauto.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {
    lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStartBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.startServiceBtn.setOnClickListener {
            val intent = Intent(this, MyService::class.java)
            startService(intent)
        }

        binding.stopServiceBtn.setOnClickListener {
            val intent = Intent(this, MyService::class.java)
            stopService(intent)
        }
    }


}