package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BlockedImpulse
import com.example.data.ShoppingApp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: ShoppingViewModel,
    modifier: Modifier = Modifier
) {
    val shoppingApps by viewModel.shoppingApps.collectAsStateWithLifecycle()
    val blockedImpulses by viewModel.blockedImpulses.collectAsStateWithLifecycle()
    val currentQuote by viewModel.currentQuote.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0FDF4))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 헤더
            DashboardHeader(isEnglish = isEnglish)

            Spacer(modifier = Modifier.height(16.dp))

            // 주요 통계 카드들
            DashboardStatsCards(
                shoppingApps = shoppingApps,
                blockedImpulses = blockedImpulses,
                isEnglish = isEnglish
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 시간대별 충동 차트
            TimeDistributionChart(blockedImpulses = blockedImpulses, isEnglish = isEnglish)

            Spacer(modifier = Modifier.height(20.dp))

            // 주간 차트
            WeeklyChart(blockedImpulses = blockedImpulses, isEnglish = isEnglish)

            Spacer(modifier = Modifier.height(20.dp))

            // 명언 카드
            QuoteCard(
                quote = currentQuote,
                onRefresh = { viewModel.rotateQuote() },
                isEnglish = isEnglish
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 최근 기록
            RecentRecordsSection(
                blockedImpulses = blockedImpulses.take(3),
                isEnglish = isEnglish
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun DashboardHeader(isEnglish: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (isEnglish) "Dashboard" else "대시보드",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            Text(
                text = SimpleDateFormat(
                    if (isEnglish) "MMM dd, yyyy" else "yyyy년 MM월 dd일",
                    Locale.getDefault()
                ).format(Date()),
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = if (isEnglish) "Settings" else "설정",
            tint = Color(0xFF64748B),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun DashboardStatsCards(
    shoppingApps: List<ShoppingApp>,
    blockedImpulses: List<BlockedImpulse>,
    isEnglish: Boolean
) {
    val totalSaved = blockedImpulses.filter { it.isBlocked }.sumOf { it.itemPrice }
    val successRate = if (blockedImpulses.isNotEmpty()) {
        (blockedImpulses.count { it.isBlocked } * 100) / blockedImpulses.size
    } else {
        0
    }
    val successCount = blockedImpulses.count { it.isBlocked }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 절약액 메인 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF6EE7B7)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEnglish) "Monthly Savings" else "이번 달 절약액",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF059669),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${NumberFormat.getInstance().format(totalSaved)}${if (isEnglish) " KRW" else " 원"}",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Text(
                        text = "🎯",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 성공률과 횟수 - 2단 카드
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 성공률
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEF3C7)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (isEnglish) "Success Rate" else "성공률",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF92400E),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$successRate%",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    // 진행률 바
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color(0xFFDDD6D0), RoundedCornerShape(3.dp))
                            .padding(top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(successRate / 100f)
                                .height(6.dp)
                                .background(Color(0xFF10B981), RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            // 기록 수
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE0E7FF)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isEnglish) "Records" else "기록",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4F46E5),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$successCount",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F46E5),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = if (isEnglish) "saved" else "번 참음",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeDistributionChart(
    blockedImpulses: List<BlockedImpulse>,
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
                text = if (isEnglish) "Hourly Distribution" else "시간대별 충동 분포",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 간단한 미니 차트 (라인 차트 시뮬레이션)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFFF8F7F5), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEnglish) "10h-20h peak time" else "10h~20h 피크 시간대",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEnglish) "Most impulses occur in afternoon (2-8 PM)" else "오후(14:00~20:00)에 충동이 가장 많습니다 📱",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun WeeklyChart(
    blockedImpulses: List<BlockedImpulse>,
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
                text = if (isEnglish) "Weekly Attempts" else "주간 충동 시도",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 주간 바 차트
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val days = if (isEnglish)
                    listOf("M", "T", "W", "T", "F", "S", "S")
                else
                    listOf("월", "화", "수", "목", "금", "토", "일")
                val heights = listOf(0.3f, 0.5f, 0.7f, 0.4f, 0.9f, 0.6f, 0.5f)

                days.forEachIndexed { index, day ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .fillMaxHeight(heights[index])
                                .background(
                                    Color(0xFF10B981),
                                    RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )
                        Text(
                            text = day,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuoteCard(
    quote: String,
    onRefresh: () -> Unit,
    isEnglish: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD1FAE5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "\"$quote\"",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRefresh,
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "✨ ${if (isEnglish) "New Quote" else "새 명언 보기"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun RecentRecordsSection(
    blockedImpulses: List<BlockedImpulse>,
    isEnglish: Boolean
) {
    if (blockedImpulses.isEmpty()) {
        return
    }

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
                text = if (isEnglish) "Recent Records" else "최근 기록",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            blockedImpulses.forEachIndexed { index, impulse ->
                RecentItemRow(impulse = impulse, isEnglish = isEnglish)
                if (index < blockedImpulses.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFF0EDE9)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentItemRow(
    impulse: BlockedImpulse,
    isEnglish: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = impulse.itemName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937)
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusText = if (impulse.isBlocked) {
                    if (isEnglish) "✓ Saved" else "✓ 참음"
                } else {
                    if (isEnglish) "✗ Purchased" else "✗ 구매"
                }
                val statusColor = if (impulse.isBlocked) Color(0xFF059669) else Color(0xFFB45309)
                val statusBg = if (impulse.isBlocked) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)

                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }

                Text(
                    text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(impulse.timestamp)),
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Text(
            text = "${NumberFormat.getInstance().format(impulse.itemPrice)}${if (isEnglish) "" else ""}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF10B981),
            textAlign = TextAlign.End
        )
    }
}
