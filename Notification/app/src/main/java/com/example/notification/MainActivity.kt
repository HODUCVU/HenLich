package com.example.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.text.SimpleDateFormat
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private lateinit var notification : Button
    private lateinit var clock : TextView
    private lateinit var clockNotification : EditText
    private lateinit var runTimer : Button
    private lateinit var currentTime : String
    private var running = false

    private var time = 0.0
    private lateinit var serviceIntent : Intent

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("My Notification","My Notification",NotificationManager.IMPORTANCE_DEFAULT)
            val manager  = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        notification = findViewById(R.id.btnNotification)
        runTimer = findViewById(R.id.btnTimer)
        clock = findViewById(R.id.txtClock)
        clockNotification = findViewById(R.id.editTime)

        //Timer
        serviceIntent = Intent(applicationContext,TimerService::class.java)
        registerReceiver(UPDATE_TIME, IntentFilter(TimerService.TIME_UPDATED))
        runTimer.setOnClickListener {
            if(!running) {
                startTimer()
                runTimer.text = "Stop"
                running = true
            } else {
                stopTimer()
                runTimer.text = "Start"
                running = false
            }
        }

        notification.setOnClickListener {
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            Log.e("current Time 0", currentTime)

            val builder : NotificationCompat.Builder = NotificationCompat.Builder(this,"My Notification")
            builder.setContentTitle("Title")
                .setContentText("Hello World")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                //to show content in lock screen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            //Âm thanh lên khi  có thông báo
            val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(alarmSound)
                .setAutoCancel(true)
            //Rung khi có thông báo
            val notification = builder.build()
            notification.defaults = Notification.DEFAULT_LIGHTS
            notification.defaults = Notification.DEFAULT_VIBRATE
            val managerCompat = NotificationManagerCompat.from(this)
            managerCompat.notify(1,notification)
        }

    }

    private fun stopTimer() {
        stopService(serviceIntent)
    }
    private fun startTimer() {
        serviceIntent.putExtra(TimerService.TIME_EXTRA,time)
        //todo
        val timeNotification : String = clockNotification.text.toString()
        if(timeNotification.isNotEmpty())
            serviceIntent.putExtra(TimerService.TIME_NOTIFICATION,timeNotification)
        else
            serviceIntent.putExtra(TimerService.TIME_NOTIFICATION,currentTime)
        startService(serviceIntent)
    }

    private val UPDATE_TIME : BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            time = intent.getDoubleExtra(TimerService.TIME_EXTRA,0.0)
            clock.text = getTimeString(time) 
        }
    }

    private fun getTimeString(time: Double): String {
        val result = time.roundToInt()
        val hours = result % 86400 / 3600
        val minus = result % 86400 % 3600 / 60
        val seconds = result % 86400 % 3600 % 60
        return String.format("%02d:%02d:%02d",hours,minus,seconds)
    }
}