package com.example.proyectofinal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class DndService : Service() {
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1) Crea canal para el servicio foreground (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                "dnd_service_channel",
                "Servicio No Molestar",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(chan)
        }

        // 2) Construye la notificación obligatoria
        val notification: Notification = NotificationCompat.Builder(this, "dnd_service_channel")
            .setContentTitle("Modo No Molestar activo")
            .setContentText("Tu dispositivo está en modo No Molestar mientras la app esté abierta.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // tu icono
            .setOngoing(true)
            // Opcional: al tocar te lleve a la app
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        // 3) Inicia el servicio en primer plano
        startForeground(1, notification)

        // 4) Activa DND
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cuando el servicio se detiene, desactiva DND
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }

    // No binding
    override fun onBind(intent: Intent?): IBinder? = null
}
