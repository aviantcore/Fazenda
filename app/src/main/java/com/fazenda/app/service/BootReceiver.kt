package com.fazenda.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fazenda.app.FazendaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val application = context.applicationContext as FazendaApplication
        val scheduleRepository = application.scheduleRepository
        val alarmManager = ScheduleAlarmManager(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                val schedules = scheduleRepository.allSchedules
                schedules.collect { list ->
                    list.filter { !it.isCompleted && it.startDate > now }.forEach { schedule ->
                        alarmManager.scheduleAlarm(schedule)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
