package com.fazenda.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.fazenda.app.FazendaApplication
import com.fazenda.app.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduleNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra("scheduleId", -1L)
        if (scheduleId == -1L) return

        // Handle notification actions
        when (intent.action) {
            "com.fazenda.app.ACTION_COMPLETE" -> {
                handleNotificationAction(context, scheduleId)
                return
            }
            "com.fazenda.app.ACTION_SNOOZE" -> {
                handleSnooze(context, scheduleId)
                return
            }
        }

        val pendingResult = goAsync()
        val application = context.applicationContext as FazendaApplication
        val scheduleRepository = application.scheduleRepository
        val categoryRepository = application.categoryRepository

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val schedule = scheduleRepository.getScheduleById(scheduleId)
                if (schedule != null && !schedule.isCompleted) {
                    val category = schedule.categoryId?.let { categoryRepository.getCategoryById(it) }
                    val categoryName = category?.name ?: "Всі рослини"
                    
                    showNotification(
                        context = context,
                        schedule = schedule,
                        title = "Заплановано догляд: $categoryName",
                        message = "${schedule.phaseTime}: ${schedule.recipe}"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(
        context: Context,
        schedule: com.fazenda.app.data.entity.ScheduleEntity,
        title: String,
        message: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "schedule_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Нагадування про догляд",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Нагадування про планові обробки рослин"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("scheduleId", schedule.id)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            schedule.id.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Виконано" action
        val completeIntent = Intent(context, ScheduleNotificationReceiver::class.java).apply {
            action = "com.fazenda.app.ACTION_COMPLETE"
            putExtra("scheduleId", schedule.id)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            (schedule.id * 10).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Відкласти на 15 хвилин" action
        val snoozeIntent = Intent(context, ScheduleNotificationReceiver::class.java).apply {
            action = "com.fazenda.app.ACTION_SNOOZE"
            putExtra("scheduleId", schedule.id)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (schedule.id * 10 + 1).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_check,
                "Виконано",
                completePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_recent_history,
                "Відкласти 15 хв",
                snoozePendingIntent
            )
            .build()

        notificationManager.notify(schedule.id.toInt(), notification)
    }

    companion object {
        fun handleNotificationAction(context: Context, scheduleId: Long) {
            val application = context.applicationContext as FazendaApplication
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val schedule = application.scheduleRepository.getScheduleById(scheduleId)
                    if (schedule != null) {
                        application.scheduleRepository.markAsCompleted(scheduleId)
                        
                        // Автоматичний запис у журнал
                        val log = com.fazenda.app.data.entity.LogEntity(
                            date = System.currentTimeMillis(),
                            plantId = null,
                            zoneId = null,
                            categoryId = schedule.categoryId,
                            actionType = com.fazenda.app.data.entity.LogActionTypes.SPRAYING,
                            comment = "Автоматичний запис: виконано план обробки.\nФаза: ${schedule.phaseTime}\nРецепт: ${schedule.recipe}"
                        )
                        application.logRepository.insertLog(log)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun handleSnooze(context: Context, scheduleId: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ScheduleNotificationReceiver::class.java).apply {
                putExtra("scheduleId", scheduleId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduleId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            try {
                val snoozeTime = System.currentTimeMillis() + 15 * 60 * 1000L // 15 хвилин
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        snoozeTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        snoozeTime,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 15 * 60 * 1000L,
                    pendingIntent
                )
            }
        }
    }
}
