package com.onthego.onthegovisitation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FireBaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        showNotification(
            remoteMessage.notification?.title.toString(),
            remoteMessage.notification?.body.toString()
        )
    }

    private fun showNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(this, "MyNotification")
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentText(message)
            .addAction(R.drawable.ic_assign_person_24dp, "Read", null)
            .addAction(R.drawable.ic_assign_person_24dp, "Dismiss", null)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "MyNotification",
                "MyNotification",
                NotificationManager.IMPORTANCE_HIGH
            )
            serviceChannel.enableLights(true)
            serviceChannel.enableVibration(true)
            manager.createNotificationChannel(serviceChannel)
        }
        manager.notify(99, builder.build())
    }
}