package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.viewmodel.ScheduleViewModel

@Composable
fun HorarioScreen(viewModel: ScheduleViewModel = viewModel()) {
    val schedules by viewModel.schedules.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8E9))
            .padding(16.dp)
    ) {
        Text(
            text = "📅 Horário Semanal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Hora", "Seg", "Ter", "Qua", "Qui", "Sex").forEach { dia ->
                Text(
                    text = dia,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF2E7D32)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        schedules.forEach { schedule ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                listOf(
                    schedule.timeSlot,
                    schedule.monday,
                    schedule.tuesday,
                    schedule.wednesday,
                    schedule.thursday,
                    schedule.friday
                ).forEach { item ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .border(1.dp, Color(0xFF81C784), RoundedCornerShape(4.dp))
                            .background(Color.White)
                    ) {
                        Text(
                            text = item, 
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }
        
        if (schedules.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum horário disponível",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
