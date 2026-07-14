package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ShoppingApp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppRankingScreen(
    viewModel: ShoppingViewModel,
    modifier: Modifier = Modifier
) {
    val shoppingApps by viewModel.shoppingApps.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()

    var selectedMonth by remember { mutableStateOf(Calendar.getInstance()) }

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
            AppRankingHeader(isEnglish = isEnglish)

            Spacer(modifier = Modifier.height(16.dp))

            // 월 선택 네비게이션
            MonthNavigationBar(
                selectedMonth = selectedMonth,
                onPrevious = {
                    selectedMonth = Calendar.getInstance().apply {
                        timeInMillis = selectedMonth.timeInMillis - (30 * 24 * 60 * 60 * 1000)
                    }
                },
                onNext = {
                    selectedMonth = Calendar.getInstance().apply {
                        timeInMillis = selectedMonth.timeInMillis + (30 * 24 * 60 * 60 * 1000)
                    }
                },
                isEnglish = isEnglish
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 정렬된 앱 목록
            val rankedApps = shoppingApps.sortedByDescending { it.usedMinutesToday }

            if (rankedApps.isNotEmpty()) {
                // 1위 앱 - 큰 카드
                RankingCard(
                    app = rankedApps[0],
                    rank = 1,
                    total = rankedApps.size,
                    isEnglish = isEnglish,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 2위~7위 - 그리드 (4개까지만 표시)
                if (rankedApps.size > 1) {
                    Text(
                        text = if (isEnglish) "Ranking 2-7" else "2위~7위",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rankedApps.drop(1).take(3).forEachIndexed { index, app ->
                            SmallRankingCard(
                                app = app,
                                rank = index + 2,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rankedApps.drop(4).take(3).forEachIndexed { index, app ->
                            SmallRankingCard(
                                app = app,
                                rank = index + 5,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // 빈 공간 채우기
                        repeat(maxOf(0, 3 - rankedApps.drop(4).size)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 신규 쇼핑앱 섹션
                NewAppsSection(
                    apps = rankedApps.sortedByDescending { it.id }.take(3),
                    isEnglish = isEnglish
                )
            } else {
                // 앱이 없을 때
                EmptyAppState(isEnglish = isEnglish)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun AppRankingHeader(isEnglish: Boolean) {
    Text(
        text = if (isEnglish) "App Ranking" else "앱 랭킹 순위",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1F2937)
    )
}

@Composable
private fun MonthNavigationBar(
    selectedMonth: Calendar,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    isEnglish: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = if (isEnglish) "Previous month" else "이전",
                tint = Color(0xFF64748B)
            )
        }

        Text(
            text = SimpleDateFormat(
                if (isEnglish) "MMMM yyyy" else "yyyy년 MM월",
                Locale.getDefault()
            ).format(selectedMonth.time),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1F2937),
            modifier = Modifier.width(120.dp),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = onNext,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = if (isEnglish) "Next month" else "다음",
                tint = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun RankingCard(
    app: ShoppingApp,
    rank: Int,
    total: Int,
    isEnglish: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = getAppColor(rank)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // 랭크 배지
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "👑 1${if (isEnglish) "st" else "위"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = app.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 앱 아이콘 (간단한 이모지)
                Text(
                    text = getAppEmoji(app.category),
                    fontSize = 48.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 통계 정보
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBox(
                    label = if (isEnglish) "Used Time" else "사용시간",
                    value = formatMinutes(app.usedMinutesToday),
                    backgroundColor = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.weight(1f)
                )

                StatBox(
                    label = if (isEnglish) "Sessions" else "사용빈도",
                    value = "${minOf(app.usedMinutesToday / 15 + 1, 99)}회",
                    backgroundColor = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "• ${if (isEnglish) "Category: " else "카테고리: "}${app.category}",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SmallRankingCard(
    app: ShoppingApp,
    rank: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = getAppColor(rank)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 순위 배지
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$rank",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 앱 이모지
            Text(
                text = getAppEmoji(app.category),
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 앱 이름
            Text(
                text = app.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 사용 시간
            Text(
                text = formatMinutes(app.usedMinutesToday),
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun NewAppsSection(
    apps: List<ShoppingApp>,
    isEnglish: Boolean
) {
    if (apps.isEmpty()) return

    Text(
        text = if (isEnglish) "New Apps" else "신규 쇼핑앱",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF64748B),
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )

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
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                apps.take(3).forEach { app ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                Color(0xFFF0FDF4),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = getAppEmoji(app.category),
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = app.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F2937),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (apps.size > 3) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isEnglish) "+ ${apps.size - 3} more apps" else "+ ${apps.size - 3}개 더보기",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun EmptyAppState(isEnglish: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📱",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = if (isEnglish) "No apps added yet" else "추가된 앱이 없습니다",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
            Text(
                text = if (isEnglish) "Add a shopping app to see rankings" else "쇼핑 앱을 추가하면 순위를 볼 수 있습니다",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// 헬퍼 함수들
private fun getAppColor(rank: Int): Color {
    return when (rank) {
        1 -> Color(0xFF10B981)      // 에메랄드
        2 -> Color(0xFF3B82F6)      // 블루
        3 -> Color(0xFFEF4444)      // 레드
        4 -> Color(0xFF8B5CF6)      // 퍼플
        5 -> Color(0xFFF59E0B)      // 앰버
        6 -> Color(0xFFF87171)      // 라이트 레드
        else -> Color(0xFF64748B)   // 슬레이트
    }
}

private fun getAppEmoji(category: String): String {
    return when {
        category.contains("쿠팡") || category.contains("Coupang") -> "🛍️"
        category.contains("무신사") || category.contains("패션") || category.contains("Fashion") -> "👕"
        category.contains("직구") || category.contains("International") -> "🌍"
        category.contains("음식") || category.contains("Food") -> "🍔"
        category.contains("도서") || category.contains("Book") -> "📚"
        else -> "🛒"
    }
}

private fun formatMinutes(minutes: Int): String {
    return if (minutes > 60) {
        "${minutes / 60}h ${minutes % 60}m"
    } else {
        "${minutes}m"
    }
}
