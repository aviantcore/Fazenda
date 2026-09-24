package com.fazenda.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fazenda.app.data.entity.ScheduleEntity

enum class ScheduleStatus {
    COMPLETED, OVERDUE, ACTIVE
}

fun ScheduleEntity.getStatus(): ScheduleStatus {
    val now = System.currentTimeMillis()
    return when {
        isCompleted -> ScheduleStatus.COMPLETED
        endDate > 0 && endDate < now -> ScheduleStatus.OVERDUE
        else -> ScheduleStatus.ACTIVE
    }
}

private data class StatusStyle(
    val icon: ImageVector,
    val label: String,
    val backgroundColor: Color,
    val textColor: Color
)

@Composable
fun ScheduleStatusBadge(
    schedule: ScheduleEntity,
    modifier: Modifier = Modifier
) {
    val status = schedule.getStatus()
    
    val style = when (status) {
        ScheduleStatus.COMPLETED -> StatusStyle(
            icon = Icons.Default.Check,
            label = "Виконано",
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        ScheduleStatus.OVERDUE -> StatusStyle(
            icon = Icons.Default.Warning,
            label = "Прострочено",
            backgroundColor = MaterialTheme.colorScheme.errorContainer,
            textColor = MaterialTheme.colorScheme.onErrorContainer
        )
        ScheduleStatus.ACTIVE -> StatusStyle(
            icon = Icons.Default.Schedule,
            label = "Активний",
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
    
    Row(
        modifier = modifier
            .background(
                color = style.backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = style.textColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = style.label,
            color = style.textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}
