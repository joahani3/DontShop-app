package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.util.*

@Composable
fun SettingsScreen(
    viewModel: ShoppingViewModel,
    modifier: Modifier = Modifier
) {
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val nightAlertLimit by viewModel.nightAlertLimit.collectAsStateWithLifecycle()

    var localBudget by remember(monthlyBudget) { mutableStateOf(monthlyBudget.toFloat()) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFEF7F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 헤더
            Text(
                text = if (isEnglish) "Settings" else "설정",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 예산 섹션
            BudgetSection(
                budget = localBudget.toLong(),
                onBudgetChange = { localBudget = it.toFloat() },
                onSave = { viewModel.setMonthlyBudget(localBudget.toLong()) },
                isEnglish = isEnglish
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 알림 섹션
            NotificationSection(
                nightAlertEnabled = nightAlertLimit,
                onNightAlertChange = { viewModel.toggleNightAlertLimit() },
                isEnglish = isEnglish
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 언어 섹션
            LanguageSection(
                isEnglish = isEnglish,
                isEnglish_state = isEnglish,
                isEnglish_onChange = { /* 언어 변경 로직 */ },
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 테마 섹션
            ThemeSection(isEnglish = isEnglish)

            Spacer(modifier = Modifier.height(20.dp))

            // 데이터 관리 섹션
            DataManagementSection(
                onResetStatistics = { viewModel.resetStatistics() },
                isEnglish = isEnglish
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun BudgetSection(
    budget: Long,
    onBudgetChange: (Long) -> Unit,
    onSave: () -> Unit,
    isEnglish: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (isEnglish) "Monthly Budget" else "월간 예산",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 예산 표시
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0FDF4), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "₩ ${NumberFormat.getInstance().format(budget)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = if (isEnglish) "per month" else "/ 월",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 슬라이더
            Slider(
                value = budget.toFloat() / 1000,
                onValueChange = { onBudgetChange((it * 1000).toLong()) },
                valueRange = 100f..10000f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF10B981),
                    activeTrackColor = Color(0xFF10B981),
                    inactiveTrackColor = Color(0xFFE5E7EB)
                ),
                steps = 89
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "100K",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "10M",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isEnglish) "Save Budget" else "예산 저장",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun NotificationSection(
    nightAlertEnabled: Boolean,
    onNightAlertChange: () -> Unit,
    isEnglish: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (isEnglish) "Notifications" else "알림 설정",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 야간 알림 제한
            SettingToggleItem(
                title = if (isEnglish) "Limit night alerts" else "야간 알림 제한",
                subtitle = if (isEnglish) "No alerts between 10 PM - 8 AM" else "밤 10시~아침 8시 알림 없음",
                isEnabled = nightAlertEnabled,
                onToggle = onNightAlertChange,
                icon = "🌙"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color(0xFFF0EDE9))

            Spacer(modifier = Modifier.height(12.dp))

            // 예산 초과 알림
            SettingTextItem(
                title = if (isEnglish) "Budget exceeded alert" else "예산 초과 시 알림",
                current = if (isEnglish) "On purchase attempt" else "구매 시도 시",
                icon = "💰"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color(0xFFF0EDE9))

            Spacer(modifier = Modifier.height(12.dp))

            // 시간 제한 알림
            SettingTextItem(
                title = if (isEnglish) "Time limit alert" else "시간 제한 알림",
                current = if (isEnglish) "When reaching 80%" else "80% 도달 시",
                icon = "⏰"
            )
        }
    }
}

@Composable
private fun LanguageSection(
    isEnglish: Boolean,
    isEnglish_state: Boolean,
    isEnglish_onChange: (Boolean) -> Unit,
    viewModel: ShoppingViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (isEnglish) "Language" else "언어",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 한국어
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = !isEnglish_state,
                    onClick = { },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF10B981),
                        unselectedColor = Color(0xFFCBD5E1)
                    )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = "한국어 (Korean)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "시스템 언어: 한국어",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 영어
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isEnglish_state,
                    onClick = { },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF10B981),
                        unselectedColor = Color(0xFFCBD5E1)
                    )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = "English",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "System language: English",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEnglish) "* Restart the app for language change to take effect" else "* 언어 변경 후 앱을 다시 시작하면 반영됩니다",
                fontSize = 10.sp,
                color = Color(0xFFF59E0B),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
private fun ThemeSection(isEnglish: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (isEnglish) "Theme" else "테마",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            val themes = listOf(
                Triple("Pastel", "🌸", Color(0xFF6EE7B7)),
                Triple("Cyberpunk", "⚡", Color(0xFFFF00FF)),
                Triple("Sage", "🍃", Color(0xFF10B981)),
                Triple("Yellow", "⭐", Color(0xFFFCD34D)),
                Triple("Sky", "☁️", Color(0xFF38BDF8))
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themes.forEach { (name, emoji, color) ->
                    ThemeOption(
                        name = name,
                        emoji = emoji,
                        color = color,
                        isSelected = name == "Pastel"
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    name: String,
    emoji: String,
    color: Color,
    isSelected: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.1f))
            .clickable { }
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = emoji,
                    fontSize = 18.sp
                )

                Text(
                    text = name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Selected",
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DataManagementSection(
    onResetStatistics: () -> Unit,
    isEnglish: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (isEnglish) "Data Management" else "데이터 관리",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isEnglish) "🗑️ Clear All Records" else "🗑️ 모든 기록 초기화",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEnglish) "This action cannot be undone" else "이 작업은 되돌릴 수 없습니다",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = if (isEnglish) "Clear all records?" else "모든 기록을 초기화하시겠어요?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEnglish)
                        "All your saved records and statistics will be permanently deleted. This cannot be undone."
                    else
                        "저장된 모든 기록과 통계가 영구적으로 삭제됩니다. 되돌릴 수 없습니다.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetStatistics()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    )
                ) {
                    Text(
                        text = if (isEnglish) "Delete" else "삭제",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(
                        text = if (isEnglish) "Cancel" else "취소",
                        color = Color(0xFF10B981)
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SettingToggleItem(
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    icon: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 18.sp)

            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF10B981),
                checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.3f)
            ),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun SettingTextItem(
    title: String,
    current: String,
    icon: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 18.sp)

            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = current,
                    fontSize = 10.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Text(
            text = ">",
            fontSize = 16.sp,
            color = Color(0xFFCBD5E1)
        )
    }
}
