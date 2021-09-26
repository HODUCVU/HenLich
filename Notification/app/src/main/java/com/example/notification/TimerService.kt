package com.example.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*

class TimerService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val timer = Timer()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val time = intent.getDoubleExtra(TIME_EXTRA,0.0)
        val timeNotification = intent.getStringExtra(TIME_NOTIFICATION)
        timer.scheduleAtFixedRate(TimerTask(time, timeNotification!!),0,1000)
        return START_NOT_STICKY
    }
    inner class TimerTask(private var time : Double,private val timeNotification : String) : java.util.TimerTask() {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun run() {
            val intent = Intent(TIME_UPDATED)
            //get current time
            val currentTime = SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(Date())
            time++
            notification(time,timeNotification,currentTime)
            intent.putExtra(TIME_EXTRA,time)
            sendBroadcast(intent)
        }
    }
    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }
    companion object {
        const val TIME_UPDATED = "timerUpdated"
        const val TIME_EXTRA = "timeExtra"
        const val TIME_NOTIFICATION = "timeNotification"
    }

    private fun notification(time : Double,timeNotification : String,currentTime : String) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("My Notification","My Notification",
                NotificationManager.IMPORTANCE_DEFAULT)
            val manager  = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
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
//        if(time.toInt() % timeNotification.toInt() == 0)
//            managerCompat.notify(1,notification)
        if(currentTime == timeNotification) //
            managerCompat.notify(1,notification)
    }

}