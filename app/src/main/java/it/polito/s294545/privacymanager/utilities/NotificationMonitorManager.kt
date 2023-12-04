package it.polito.s294545.privacymanager.utilities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import it.polito.s294545.privacymanager.R

class NotificationMonitorManager : NotificationListenerService() {
    private val NOTIFICATION_ID = 2
    private lateinit var notificationManager: NotificationManager
    private val channelId = "NotificationMonitorServiceChannel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground(NOTIFICATION_ID, createNotification()) // Start as a foreground service
        return START_STICKY
    }

    private fun createNotification(): Notification {
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(
            channelId,
            "Notification Monitor Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(notificationChannel)

        return Notification.Builder(this, channelId)
            .setContentTitle("Monitoring Service")
            .setContentText("Monitoring notifications...")
            .setSmallIcon(R.drawable.icon_app) // Replace with your own icon
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Get the notification object
        val notification = sbn.notification
        // Get the notification title
        val title = notification.extras.getString(Notification.EXTRA_TITLE)
        // Get the notification text
        val text = notification.extras.getString(Notification.EXTRA_TEXT)
        // Do something with the notification data
        Log.d("myapp", sbn.packageName)

        //cancelNotification(sbn.key)

        /*
        val intent = Intent("android.service.notification.NotificationListenerService")
        intent.putExtra("Notification Title", notification)
        sendBroadcast(intent)
         */
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Do something when the notification is removed
    }

    fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE) // Stop as a foreground service
        stopSelf()
    }
}