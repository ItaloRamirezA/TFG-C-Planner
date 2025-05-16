package com.app.cplanner.session

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.app.cplanner.BaseActivity.Companion.showToast
import com.app.cplanner.R
import com.app.cplanner.activities.TareasListActivity

/**
 * Clase NotificacionesActivity
 *
 * Esta clase sirve para crear notificaciones y programarlas, principalmente
 * para las tareas que agregues.
 */
class LocalNotification(private val context: Context) {
    companion object {
        const val MY_CHANNEL_ID = "MyChannel"
    }

    // Para ejecutar la notificación en un futuro
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Crear canal de notificación si no existe
     * Para móviles con Android 8.0 (API 26) o superior
     */
    private fun crearCanalNotificacion() {
        val nombreCanal = "Canal de notificaciones"
        val descripcionCanal = "Este es el canal de notificaciones"
        val importancia = NotificationManager.IMPORTANCE_DEFAULT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(MY_CHANNEL_ID, nombreCanal, importancia).apply {
                description = descripcionCanal
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(canal)
        }
    }

    /**
     * Crear una notificación y lanzarla cuando se llame al método.
     *
     * @param titulo Título que queremos que tenga la notificación
     * @param texto Texto de la notificación
     * @param imagen Imagen de la notificación (si se pone 0, la imagen sera la del logo de la aplicación)
     */
    private fun crearNotificacion(titulo: String, texto: String, imagen: Int) {
        crearCanalNotificacion()

        // Si la imagen es 0, se pone la de la aplicacion por default
        var imagenFinal = imagen
        if (imagen == 0) { imagenFinal = R.mipmap.logo_cplanner_foreground }

        // Intent para abrir las tareas cuando se toque la notificación
        val intent = Intent(context, TareasListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, MY_CHANNEL_ID)
            .setSmallIcon(imagenFinal) // Logo de la aplicación
            .setContentTitle(titulo) // Título
            .setContentText(texto) // Texto
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridad alta
            .setAutoCancel(true) // Elimina la notificación al hacer clic
            .setContentIntent(pendingIntent) // Abre la actividad al hacer clic

        // Obtener NotificationManager para lanzarla
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }

    /**
     * Programar una notificación para una fecha y hora específica.
     *
     * @param titulo Título que queremos que tenga la notificación
     * @param texto Texto de la notificación
     * @param imagen Imagen de la notificación (si se pone 0, la imagen sera la del logo de la aplicación)
     *
     * @param anio El año de la notificación
     * @param mes El mes de la notificación
     * @param dia El día de la notificación
     * @param hora La hora de la notificación en formato de 24 horas
     * @param minuto El minuto de la notificación
     */
    fun programarNotificacion(titulo: String, texto: String, imagen: Int, anio: Int, mes: Int, dia: Int, hora: Int, minuto: Int) {
        if (context is Activity) {
            showToast(context, "Programando notificación para: $anio/$mes/$dia $hora:$minuto")
        } else {
            throw IllegalArgumentException("El contexto proporcionado no es una instancia de Activity")
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, anio)
            set(Calendar.MONTH, mes - 1) // Meses van de 0 a 11
            set(Calendar.DAY_OF_MONTH, dia)
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()
        if (delay > 0) {
            handler.postDelayed({
                crearNotificacion(titulo, texto, imagen)
            }, delay)
        } else {
            showToast(context, "La fecha y hora ya pasaron. No se lanzará la notificación.")
        }
    }
}