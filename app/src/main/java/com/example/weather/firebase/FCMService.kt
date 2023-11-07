package com.example.weather.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.weather.MainActivity
import com.example.weather.R
import com.example.weather.model.Weather
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import java.util.Random


class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: " + remoteMessage.data)

            val gson = Gson()
            val weather = gson.fromJson(remoteMessage.data["weather"], Weather::class.java)
            Log.d("notification_weather_description",weather?.description.toString())
            if (weather?.description != null && weather.description!!.toLowerCase().contains("rain")) {
                sendRainNotification()
            }
        }

    }


    private fun sendRainNotification() {
        try {

            var notificationsManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = Notification.Builder(this)
            builder.setAutoCancel(true)
            builder.setTicker(System.currentTimeMillis().toString())
            builder.setPriority(Notification.PRIORITY_HIGH)
            builder.setDefaults(Notification.DEFAULT_VIBRATE)

            val channel = NotificationChannel(
                resources.getString(R.string.default_notification_channel_id),
                resources.getString(R.string.default_notification_channel_id),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationsManager.createNotificationChannel(channel)
            builder.setChannelId(resources.getString(R.string.default_notification_channel_id))

            builder.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)

            builder.setContentTitle("Rain Alert")
            builder.setContentText("It may rain in your location today.")
            builder.setSmallIcon(R.drawable.elevend)
            builder.setColor(ContextCompat.getColor(this, R.color.colorAccent))

            val intent = Intent(applicationContext, MainActivity::class.java)
            var pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            builder.setContentIntent(pendingIntent)
            builder.setOngoing(false)
            builder.setShowWhen(true)
            builder.build()
            var myNotification = builder.notification
            val random = Random()
            notificationsManager?.notify(random.nextInt(), myNotification)
        }catch (e: Exception) {
          Log.d("catch_exception","Something went wrong")
        }
    }
    override fun onNewToken(token: String) {
    }
}
