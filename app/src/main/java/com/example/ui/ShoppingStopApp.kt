package com.example.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BlockedImpulse
import com.example.data.ShoppingApp
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.workez365.stopshop.R
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingStopApp(
    viewModel: ShoppingViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val shoppingApps by viewModel.shoppingApps.collectAsStateWithLifecycle()
    val blockedImpulses by viewModel.blockedImpulses.collectAsStateWithLifecycle()
    val warningMessage by viewModel.warningMessage.collectAsStateWithLifecycle()
    val activeApp by viewModel.activeApp.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()

    // 팝업/모달 컨트롤
    val showBrakeDialog by viewModel.showBrakeDialog.collectAsStateWithLifecycle()
    val showResultDialog by viewModel.showResultDialog.collectAsStateWithLifecycle()

    // 테마 변경 실시간 미리보기 및 선택을 위한 상태
    var currentTheme by remember { mutableStateOf(ThemeStyle.PASTEL) }
    var showThemeSelector by remember { mutableStateOf(false) }

    val activeColors = when (currentTheme) {
        ThemeStyle.PASTEL -> PastelThemeColors
        ThemeStyle.CYBERPUNK -> CyberpunkThemeColors
        ThemeStyle.SAGE -> SageThemeColors
        ThemeStyle.YELLOW -> YellowThemeColors
        ThemeStyle.SKY -> SkyThemeColors
    }

    CompositionLocalProvider(LocalCustomColors provides activeColors) {
        val colors = LocalCustomColors.current
        val AppleBgLight = colors.appleBgLight
        val AppleCardLight = colors.appleCardLight
        val AppleTextDark = colors.appleTextDark
        val AppleTextGrey = colors.appleTextGrey
        val PastelBlue = colors.pastelBlue
        val PastelPeach = colors.pastelPeach
        val PastelMint = colors.pastelMint
        val PastelYellow = colors.pastelYellow
        val PastelRed = colors.pastelRed
        val Indigo50 = colors.bgGradientStart
        val Rose50 = colors.bgGradientEnd
        val Slate100 = colors.slate100
        val Slate200 = colors.slate200

        val coroutineScope = rememberCoroutineScope()
        val pagerState = rememberPagerState(initialPage = currentTab) { 3 }

        // Sync ViewModel currentTab -> PagerState
        LaunchedEffect(currentTab) {
            if (pagerState.currentPage != currentTab) {
                pagerState.animateScrollToPage(currentTab)
            }
        }

        // Sync PagerState -> ViewModel currentTab
        LaunchedEffect(pagerState.currentPage) {
            if (pagerState.currentPage != currentTab) {
                viewModel.selectTab(pagerState.currentPage)
            }
        }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = AppleBgLight,
            bottomBar = {
                // 아이폰 스타일의 둥근 플로팅 하단 탭바
                IPhoneBottomNavigationBar(
                    currentTab = currentTab,
                    isEnglish = isEnglish,
                    onTabSelected = { tab ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(tab)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // 상단 에러/알림 배너
                    warningMessage?.let { msg ->
                        WarningBanner(
                            message = msg,
                            onDismiss = { viewModel.clearWarning() }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 현재 탭 뷰 전환 (좌우 스와이프 슬라이드 지원)
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { page ->
                        when (page) {
                            0 -> DashboardScreen(
                                viewModel = viewModel,
                                shoppingApps = shoppingApps,
                                blockedImpulses = blockedImpulses,
                                onNavigateToTab = { targetTab ->
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(targetTab)
                                    }
                                }
                            )
                            1 -> StatisticsScreen(
                                viewModel = viewModel,
                                blockedImpulses = blockedImpulses
                            )
                            2 -> SettingsScreen(
                                viewModel = viewModel,
                                shoppingApps = shoppingApps,
                                onThemeSelectorClick = { showThemeSelector = true }
                            )
                        }
                    }
                }

                // === 팝업창 1: 결제 직전 브레이크 작동 다이얼로그 ===
                if (showBrakeDialog) {
                    BrakeSimulationDialog(viewModel = viewModel)
                }

                // === 팝업창 2: 충동구매 참기 성공/실패 결과 애니메이션 피드백 ===
                showResultDialog?.let { isSuccess ->
                    ResultFeedbackDialog(
                        isSuccess = isSuccess,
                        onDismiss = { viewModel.closeResultDialog() }
                    )
                }

                // === 팝업창 3: 트렌디 디자인 테마 선택 및 실시간 미리보기 모달 ===
                if (showThemeSelector) {
                    ThemeSelectorDialog(
                        currentTheme = currentTheme,
                        onThemeSelected = { currentTheme = it },
                        onDismiss = { showThemeSelector = false }
                    )
                }

                // === 팝업창 4: 실시간 쇼핑 세션 감시 오버레이 ===
                activeApp?.let { app ->
                    ActiveShoppingSessionDialog(
                        app = app,
                        onAddMinute = { viewModel.addSessionMinuteSimulated() },
                        onStop = { viewModel.stopShoppingAppSession() }
                    )
                }
            }
        }
    }
}

// === 공통 UI: 아이폰 스타일 헤더 ===
@Composable
fun IPhoneHeader(onSettingsClick: () -> Unit) {
    val colors = LocalCustomColors.current
    val AppleBgLight = colors.appleBgLight
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelBlue = colors.pastelBlue
    val PastelPeach = colors.pastelPeach
    val PastelMint = colors.pastelMint
    val PastelYellow = colors.pastelYellow
    val PastelRed = colors.pastelRed
    val Indigo50 = colors.bgGradientStart
    val Rose50 = colors.bgGradientEnd
    val Slate100 = colors.slate100
    val Slate200 = colors.slate200

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PastelBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Logo Icon",
                        tint = PastelBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "쇼핑그만",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextDark,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "스마트 충동구매 브레이커",
                        fontSize = 10.sp,
                        color = AppleTextGrey,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 우측: 아이폰 스타일 설정 원형 버튼 (클릭 시 테마 메뉴 팝업)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Slate100)
                    .clickable { onSettingsClick() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = AppleTextGrey,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Slate100)
        )
    }
}

// === 공통 UI: 시스템 알림/경고 배너 ===
@Composable
fun WarningBanner(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = PastelRed),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Warning",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// === 공통 UI: 아이폰 스타일의 슬릭한 하단 네비게이션 ===
@Composable
fun IPhoneBottomNavigationBar(
    currentTab: Int,
    isEnglish: Boolean,
    onTabSelected: (Int) -> Unit
) {
    val colors = LocalCustomColors.current
    val AppleCardLight = colors.appleCardLight
    val AppleTextGrey = colors.appleTextGrey
    val PastelRed = colors.pastelRed
    val PastelBlue = colors.pastelBlue

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .navigationBarsPadding(), // 시스템 하단 바 겹침 해결
        shape = RoundedCornerShape(24.dp),
        color = AppleCardLight,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple(Localization.t("tab_dashboard", isEnglish), Icons.Outlined.Home, Icons.Filled.Home),
                Triple(Localization.t("tab_apps", isEnglish), Icons.Outlined.BarChart, Icons.Filled.BarChart),
                Triple(Localization.t("tab_settings", isEnglish), Icons.Outlined.Settings, Icons.Filled.Settings)
            )

            tabs.forEachIndexed { index, (label, outlinedIcon, filledIcon) ->
                val isSelected = currentTab == index
                // 활성화된 탭은 이미지처럼 붉은/코랄 계열(여기선 PastelRed를 활성화 컬러로) 또는 PastelRed 테마에 맞춤
                val activeTabColor = PastelRed
                val tabColor = if (isSelected) activeTabColor else AppleTextGrey

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 6.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isSelected) filledIcon else outlinedIcon,
                        contentDescription = label,
                        tint = tabColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = tabColor
                    )
                }
            }
        }
    }
}

// === 공통 UI: 원형 제한시간 그라데이션 대시보드 ===
@Composable
fun DailyLimitDashboard(
    totalRemaining: Int,
    totalLimit: Int,
    totalSavedMoney: Int,
    totalSpentMoney: Int
) {
    val colors = LocalCustomColors.current
    val AppleBgLight = colors.appleBgLight
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelBlue = colors.pastelBlue
    val PastelPeach = colors.pastelPeach
    val PastelMint = colors.pastelMint
    val PastelYellow = colors.pastelYellow
    val PastelRed = colors.pastelRed
    val Indigo50 = colors.bgGradientStart
    val Rose50 = colors.bgGradientEnd
    val Slate100 = colors.slate100
    val Slate200 = colors.slate200

    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.KOREA) }
    val progress = if (totalLimit > 0) {
        (totalRemaining.toFloat() / totalLimit.toFloat()).coerceIn(0f, 1f)
    } else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Indigo50, Rose50)
                    )
                )
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "오늘의 남은 쇼핑 시간",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppleTextGrey,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 원형 진행률 링
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(96.dp)
            ) {
                // Background Track Circle
                CircularProgressIndicator(
                    progress = 1f,
                    color = Color.White,
                    strokeWidth = 6.dp,
                    modifier = Modifier.fillMaxSize()
                )
                // Active Indigo Progress Ring
                CircularProgressIndicator(
                    progress = progress,
                    color = PastelBlue, // Indigo-500
                    strokeWidth = 6.dp,
                    modifier = Modifier.fillMaxSize()
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$totalRemaining",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = AppleTextDark
                    )
                    Text(
                        text = "MINUTES LEFT",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 하단 보조 정보 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "총 제한 시간", fontSize = 10.sp, color = AppleTextGrey)
                    Text(text = "${totalLimit}분", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppleTextDark)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.width(1.dp).height(16.dp).background(Slate200))
                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "오늘 아낀 금액", fontSize = 10.sp, color = AppleTextGrey)
                    Text(
                        text = formatter.format(totalSavedMoney), 
                        fontSize = 13.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = PastelMint // Emerald-500
                    )
                }
            }
        }
    }
}

data class WeeklyBarData(val label: String, val minutes: Int)

@Composable
fun WeeklyUsageChart(
    weeklyData: List<WeeklyBarData>
) {
    val colors = LocalCustomColors.current
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelBlue = colors.pastelBlue
    val Slate100 = colors.slate100
    val Slate200 = colors.slate200

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = AppleCardLight),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📅 이번 주 쇼핑 앱 사용 시간",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextDark
                    )
                    Text(
                        text = "일주일간의 쇼핑 몰입도 추이를 분석합니다.",
                        fontSize = 11.sp,
                        color = AppleTextGrey
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PastelBlue.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    val average = weeklyData.map { it.minutes }.average().toInt()
                    Text(
                        text = "일평균 ${average}분",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bars Row
            val maxMinutes = (weeklyData.maxOfOrNull { it.minutes } ?: 60).coerceAtLeast(60)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { data ->
                    val barHeightFraction = (data.minutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.08f, 1f)
                    val isToday = data.label == "오늘"
                    
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Minutes text above bar
                        Text(
                            text = "${data.minutes}분",
                            fontSize = 9.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) PastelBlue else AppleTextGrey
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Bar drawing
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(barHeightFraction * 0.75f) // reserve space for text
                                .width(16.dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    if (isToday) {
                                        Brush.verticalGradient(
                                            colors = listOf(PastelBlue, PastelBlue.copy(alpha = 0.6f))
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            colors = listOf(Slate200, Slate100)
                                        )
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Day Label
                        Text(
                            text = data.label,
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isToday) PastelBlue else AppleTextDark
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 탭 1: 대시보드 화면
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: ShoppingViewModel,
    shoppingApps: List<ShoppingApp>,
    blockedImpulses: List<BlockedImpulse>,
    onNavigateToTab: (Int) -> Unit
) {
    val colors = LocalCustomColors.current
    val AppleBgLight = colors.appleBgLight
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelRed = colors.pastelRed
    val PastelBlue = colors.pastelBlue
    val PastelMint = colors.pastelMint
    val Slate100 = colors.slate100
    val Slate200 = colors.slate200

    val currentQuote by viewModel.currentQuote.collectAsStateWithLifecycle()
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.KOREA) }
    val monthlyBudgetState by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()

    // 통계 및 요약 계산
    val successBlocked = blockedImpulses.filter { it.isBlocked }
    val failedPurchase = blockedImpulses.filter { !it.isBlocked }

    val totalSavedMoney = successBlocked.sumOf { it.itemPrice.toLong() }
    val totalSpentMoney = failedPurchase.sumOf { it.itemPrice.toLong() }

    // 7월 소비 집계
    val julySpent = blockedImpulses.filter { impulse ->
        if (!impulse.isBlocked) {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = impulse.timestamp }
            cal.get(java.util.Calendar.MONTH) == 6 // July
        } else false
    }.sumOf { it.itemPrice.toLong() }

    val displaySpent = julySpent
    val displayBudget = monthlyBudgetState
    val progressPercent = if (displayBudget > 0) {
        (displaySpent.toFloat() / displayBudget.toFloat() * 100).toInt()
    } else {
        0
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // 1. 대시보드 맞춤형 탑 바
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEnglish) "Stop Shopping" else "쇼핑 그만",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = AppleTextDark,
                    letterSpacing = (-0.5).sp
                )
                
                IconButton(
                    onClick = { /* 알림 액션 */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = AppleTextDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 2. 경고 배너 카드 (잠깐! 지금은 쇼핑을 멈출 시간이에요)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = PastelRed),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ShoppingBagIcon(modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (isEnglish) "Hold on!\nTime to stop shopping" else "잠깐! 지금은\n쇼핑을 멈출 시간이에요",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isEnglish) "You exceeded today's shopping budget." else "오늘 쇼핑 예산을 초과했어요.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // 쇼핑 멈추기 흰색 둥근 버튼
                    Button(
                        onClick = {
                            // 결제 브레이크 작동 시뮬레이션 즉각 가동!
                            viewModel.triggerBrakeSimulator("충동구매 물품", "128000")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = PastelRed
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(0.9f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text(
                            text = if (isEnglish) "Stop Shopping Now" else "쇼핑 멈추기",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 3. 오늘의 소비 요약 카드
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = AppleCardLight),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.t("today_consumption_summary", isEnglish),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextDark
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onNavigateToTab(1) } // 통계 탭으로
                        ) {
                            Text(
                                text = Localization.t("more", isEnglish),
                                fontSize = 11.sp,
                                color = AppleTextGrey,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "More",
                                tint = AppleTextGrey,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = Localization.t("spent_amount", isEnglish),
                                fontSize = 11.sp,
                                color = AppleTextGrey,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (isEnglish) {
                                    "${formatter.format(displaySpent).replace("₩", "")} ${Localization.t("won", isEnglish)}"
                                } else {
                                    "${formatter.format(displaySpent).replace("₩", "")}원"
                                },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = PastelRed
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isEnglish) {
                                    "${Localization.t("budget", isEnglish)} ${formatter.format(displayBudget).replace("₩", "")} ${Localization.t("won", isEnglish)}"
                                } else {
                                    "예산 ${formatter.format(displayBudget).replace("₩", "")}원"
                                },
                                fontSize = 11.sp,
                                color = AppleTextGrey
                            )
                        }
                        
                        // 실시간 꺾은선 차트 드로잉 영역
                        LineChartMini(
                            modifier = Modifier
                                .width(120.dp)
                                .height(56.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 빨간색 프로그레스바
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Slate100)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((progressPercent / 100f).coerceIn(0f, 1f))
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PastelRed)
                            )
                        }
                        Text(
                            text = "$progressPercent%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextDark
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 4. 등록된 쇼핑 앱 카드
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = AppleCardLight),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.t("registered_shopping_apps", isEnglish),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextDark
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onNavigateToTab(2) } // 설정 탭으로
                        ) {
                            Text(
                                text = Localization.t("manage", isEnglish),
                                fontSize = 11.sp,
                                color = AppleTextGrey,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Manage",
                                tint = AppleTextGrey,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // 5개 대표 앱 가로 Row 목록
                    val brandApps = listOf("쿠팡", "무신사", "11번가", "G마켓", "위메프")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        brandApps.forEach { appName ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.clickable {
                                    // 해당 앱을 누르면 정보 보기 또는 시뮬레이터
                                    viewModel.triggerBrakeSimulator(appName, "35000")
                                }
                            ) {
                                ShoppingAppBrandIcon(brandName = appName)
                                Text(
                                    text = appName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppleTextDark
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 5. 오늘의 한마디 카드
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = AppleCardLight),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.65f)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = Localization.t("todays_quote", isEnglish),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "“$currentQuote”",
                            fontSize = 11.sp,
                            color = AppleTextGrey,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 15.sp
                        )
                    }
                    
                    // 우측에 AI 생성한 일러스트 캐릭터 귀엽게 배치
                    Image(
                        painter = painterResource(id = R.drawable.img_wise_character),
                        contentDescription = "Advice Character",
                        modifier = Modifier
                            .size(90.dp)
                            .align(Alignment.BottomEnd)
                            .padding(end = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

// 오늘의 명언 카드 컴포저블
@Composable
fun QuoteCard(quote: String, onRefresh: () -> Unit) {
    val colors = LocalCustomColors.current
    val AppleBgLight = colors.appleBgLight
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelBlue = colors.pastelBlue
    val PastelPeach = colors.pastelPeach
    val PastelMint = colors.pastelMint
    val PastelYellow = colors.pastelYellow
    val PastelRed = colors.pastelRed
    val Indigo50 = colors.bgGradientStart
    val Rose50 = colors.bgGradientEnd
    val Slate100 = colors.slate100
    val Slate200 = colors.slate200

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = AppleCardLight),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .background(AppleCardLight)
                .border(1.dp, Slate100, RoundedCornerShape(20.dp))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(PastelYellow.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💡", fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "오늘의 충동구매 브레이커 조언",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextDark
                    )
                }
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Shuffle Quote",
                        tint = PastelBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "“$quote”",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppleTextGrey,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 히스토리 항목 카드 컴포저블
@Composable
fun HistoryItemCard(item: BlockedImpulse, isEnglish: Boolean) {
    val colors = LocalCustomColors.current
    val AppleBgLight = colors.appleBgLight
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelBlue = colors.pastelBlue
    val PastelPeach = colors.pastelPeach
    val PastelMint = colors.pastelMint
    val PastelYellow = colors.pastelYellow
    val PastelRed = colors.pastelRed
    val Indigo50 = colors.bgGradientStart
    val Rose50 = colors.bgGradientEnd
    val Slate100 = colors.slate100
    val Slate200 = colors.slate200

    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.KOREA) }
    val sdf = remember { SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate100, RoundedCornerShape(16.dp))
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = AppleCardLight),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (item.isBlocked) PastelMint else PastelRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isBlocked) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = if (item.isBlocked) Localization.t("history_saved_desc", isEnglish) else Localization.t("history_spent_desc", isEnglish),
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.itemName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (item.isBlocked) Localization.t("history_saved_badge", isEnglish) else Localization.t("history_spent_badge", isEnglish),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isBlocked) Color(0xFF4CD964) else PastelRed
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) {
                            "%,d KRW".format(item.itemPrice)
                        } else {
                            "${formatter.format(item.itemPrice).replace("₩", "")}원"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextGrey
                    )
                    Text(
                        text = sdf.format(Date(item.timestamp)),
                        fontSize = 10.sp,
                        color = AppleTextGrey.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // 적용한 다짐 문구 표시
                Text(
                    text = "${Localization.t("history_resolution_prefix", isEnglish)}: ${item.resolutionSelected}",
                    fontSize = 11.sp,
                    color = AppleTextDark.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                )
            }
        }
    }
}

// ==========================================
// 탭 1: 소비 통계 분석 화면 (이미지 2 리디자인)
// ==========================================
@Composable
fun StatisticsScreen(
    viewModel: ShoppingViewModel,
    blockedImpulses: List<BlockedImpulse>
) {
    val colors = LocalCustomColors.current
    val AppleBgLight = colors.appleBgLight
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelRed = colors.pastelRed
    val PastelBlue = colors.pastelBlue
    val PastelMint = colors.pastelMint
    val PastelPeach = colors.pastelPeach
    val Slate100 = colors.slate100
    val Slate200 = colors.slate200

    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.KOREA) }

    // 통계 계산 (실제 데이터와 시뮬레이션 데이터를 자연스럽게 믹스)
    val successBlocked = blockedImpulses.filter { it.isBlocked }
    val failedPurchase = blockedImpulses.filter { !it.isBlocked }

    val totalSavedMoney = successBlocked.sumOf { it.itemPrice.toLong() }
    val totalSpentMoney = failedPurchase.sumOf { it.itemPrice.toLong() }

    val monthlyBudgetState by viewModel.monthlyBudget.collectAsStateWithLifecycle()

    // 5월, 6월, 7월 데이터 집계
    val monthlySpent = mutableMapOf<String, Long>(
        "5월" to 0L,
        "6월" to 0L,
        "7월" to 0L
    )

    blockedImpulses.forEach { impulse ->
        if (!impulse.isBlocked) {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = impulse.timestamp }
            val m = cal.get(java.util.Calendar.MONTH) // 0-indexed (4 for May, 5 for June, 6 for July)
            val monthStr = when (m) {
                4 -> "5월"
                5 -> "6월"
                6 -> "7월"
                else -> null
            }
            if (monthStr != null) {
                monthlySpent[monthStr] = (monthlySpent[monthStr] ?: 0L) + impulse.itemPrice
            }
        }
    }

    val displayTotalSpent = monthlySpent["7월"] ?: 0L
    val displayBudget = monthlyBudgetState
    val displayOverBudget = displayTotalSpent - displayBudget

    // 일평균 계산 (7월 기준 31일 나누기)
    val displayAverageDaily = if (displayTotalSpent > 0) {
        displayTotalSpent / 31
    } else {
        0L
    }

    // 7월 쇼핑 횟수 (실패한 내역 개수)
    val julyFailedCount = blockedImpulses.count { impulse ->
        if (!impulse.isBlocked) {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = impulse.timestamp }
            cal.get(java.util.Calendar.MONTH) == 6
        } else false
    }
    val displayShoppingCount = julyFailedCount

    // 카테고리별 소비액 집계 (7월 기준)
    val categorySpending = mutableMapOf(
        "패션/의류" to 0L,
        "생활/건강" to 0L,
        "전자기기" to 0L,
        "식품" to 0L,
        "기타" to 0L
    )

    blockedImpulses.forEach { impulse ->
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = impulse.timestamp }
        if (cal.get(java.util.Calendar.MONTH) == 6) { // 7월
            val cat = when {
                impulse.itemName.contains("갤럭시") || impulse.itemName.contains("아이폰") || impulse.itemName.contains("헤드폰") || impulse.itemName.contains("마우스") || impulse.itemName.contains("키보드") || impulse.itemName.contains("전자기기") -> "전자기기"
                impulse.itemName.contains("자켓") || impulse.itemName.contains("런닝화") || impulse.itemName.contains("반팔티") || impulse.itemName.contains("의류") || impulse.itemName.contains("패션") || impulse.itemName.contains("가죽") || impulse.itemName.contains("지갑") -> "패션/의류"
                impulse.itemName.contains("영양제") || impulse.itemName.contains("오메가3") || impulse.itemName.contains("생활") || impulse.itemName.contains("건강") || impulse.itemName.contains("향수") -> "생활/건강"
                impulse.itemName.contains("피자") || impulse.itemName.contains("단백질") || impulse.itemName.contains("식품") || impulse.itemName.contains("야식") || impulse.itemName.contains("치킨") -> "식품"
                else -> "기타"
            }
            categorySpending[cat] = (categorySpending[cat] ?: 0L) + impulse.itemPrice
        }
    }

    val totalJulySpentForCategories = categorySpending.values.sum()
    val categoryList = if (totalJulySpentForCategories > 0) {
        categorySpending.map { (cat, amt) ->
            cat to (amt.toDouble() / totalJulySpentForCategories.toDouble() * 100.0)
        }
    } else {
        listOf(
            "패션/의류" to 40.0,
            "생활/건강" to 20.0,
            "전자기기" to 20.0,
            "식품" to 10.0,
            "기타" to 10.0
        )
    }

    val categoryColors = listOf(
        Color(0xFFE52521), // 패션/의류: 강렬한 레드
        Color(0xFF007AFF), // 생활/건강: 스카이 블루
        Color(0xFFFF9500), // 전자기기: 비비드 오렌지
        Color(0xFF34C759), // 식품: 신선한 그린
        Color(0xFF8E8E93)  // 기타: 차분한 그레이
    )

    // 소비 추이 X, Y 레이블 및 데이터 포인트 준비 (3월 ~ 7월 추이)
    val trendLabels = if (isEnglish) {
        listOf("Mar", "Apr", "May", "Jun", "Jul")
    } else {
        listOf("3월", "4월", "5월", "6월", "7월")
    }
    val trendYLabels = if (isEnglish) {
        listOf("0", "200k", "400k", "600k")
    } else {
        listOf("0", "20만", "40만", "60만")
    }
    
    // 3월과 4월은 임의의 자연스러운 시드 값(100000원, 150000원), 5/6/7월은 실제 데이터 반영
    val maySpent = monthlySpent["5월"] ?: 0L
    val juneSpent = monthlySpent["6월"] ?: 0L
    val julySpent = monthlySpent["7월"] ?: 0L

    val maxChartVal = 600000f
    val trendPoints = listOf(
        100000f / maxChartVal,
        150000f / maxChartVal,
        maySpent.toFloat() / maxChartVal,
        juneSpent.toFloat() / maxChartVal,
        julySpent.toFloat() / maxChartVal
    ).map { it.coerceIn(0f, 1f) }

    val tabs = if (isEnglish) listOf("Day", "Week", "Month") else listOf("일", "주", "월")
    var selectedPeriodTab by remember(isEnglish) { mutableStateOf(if (isEnglish) "Month" else "월") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // 1. 소비 통계 상단 헤더 영역 (이미지 리디자인 스타일)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Localization.t("consumption_stats", isEnglish),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextDark
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 이전 버튼
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            .clip(CircleShape)
                            .clickable { /* 이전 달 */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Prev Month",
                            tint = AppleTextDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // 날짜 표시 드롭다운 버튼
                    Row(
                        modifier = Modifier
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .clickable { /* 월 선택 */ }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isEnglish) "Jul 2024" else "2024년 7월",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextDark
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = AppleTextGrey,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // 다음 버튼
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            .clip(CircleShape)
                            .clickable { /* 다음 달 */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = AppleTextDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // 2. 총 지출 카드 (예산 초과 하이라이트 배지 포함)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, Slate100, RoundedCornerShape(24.dp))
                    .shadow(1.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = AppleCardLight),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Localization.t("total_spent", isEnglish),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppleTextGrey
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEnglish) "%,d KRW".format(displayTotalSpent) else "%,d원".format(displayTotalSpent),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = AppleTextDark,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEnglish) "${Localization.t("budget", isEnglish)} %,d KRW".format(displayBudget) else "예산 %,d원".format(displayBudget),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppleTextGrey
                        )
                    }
                    
                    // 예산 초과 상태 배지
                    if (displayOverBudget > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFFECEC))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isEnglish) {
                                    "+%,d KRW\n(Over Budget)".format(displayOverBudget)
                                } else {
                                    "+%,d원\n(예산 초과)".format(displayOverBudget)
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE52521),
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 3. 평균 일 지출 & 쇼핑 횟수 카드 세트 (2열 배치)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 평균 일 지출 카드
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Slate100, RoundedCornerShape(24.dp))
                        .shadow(1.dp, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = AppleCardLight),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = Localization.t("avg_daily_spent", isEnglish),
                            fontSize = 11.sp,
                            color = AppleTextGrey,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isEnglish) "%,d KRW".format(displayAverageDaily) else "%,d원".format(displayAverageDaily),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${Localization.t("vs_last_month", isEnglish)} +23%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE52521)
                        )
                    }
                }

                // 쇼핑 횟수 카드
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Slate100, RoundedCornerShape(24.dp))
                        .shadow(1.dp, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = AppleCardLight),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = Localization.t("shopping_count", isEnglish),
                            fontSize = 11.sp,
                            color = AppleTextGrey,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isEnglish) "$displayShoppingCount ${Localization.t("times_unit", isEnglish)}" else "${displayShoppingCount}회",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEnglish) {
                                "${Localization.t("vs_last_month", isEnglish)} +7 ${Localization.t("times_unit", isEnglish)}"
                            } else {
                                "지난 달 대비 +7회"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE52521)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 4. 카테고리별 지출 카드 (도넛 차트 탑재)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate100, RoundedCornerShape(24.dp))
                    .shadow(1.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = AppleCardLight),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = Localization.t("category_spent", isEnglish),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextDark
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 왼쪽: 도넛 차트 캔버스
                        CategoryDonutChart(
                            modifier = Modifier
                                .size(120.dp)
                                .weight(1f),
                            categories = categoryList,
                            colors = categoryColors,
                            centerText = if (isEnglish) "%,d KRW".format(displayTotalSpent) else "%,d원".format(displayTotalSpent)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // 오른쪽: 카테고리 상세 목록
                        Column(
                            modifier = Modifier.weight(1.2f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categoryList.forEachIndexed { index, pair ->
                                val color = categoryColors[index]
                                val calculatedValue = (displayTotalSpent * (pair.second / 100.0)).toLong()
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val localizedCat = when (pair.first) {
                                            "패션/의류" -> Localization.t("cat_fashion", isEnglish)
                                            "생활/건강" -> Localization.t("cat_life", isEnglish)
                                            "전자기기" -> Localization.t("cat_digital", isEnglish)
                                            "식품" -> Localization.t("cat_food", isEnglish)
                                            else -> Localization.t("cat_etc", isEnglish)
                                        }
                                        Text(
                                            text = localizedCat,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AppleTextDark
                                        )
                                    }
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${pair.second.toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppleTextDark
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isEnglish) "(%,d KRW)".format(calculatedValue) else "(%,d원)".format(calculatedValue),
                                            fontSize = 10.sp,
                                            color = AppleTextGrey
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 5. 소비 추이 카드 (꺾은선 차트 탑재)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate100, RoundedCornerShape(24.dp))
                    .shadow(1.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = AppleCardLight),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.t("trend_chart_title", isEnglish),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextDark
                        )
                        
                        // 일, 주, 월 알약 탭 선택 버튼
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF2F2F7))
                                .padding(2.dp)
                        ) {
                            tabs.forEach { tab ->
                                val isSelected = tab == selectedPeriodTab
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFFE52521) else Color.Transparent)
                                        .clickable { selectedPeriodTab = tab }
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tab,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else AppleTextGrey
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 꺾은선 차트 그리기
                    ConsumptionTrendsLineChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        points = trendPoints,
                        xLabels = trendLabels,
                        yLabels = trendYLabels,
                        lineColor = Color(0xFFE52521)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 6. 충동구매 브레이크 히스토리 (기능성 보전)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📝 ${Localization.t("break_history", isEnglish)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextDark
                )
                if (blockedImpulses.isNotEmpty()) {
                    Text(
                        text = Localization.t("reset_all", isEnglish),
                        fontSize = 11.sp,
                        color = PastelRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.resetStatistics() }
                            .padding(4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (blockedImpulses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Empty History",
                            tint = AppleTextGrey.copy(alpha = 0.4f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Localization.t("no_break_history_desc", isEnglish),
                            fontSize = 12.sp,
                            color = AppleTextGrey
                        )
                    }
                }
            }
        } else {
            items(blockedImpulses) { item ->
                HistoryItemCard(item = item, isEnglish = isEnglish)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// 쇼핑앱 제한 관리 카드
@Composable
fun ShoppingAppLimitCard(
    app: ShoppingApp,
    onSimulateTime: (Int) -> Unit,
    onLockToggle: () -> Unit,
    onDelete: () -> Unit,
    onLimitChange: (Int) -> Unit
) {
    val colors = LocalCustomColors.current
    val AppleBgLight = colors.appleBgLight
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelBlue = colors.pastelBlue
    val PastelPeach = colors.pastelPeach
    val PastelMint = colors.pastelMint
    val PastelYellow = colors.pastelYellow
    val PastelRed = colors.pastelRed
    val Indigo50 = colors.bgGradientStart
    val Rose50 = colors.bgGradientEnd
    val Slate100 = colors.slate100
    val Slate200 = colors.slate200

    val isOverLimit = app.usedMinutesToday >= app.dailyLimitMinutes
    val progress = if (app.dailyLimitMinutes > 0) {
        (app.usedMinutesToday.toFloat() / app.dailyLimitMinutes.toFloat()).coerceIn(0f, 1f)
    } else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate100, RoundedCornerShape(20.dp))
            .shadow(1.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isLocked) Color(0xFFF0F2F6) else AppleCardLight
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 상단: 이름, 카테고리, 삭제 아이콘
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isOverLimit) PastelRed else PastelBlue.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (app.isLocked) "🔒" else "🛍️",
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = app.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (app.isLocked) AppleTextGrey else AppleTextDark
                            )
                            if (app.isLocked) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge(containerColor = PastelRed) {
                                    Text("완전잠금", color = Color.White, fontSize = 9.sp)
                                }
                            }
                        }
                        Text(
                            text = app.category,
                            fontSize = 11.sp,
                            color = AppleTextGrey
                        )
                    }
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete App",
                        tint = PastelRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 중단: 사용 한도 시간 조정 컨트롤 (+/-)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "하루 한도 설정:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleTextDark
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (app.dailyLimitMinutes > 5) onLimitChange(app.dailyLimitMinutes - 5) },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E5EA))
                    ) {
                        Icon(Icons.Default.Remove, "Decrease", modifier = Modifier.size(14.dp), tint = AppleTextDark)
                    }
                    
                    Text(
                        text = "${app.dailyLimitMinutes}분",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = AppleTextDark
                    )

                    IconButton(
                        onClick = { onLimitChange(app.dailyLimitMinutes + 5) },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E5EA))
                    ) {
                        Icon(Icons.Default.Add, "Increase", modifier = Modifier.size(14.dp), tint = AppleTextDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 중단 2: 사용 통계 게이지 바
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "오늘 사용량: ${app.usedMinutesToday}분",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverLimit) PastelRed else AppleTextDark
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverLimit) PastelRed else AppleTextGrey
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isOverLimit) PastelRed else PastelBlue,
                    trackColor = Color(0xFFE5E5EA)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 하단: 실제 제어 및 테스트용 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 사용 시뮬레이터 조작 (+5분)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "테스트용 조작:",
                        fontSize = 11.sp,
                        color = AppleTextGrey,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { onSimulateTime(5) },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelBlue.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("+5분", color = PastelBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onSimulateTime(-5) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E5EA)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("-5분", color = AppleTextGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 앱 잠금 활성화 스위치 토글
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onLockToggle() }
                        .background(if (app.isLocked) PastelRed.copy(alpha = 0.15f) else Color(0xFFF2F2F7))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (app.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Lock Switch",
                        tint = if (app.isLocked) PastelRed else AppleTextGrey,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (app.isLocked) "잠금 해제" else "앱 잠금",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (app.isLocked) PastelRed else AppleTextDark
                    )
                }
            }
        }
    }
}

// 설치된 앱 정보 모델
data class InstalledAppInfo(
    val name: String,
    val packageName: String,
    val category: String,
    val isRecommended: Boolean
)

// 디바이스에 설치된 실행 가능한 앱 중 쇼핑앱 선별 로직
fun getInstalledApps(context: android.content.Context): List<InstalledAppInfo> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    
    val resolveInfos = try {
        pm.queryIntentActivities(intent, 0)
    } catch (e: Exception) {
        emptyList()
    }
    
    // 한국인들이 많이 쓰는 대표적인 쇼핑, 패션, 해외직구, 마트/식품 키워드 모음
    val shoppingKeywords = listOf(
        "쇼핑", "쿠팡", "11번가", "티몬", "위메프", "지마켓", "옥션", "올리브영", "무신사", 
        "지그재그", "에이블리", "당근", "중고나라", "번개장터", "알리", "테무", "마켓컬리", 
        "컬리", "홈쇼핑", "백화점", "마트", "다이소", "쓱", "ssg", "롯데온", "트렌비", 
        "발란", "kream", "크림", "shopping", "coupang", "gmarket", "auction", 
        "11st", "tmon", "wemake", "musinsa", "zigzag", "ably", "aliexpress", 
        "temu", "amazon", "ebay", "shopee", "qoo10", "market", "store", "mall", 
        "lotte", "ssg", "kurly", "daiso", "kream", "g9", "lotteon", "hfashion",
        "wconcept", "sivillage", "brandi", "lookpin"
    )
    
    val appsList = mutableListOf<InstalledAppInfo>()
    
    for (info in resolveInfos) {
        val appName = info.loadLabel(pm).toString()
        val packageName = info.activityInfo.packageName
        val appInfo = info.activityInfo.applicationInfo
        
        // 1. Android Oreo 이상에서 제공하는 표준 카테고리 속성 확인 (가장 교과서적인 판별법)
        val isShoppingCategory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appInfo.category == 4 // ApplicationInfo.CATEGORY_SHOPPING
        } else {
            false
        }
        
        // 2. 한국/글로벌 커스텀 쇼핑 앱 키워드 감지 판별법
        val appNameLower = appName.lowercase()
        val packageNameLower = packageName.lowercase()
        val hasKeyword = shoppingKeywords.any { 
            appNameLower.contains(it) || packageNameLower.contains(it) 
        }
        
        val isRecommended = isShoppingCategory || hasKeyword
        
        // 지능형 자동 카테고리 분류
        val matchedCategory = when {
            appNameLower.contains("해외") || appNameLower.contains("직구") || appNameLower.contains("ali") || appNameLower.contains("aliexpress") || appNameLower.contains("temu") || appNameLower.contains("테무") || packageNameLower.contains("aliexpress") || packageNameLower.contains("temu") -> "해외직구"
            appNameLower.contains("패션") || appNameLower.contains("뷰티") || appNameLower.contains("의류") || appNameLower.contains("무신사") || appNameLower.contains("에이블리") || appNameLower.contains("지그재그") || appNameLower.contains("brandi") || appNameLower.contains("kream") || appNameLower.contains("크림") -> "패션/뷰티"
            appNameLower.contains("식품") || appNameLower.contains("마트") || appNameLower.contains("컬리") || appNameLower.contains("kurly") || appNameLower.contains("야식") || appNameLower.contains("배달") || appNameLower.contains("요기요") || appNameLower.contains("배민") || appNameLower.contains("배달의민족") -> "식품/마트"
            else -> "종합 쇼핑"
        }
        
        // 자기 자신(ShoppingStopApp)은 차단 및 추천 목록에서 제외
        if (packageName != context.packageName) {
            appsList.add(
                InstalledAppInfo(
                    name = appName,
                    packageName = packageName,
                    category = matchedCategory,
                    isRecommended = isRecommended
                )
            )
        }
    }
    
    return appsList.sortedWith(
        compareByDescending<InstalledAppInfo> { it.isRecommended }
            .thenBy { it.name }
    )
}

// 쇼핑앱 직접/설치 추가 팝업 다이얼로그
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShoppingAppDialog(
    onDismiss: () -> Unit,
    onAddApps: (List<Triple<String, String, String?>>, Int) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalCustomColors.current
    val AppleBgLight = colors.appleBgLight
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelBlue = colors.pastelBlue
    val PastelPeach = colors.pastelPeach
    val PastelMint = colors.pastelMint
    val PastelYellow = colors.pastelYellow
    val PastelRed = colors.pastelRed
    val Indigo50 = colors.bgGradientStart
    val Rose50 = colors.bgGradientEnd
    val Slate100 = colors.slate100
    val Slate200 = colors.slate200

    var addMethodTab by remember { mutableStateOf(0) } // 0: 설치 앱 선택, 1: 직접 등록

    var appName by remember { mutableStateOf("") }
    var limitString by remember { mutableStateOf("30") }
    var selectedCategory by remember { mutableStateOf("종합 쇼핑") }
    
    // 다중 선택을 지원하기 위한 상태 변수
    val selectedApps = remember { mutableStateMapOf<String, InstalledAppInfo>() }
    
    val categories = listOf("종합 쇼핑", "패션/뷰티", "해외직구", "식품/마트", "기타")

    // 설치 앱 목록 로드 및 필터 상태
    var installedApps by remember { mutableStateOf<List<InstalledAppInfo>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // 인기 쇼핑앱 프리셋 (가상 추천용 - 에뮬레이터 환경 완벽 호환)
    val popularPresetApps = remember {
        listOf(
            InstalledAppInfo("쿠팡 (Coupang)", "com.coupang.mobile", "종합 쇼핑", true),
            InstalledAppInfo("무신사 (Musinsa)", "com.musinsa.store", "패션/뷰티", true),
            InstalledAppInfo("알리익스프레스", "com.alibaba.aliexpress", "해외직구", true),
            InstalledAppInfo("에이블리 (Ably)", "com.ably.retail", "패션/뷰티", true),
            InstalledAppInfo("올리브영 (Olive Young)", "com.cj.oliveyoung", "패션/뷰티", true),
            InstalledAppInfo("당근마켓 (Daangn)", "com.towneers.www", "종합 쇼핑", true),
            InstalledAppInfo("마켓컬리 (Market Kurly)", "kr.co.kurly.kurlyToGo", "식품/마트", true),
            InstalledAppInfo("테무 (Temu)", "com.einnovation.temu", "해외직구", true)
        )
    }

    LaunchedEffect(Unit) {
        isLoadingApps = true
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            getInstalledApps(context)
        }.let {
            installedApps = it
            isLoadingApps = false
        }
    }

    // 추천 또는 전체 필터링된 목록 준비
    val detectedShoppingApps = installedApps.filter { it.isRecommended }
    val recommendedListToShow = remember(installedApps) {
        if (detectedShoppingApps.isEmpty()) {
            popularPresetApps
        } else {
            val detectedPackages = detectedShoppingApps.map { it.packageName }.toSet()
            detectedShoppingApps + popularPresetApps.filter { it.packageName !in detectedPackages }
        }
    }

    val finalRecommendedList = remember(recommendedListToShow, searchQuery) {
        if (searchQuery.isBlank()) {
            recommendedListToShow
        } else {
            recommendedListToShow.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    val finalAllInstalledList = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = AppleCardLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .shadow(12.dp, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🛍️ 차단할 쇼핑앱 등록",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = AppleTextDark
                )
                
                Spacer(modifier = Modifier.height(14.dp))

                // 세그먼트 전환 탭 UI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("📱 설치된 앱 선택", "✍️ 직접 등록").forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (addMethodTab == index) Color.White else Color.Transparent)
                                .clickable { 
                                    addMethodTab = index 
                                    // 탭 전환 시 기존 선택 초기화
                                    if (index == 1) {
                                        appName = ""
                                        selectedApps.clear()
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (addMethodTab == index) AppleTextDark else AppleTextGrey
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (addMethodTab == 0) {
                    // === 설치된 앱 선택 탭 ===
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 판단 기준 설명 요약
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PastelBlue.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .border(1.dp, PastelBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "💡 쇼핑앱 자동 판별 기준:\n안드로이드 시스템 카테고리가 '쇼핑'인 앱이거나, 이름/패키지명에 쇼핑 키워드(쿠팡, 무신사, 마트, 쇼핑 등)가 분석된 앱을 판별하여 최상단 추천으로 띄워 드립니다.",
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = PastelBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 검색 필드
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("앱 이름 검색...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = AppleTextGrey, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PastelBlue,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 앱 선택 리스트 컨테이너 헤더 (여기에 전체 선택 추가)
                        val listToShow = if (searchQuery.isBlank() || finalRecommendedList.isNotEmpty()) {
                            finalRecommendedList
                        } else {
                            finalAllInstalledList
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "✨ 추천 쇼핑 앱" else "🔍 검색 결과",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleTextGrey
                            )
                            
                            val isAllSelected = listToShow.isNotEmpty() && listToShow.all { selectedApps.containsKey(it.packageName) }
                            
                            TextButton(
                                onClick = {
                                    if (isAllSelected) {
                                        listToShow.forEach { selectedApps.remove(it.packageName) }
                                    } else {
                                        listToShow.forEach { selectedApps[it.packageName] = it }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isAllSelected) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                        contentDescription = "전체 선택",
                                        tint = if (isAllSelected) PastelBlue else AppleTextGrey,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isAllSelected) "전체 해제" else "전체 선택",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAllSelected) PastelBlue else AppleTextGrey
                                    )
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            if (isLoadingApps) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = PastelBlue, strokeWidth = 3.dp)
                                }
                            } else {
                                if (listToShow.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("검색된 앱이 없습니다.", fontSize = 12.sp, color = AppleTextGrey)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize().padding(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(listToShow) { app ->
                                            val isSelected = selectedApps.containsKey(app.packageName)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isSelected) PastelBlue.copy(alpha = 0.15f) else Color.Transparent)
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) PastelBlue else Color.Transparent,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable {
                                                        if (isSelected) {
                                                            selectedApps.remove(app.packageName)
                                                        } else {
                                                            selectedApps[app.packageName] = app
                                                        }
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // 가상 아이콘 로고
                                                ShoppingAppLogo(appName = app.name, modifier = Modifier.size(24.dp))
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = app.name,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AppleTextDark,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = app.packageName,
                                                        fontSize = 10.sp,
                                                        color = AppleTextGrey,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                
                                                // 카테고리 뱃지
                                                Box(
                                                    modifier = Modifier
                                                        .background(PastelMint.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = app.category,
                                                        color = PastelMint,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.width(8.dp))
                                                
                                                // 체크박스 아이콘 표시
                                                Icon(
                                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                                    contentDescription = "선택 상태",
                                                    tint = if (isSelected) PastelBlue else AppleTextGrey.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 선택 확인 표시 및 시간 설정
                        if (selectedApps.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PastelBlue.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = PastelBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "선택된 앱: ${selectedApps.size}개 (${selectedApps.values.joinToString(", ") { it.name }})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppleTextDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // 시간 제한 텍스트 필드
                        OutlinedTextField(
                            value = limitString,
                            onValueChange = { limitString = it.filter { char -> char.isDigit() } },
                            label = { Text("하루 한도 제한 시간 (분)", fontSize = 12.sp) },
                            placeholder = { Text("예: 30") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PastelBlue,
                                focusedLabelColor = PastelBlue
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // === 직접 등록 탭 ===
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = appName,
                            onValueChange = { appName = it },
                            label = { Text("쇼핑앱 이름") },
                            placeholder = { Text("예: 마이스타일숍") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PastelBlue,
                                focusedLabelColor = PastelBlue
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = limitString,
                            onValueChange = { limitString = it.filter { char -> char.isDigit() } },
                            label = { Text("하루 한도 제한 시간 (분)") },
                            placeholder = { Text("예: 20") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PastelBlue,
                                focusedLabelColor = PastelBlue
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 카테고리 칩 영역
                        Text(
                            text = "카테고리 선택:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextGrey,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            categories.take(3).forEach { cat ->
                                CategoryChip(
                                    category = cat,
                                    isSelected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            categories.drop(3).forEach { cat ->
                                CategoryChip(
                                    category = cat,
                                    isSelected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 액션 버튼 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소", color = AppleTextGrey, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val limit = limitString.toIntOrNull() ?: 30
                            if (addMethodTab == 0) {
                                if (selectedApps.isNotEmpty()) {
                                    val appsToRegister = selectedApps.values.map { 
                                        Triple(it.name, it.category, it.packageName as String?) 
                                    }
                                    onAddApps(appsToRegister, limit)
                                }
                            } else {
                                if (appName.isNotBlank()) {
                                    onAddApps(listOf(Triple(appName, selectedCategory, null)), limit)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelBlue),
                        enabled = if (addMethodTab == 0) selectedApps.isNotEmpty() else appName.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (addMethodTab == 0) "${selectedApps.size}개 앱 등록하기" else "등록하기", 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    val AppleBgLight = colors.appleBgLight
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelBlue = colors.pastelBlue
    val PastelPeach = colors.pastelPeach
    val PastelMint = colors.pastelMint
    val PastelYellow = colors.pastelYellow
    val PastelRed = colors.pastelRed
    val Indigo50 = colors.bgGradientStart
    val Rose50 = colors.bgGradientEnd
    val Slate100 = colors.slate100
    val Slate200 = colors.slate200

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(if (isSelected) PastelBlue else Color(0xFFE5E5EA))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = category,
            fontSize = 11.sp,
            color = if (isSelected) Color.White else AppleTextDark,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==========================================
// 탭 2: 설정 화면 (이미지 3 리디자인)
// ==========================================
@Composable
fun ShoppingAppLogo(appName: String, modifier: Modifier = Modifier) {
    val normalized = appName.lowercase()
    val (bgColor, letter) = when {
        normalized.contains("쿠팡") || normalized.contains("coupang") -> Color(0xFFE52521) to "C"
        normalized.contains("무신사") || normalized.contains("musinsa") -> Color(0xFF000000) to "M"
        normalized.contains("11번가") || normalized.contains("11st") -> Color(0xFFFF1B51) to "11"
        normalized.contains("g마켓") || normalized.contains("gmarket") -> Color(0xFF00B050) to "G"
        normalized.contains("위메프") || normalized.contains("wemake") -> Color(0xFFE60023) to "W"
        normalized.contains("네이버") || normalized.contains("naver") -> Color(0xFF03C75A) to "N"
        normalized.contains("알리") || normalized.contains("ali") -> Color(0xFFFF4747) to "A"
        else -> Color(0xFF8B5CF6) to appName.take(1).uppercase()
    }

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = Color.White,
            fontSize = if (letter == "11") 12.sp else 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF64748B),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItemRow(
    icon: @Composable () -> Unit,
    title: String,
    rightText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = rightText,
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Details",
                tint = Color(0xFFC7C7CC),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ShoppingViewModel,
    shoppingApps: List<ShoppingApp>,
    onThemeSelectorClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    val AppleBgLight = colors.appleBgLight
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelBlue = colors.pastelBlue
    val PastelPeach = colors.pastelPeach
    val PastelMint = colors.pastelMint
    val PastelYellow = colors.pastelYellow
    val PastelRed = colors.pastelRed
    val Slate100 = colors.slate100
    val Slate200 = colors.slate200

    // 사용자 설정 상태 라이브 관찰
    val monthlyBudgetState by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val overspendingAlertState by viewModel.overspendingAlert.collectAsStateWithLifecycle()
    val shoppingTimeLimitState by viewModel.shoppingTimeLimit.collectAsStateWithLifecycle()
    val nightAlertLimitState by viewModel.nightAlertLimit.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialogApp by remember { mutableStateOf<ShoppingApp?>(null) }
    var showOverspendingDialog by remember { mutableStateOf(false) }
    var showTimeLimitDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    var testItemName by remember { mutableStateOf("") }
    var testItemPriceString by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // 1. 설정 상단 타이틀
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "설정",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = AppleTextDark,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // 2. 쇼핑 앱 관리 섹션 (이미지 3 대응)
        item {
            SettingsSectionTitle(text = "쇼핑 앱 관리")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate100, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = AppleCardLight),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (shoppingApps.isEmpty()) {
                        Text(
                            text = "등록된 제한 쇼핑앱이 없습니다.",
                            fontSize = 13.sp,
                            color = AppleTextGrey,
                            modifier = Modifier.padding(24.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        shoppingApps.forEachIndexed { index, app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showEditDialogApp = app }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ShoppingAppLogo(appName = app.name)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = app.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AppleTextDark
                                        )
                                        Text(
                                            text = "${app.dailyLimitMinutes}분 제한 (오늘 ${app.usedMinutesToday}분 사용)",
                                            fontSize = 11.sp,
                                            color = AppleTextGrey
                                        )
                                    }
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 스위치 토글 (ON/OFF)
                                    Switch(
                                        checked = !app.isLocked,
                                        onCheckedChange = { viewModel.toggleAppLock(app) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = PastelRed,
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color(0xFFD1D1D6)
                                        )
                                    )
                                    
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Edit app",
                                        tint = Color(0xFFC7C7CC),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            if (index < shoppingApps.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )
                    
                    // + 앱 추가하기 버튼
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAddDialog = true }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = PastelRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "앱 추가하기",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelRed
                            )
                        }
                    }
                }
            }
        }

        // 3. 알림 설정 섹션 (이미지 3 대응)
        item {
            SettingsSectionTitle(text = "알림 설정")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate100, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = AppleCardLight),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 과소비 경고 알림
                    SettingsItemRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notification",
                                tint = PastelRed,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        title = "과소비 경고 알림",
                        rightText = overspendingAlertState,
                        onClick = { showOverspendingDialog = true }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )
                    
                    // 쇼핑 시간 제한
                    SettingsItemRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Time limit",
                                tint = PastelBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        title = "쇼핑 시간 제한",
                        rightText = shoppingTimeLimitState,
                        onClick = { showTimeLimitDialog = true }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )
                    
                    // 야간 알림 제한
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = "Night limit",
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "야간 알림 제한",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppleTextDark
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "오후 10시 ~ 오전 8시",
                                fontSize = 11.sp,
                                color = AppleTextGrey
                            )
                            Switch(
                                checked = nightAlertLimitState,
                                onCheckedChange = { viewModel.toggleNightAlertLimit() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PastelRed,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFD1D1D6)
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. 예산 설정 섹션 (이미지 3 대응)
        item {
            SettingsSectionTitle(text = "예산 설정")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate100, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = AppleCardLight),
                shape = RoundedCornerShape(22.dp)
            ) {
                SettingsItemRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = "Budget",
                            tint = PastelMint,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    title = "월 예산",
                    rightText = "%,d원".format(monthlyBudgetState),
                    onClick = { showBudgetDialog = true }
                )
            }
        }

        // 5. 기타 설정 섹션 (이미지 3 대응)
        item {
            SettingsSectionTitle(text = if (isEnglish) "Other Settings" else "기타 설정")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate100, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = AppleCardLight),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showResetDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEnglish) "Reset All Data" else "데이터 초기화",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Reset",
                            tint = Color(0xFFC7C7CC),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 6. 테마 설정 및 충동구매 테스트
        item {
            SettingsSectionTitle(text = "테마 및 충동구매 테스트")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate100, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = AppleCardLight),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎨 테마 설정",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextDark
                    )
                    Text(
                        text = "어플리케이션의 분위기 테마를 선택할 수 있습니다.",
                        fontSize = 11.sp,
                        color = AppleTextGrey,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = onThemeSelectorClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PastelBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "원하는 테마 색상 선택하기",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "🛑 충동구매 강제 브레이크",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextDark
                    )
                    Text(
                        text = "사려는 품목과 가격을 써서 브레이크 팝업을 직접 작동시켜 보세요.",
                        fontSize = 11.sp,
                        color = AppleTextGrey,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = testItemName,
                        onValueChange = { testItemName = it },
                        label = { Text("사려고 하는 상품명") },
                        placeholder = { Text("예: 나이키 신발, 아이패드") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PastelBlue,
                            focusedLabelColor = PastelBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = testItemPriceString,
                        onValueChange = { testItemPriceString = it.filter { char -> char.isDigit() } },
                        label = { Text("예상 가격 (원)") },
                        placeholder = { Text("예: 128000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PastelBlue,
                            focusedLabelColor = PastelBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.triggerBrakeSimulator(testItemName, testItemPriceString)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PanTool,
                            contentDescription = "Stop",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "결제 직전 브레이크 밟기",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // === 쇼핑앱 수동 추가 팝업 다이얼로그 ===
    if (showAddDialog) {
        AddShoppingAppDialog(
            onDismiss = { showAddDialog = false },
            onAddApps = { appsList, limit ->
                viewModel.addShoppingApps(appsList, limit)
                showAddDialog = false
            }
        )
    }

    // === 쇼핑앱 상세 편집 팝업 다이얼로그 ===
    showEditDialogApp?.let { app ->
        EditShoppingAppDialog(
            app = app,
            onDismiss = { showEditDialogApp = null },
            onDelete = {
                viewModel.deleteShoppingApp(app)
                showEditDialogApp = null
            },
            onLockToggle = { viewModel.toggleAppLock(app) },
            onSimulateTime = { delta -> viewModel.simulateAppUsage(app, delta) },
            onLimitChange = { newLimit -> viewModel.updateShoppingApp(app.copy(dailyLimitMinutes = newLimit)) },
            onRunApp = { viewModel.startShoppingAppSession(app) }
        )
    }

    // === 과소비 경고 알림 선택 다이얼로그 ===
    if (showOverspendingDialog) {
        OverspendingAlertDialog(
            currentOption = overspendingAlertState,
            onDismiss = { showOverspendingDialog = false },
            onSelectOption = { option -> viewModel.setOverspendingAlert(option) }
        )
    }

    // === 쇼핑 시간 제한 선택 다이얼로그 ===
    if (showTimeLimitDialog) {
        TimeLimitDialog(
            currentOption = shoppingTimeLimitState,
            onDismiss = { showTimeLimitDialog = false },
            onSelectOption = { option -> viewModel.setShoppingTimeLimit(option) }
        )
    }

    // === 월 예산 변경 다이얼로그 ===
    if (showBudgetDialog) {
        BudgetEditDialog(
            currentBudget = monthlyBudgetState,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { budget -> viewModel.setMonthlyBudget(budget) }
        )
    }

    // === 데이터 초기화 확인 다이얼로그 ===
    if (showResetDialog) {
        ResetConfirmDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = { viewModel.resetStatistics() }
        )
    }
}

// === 상세 편집 및 특수 알림 설정 팝업들 ===

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShoppingAppDialog(
    app: ShoppingApp,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onLockToggle: () -> Unit,
    onSimulateTime: (Int) -> Unit,
    onLimitChange: (Int) -> Unit,
    onRunApp: () -> Unit
) {
    val colors = LocalCustomColors.current
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelBlue = colors.pastelBlue
    val PastelRed = colors.pastelRed

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppleCardLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShoppingAppLogo(appName = app.name, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = app.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextDark
                )
                Text(
                    text = app.category,
                    fontSize = 12.sp,
                    color = AppleTextGrey
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 일일 제한 한도
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "일일 제한 한도",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextDark
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (app.dailyLimitMinutes > 5) onLimitChange(app.dailyLimitMinutes - 5) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5E5EA))
                        ) {
                            Icon(Icons.Default.Remove, "Decrease", modifier = Modifier.size(16.dp), tint = AppleTextDark)
                        }
                        Text(
                            text = "${app.dailyLimitMinutes}분",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = AppleTextDark
                        )
                        IconButton(
                            onClick = { onLimitChange(app.dailyLimitMinutes + 5) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5E5EA))
                        ) {
                            Icon(Icons.Default.Add, "Increase", modifier = Modifier.size(16.dp), tint = AppleTextDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 완전 잠금 설정
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "완전 잠금 설정",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextDark
                    )
                    Switch(
                        checked = app.isLocked,
                        onCheckedChange = { onLockToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PastelRed,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D1D6)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 사용 시뮬레이션
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "사용량 테스트",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextDark
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { onSimulateTime(5) },
                            colors = ButtonDefaults.buttonColors(containerColor = PastelBlue.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("+5분", color = PastelBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onSimulateTime(-5) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E5EA)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("-5분", color = AppleTextGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                // 쇼핑앱 실행하기 버튼 (감시 활성화)
                Button(
                    onClick = {
                        onDismiss() // 다이얼로그 닫기
                        onRunApp() // 세션 시작
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (app.isLocked || app.usedMinutesToday >= app.dailyLimitMinutes) Color(0xFFE2E8F0) else colors.pastelMint
                    ),
                    enabled = !(app.isLocked || app.usedMinutesToday >= app.dailyLimitMinutes),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (app.isLocked || app.usedMinutesToday >= app.dailyLimitMinutes) "🔒 시간 초과 또는 완전 잠금 상태" else "🛍️ 쇼핑 앱 실행 (감시 활성화)",
                            color = if (app.isLocked || app.usedMinutesToday >= app.dailyLimitMinutes) AppleTextGrey else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = PastelRed.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("앱 삭제", color = PastelRed, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = PastelBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("확인", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverspendingAlertDialog(
    currentOption: String,
    onDismiss: () -> Unit,
    onSelectOption: (String) -> Unit
) {
    val options = listOf("예산 초과 시", "예산 80% 도달 시", "예산 50% 도달 시", "알림 끄기")
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "과소비 경고 알림 조건",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectOption(option)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            fontSize = 14.sp,
                            color = if (option == currentOption) Color(0xFFE52521) else Color(0xFF334155),
                            fontWeight = if (option == currentOption) FontWeight.Bold else FontWeight.Medium
                        )
                        if (option == currentOption) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFFE52521),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("취소", color = Color(0xFF64748B))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeLimitDialog(
    currentOption: String,
    onDismiss: () -> Unit,
    onSelectOption: (String) -> Unit
) {
    val options = listOf("1일 30분", "1일 1시간", "1일 2시간", "시간 제한 없음")
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "쇼핑 시간 제한",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectOption(option)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            fontSize = 14.sp,
                            color = if (option == currentOption) Color(0xFFE52521) else Color(0xFF334155),
                            fontWeight = if (option == currentOption) FontWeight.Bold else FontWeight.Medium
                        )
                        if (option == currentOption) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFFE52521),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("취소", color = Color(0xFF64748B))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditDialog(
    currentBudget: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var budgetInput by remember { mutableStateOf(currentBudget.toString()) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "월 예산 변경",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it.filter { char -> char.isDigit() } },
                    label = { Text("월 예산 (원)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE52521),
                        focusedLabelColor = Color(0xFFE52521)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소", color = Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val budget = budgetInput.toLongOrNull() ?: 0L
                            if (budget > 0L) {
                                onConfirm(budget)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE52521)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("변경", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ResetConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFE52521),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "데이터를 초기화하시겠습니까?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "이 작업은 되돌릴 수 없으며, 모든 소비 내역이 초기 상태로 초기화됩니다.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소", color = Color(0xFF64748B))
                    }
                    Button(
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE52521)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("초기화", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 팝업 1: 결제 직전 브레이크 작동 다이얼로그 (나의 다짐 입력)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrakeSimulationDialog(
    viewModel: ShoppingViewModel
) {
    val colors = LocalCustomColors.current
    val AppleBgLight = colors.appleBgLight
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelBlue = colors.pastelBlue
    val PastelPeach = colors.pastelPeach
    val PastelMint = colors.pastelMint
    val PastelYellow = colors.pastelYellow
    val PastelRed = colors.pastelRed
    val Indigo50 = colors.bgGradientStart
    val Rose50 = colors.bgGradientEnd
    val Slate100 = colors.slate100
    val Slate200 = colors.slate200

    val itemName by viewModel.brakeItemName.collectAsStateWithLifecycle()
    val itemPrice by viewModel.brakeItemPrice.collectAsStateWithLifecycle()
    val selectedResolution by viewModel.selectedResolution.collectAsStateWithLifecycle()
    val userResolutionInput by viewModel.userResolutionInput.collectAsStateWithLifecycle()
    val currentQuote by viewModel.currentQuote.collectAsStateWithLifecycle()

    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.KOREA) }
    val formattedPrice = formatter.format(itemPrice.toIntOrNull() ?: 0)

    Dialog(
        onDismissRequest = { viewModel.closeBrakeDialog() }
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = AppleCardLight,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .shadow(12.dp, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 경고 아이콘 & 타이틀
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(PastelRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Stop Buying",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "🚨 쇼핑 멈춤 사이렌! 🚨",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = PastelRed
                )
                Text(
                    text = "정말 필요한 소비인지 냉정하게 판단할 시간입니다.",
                    fontSize = 11.sp,
                    color = AppleTextGrey,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 결제 희망 물건 요약 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PastelYellow.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "구매 시도 상품: $itemName",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "결제 금액: $formattedPrice",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = PastelRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 자극 명언 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F7)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "💡 명언: “$currentQuote”",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextDark,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 나의 다짐 선택 (5개 템플릿 칩)
                Text(
                    text = "📝 나의 다짐을 마음에 새겨주세요 (선택):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextDark,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 다짐 템플릿 스크롤 영역
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(viewModel.resolutionTemplates) { template ->
                        val isSelected = selectedResolution == template && userResolutionInput.isBlank()
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectResolution(template) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) PastelBlue.copy(alpha = 0.3f) else Color(0xFFF2F2F7)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = template,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AppleTextDark else AppleTextGrey,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 다짐 직접 입력창 (MZ 스타일의 꼼꼼한 확약 프로세스)
                OutlinedTextField(
                    value = userResolutionInput,
                    onValueChange = { viewModel.setUserResolutionInput(it) },
                    placeholder = { Text("또는 나만의 다짐을 직접 적어주세요...") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PastelBlue,
                        focusedLabelColor = PastelBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 최종 선택 액션 버튼 2개
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.confirmBrakeAndSave() },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelMint),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.VolunteerActivism, contentDescription = "Save", tint = AppleTextDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "충동구매 포기하고 돈 아끼기! 💚",
                            color = AppleTextDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.confirmBrakeAndPurchase() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelRed),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "다짐 어기고 그냥 구매하기... 💸",
                            color = PastelRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 팝업 2: 참기 성공/실패 결과 피드백 다이얼로그
// ==========================================
@Composable
fun ResultFeedbackDialog(
    isSuccess: Boolean,
    onDismiss: () -> Unit
) {
    val colors = LocalCustomColors.current
    val AppleBgLight = colors.appleBgLight
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelBlue = colors.pastelBlue
    val PastelPeach = colors.pastelPeach
    val PastelMint = colors.pastelMint
    val PastelYellow = colors.pastelYellow
    val PastelRed = colors.pastelRed
    val Indigo50 = colors.bgGradientStart
    val Rose50 = colors.bgGradientEnd
    val Slate100 = colors.slate100
    val Slate200 = colors.slate200

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSuccess) PastelMint else PastelRed.copy(alpha = 0.9f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(10.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSuccess) "🎉" else "😭",
                        fontSize = 44.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (isSuccess) "충동구매 방지 대성공!" else "앗, 지름신이 이겼습니다",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = AppleTextDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isSuccess) {
                        "엄청난 인내심입니다! 절약된 금액만큼 미래에 더 소중한 투자를 할 수 있게 되었습니다. 멋진 선택이에요! 💚"
                    } else {
                        "이번엔 무너졌지만 다음엔 참을 수 있습니다! 실망하지 말고 지속적인 알림 한도 및 다짐 템플릿 훈련을 받아보세요."
                    },
                    fontSize = 13.sp,
                    color = AppleTextDark.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AppleTextDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isSuccess) "자랑스러운 나를 칭찬하며 닫기" else "다시 다짐해보기",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// === 테마 선택 다이얼로그 (실시간 미리보기 제공) ===
@Composable
fun ThemeSelectorDialog(
    currentTheme: ThemeStyle,
    onThemeSelected: (ThemeStyle) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalCustomColors.current
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val Slate100 = colors.slate100
    val PastelBlue = colors.pastelBlue

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, Slate100, RoundedCornerShape(28.dp))
                .shadow(8.dp, RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(containerColor = AppleCardLight),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎨 실시간 테마 미리보기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "새로운 스타일을 클릭하면 실시간으로 배경과 전체 UI 테마가 업데이트됩니다.",
                    fontSize = 11.sp,
                    color = AppleTextGrey,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                ThemeStyle.values().forEach { style ->
                    val isSelected = style == currentTheme
                    val styleColors = when (style) {
                        ThemeStyle.PASTEL -> PastelThemeColors
                        ThemeStyle.CYBERPUNK -> CyberpunkThemeColors
                        ThemeStyle.SAGE -> SageThemeColors
                        ThemeStyle.YELLOW -> YellowThemeColors
                        ThemeStyle.SKY -> SkyThemeColors
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onThemeSelected(style) }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) PastelBlue else styleColors.slate100,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) styleColors.appleBgLight else styleColors.appleCardLight
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(styleColors.pastelBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = style.emoji, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = style.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = styleColors.appleTextDark
                                )
                                Text(
                                    text = style.desc,
                                    fontSize = 10.sp,
                                    color = styleColors.appleTextGrey,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            // Color palette dots preview
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(styleColors.pastelBlue))
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(styleColors.pastelPeach))
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(styleColors.pastelMint))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("확인 완료", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 헬퍼 UI: 캔버스 기반 프리미엄 쇼핑백 아이콘
// ==========================================
@Composable
fun ShoppingBagIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFF5E5E)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 쇼핑백 손잡이 (Arc)
        val handlePath = Path().apply {
            moveTo(width * 0.35f, height * 0.3f)
            cubicTo(
                width * 0.35f, height * 0.05f,
                width * 0.65f, height * 0.05f,
                width * 0.65f, height * 0.3f
            )
        }
        drawPath(
            path = handlePath,
            color = color.copy(alpha = 0.8f),
            style = Stroke(width = width * 0.08f)
        )

        // 쇼핑백 몸통 (둥근 직사각형)
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.3f),
            size = androidx.compose.ui.geometry.Size(width * 0.7f, height * 0.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.1f, width * 0.1f)
        )

        // 쇼핑백 가로 띠 장식
        drawRect(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.55f),
            size = androidx.compose.ui.geometry.Size(width * 0.7f, height * 0.12f)
        )
    }
}

// ==========================================
// 헬퍼 UI: 캔버스 기반 프리미엄 미니 꺾은선 차트
// ==========================================
@Composable
fun LineChartMini(
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF007AFF)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 모의 포인트들 (부드러운 주간 사용량 상승/하락 추이)
        val points = listOf(
            androidx.compose.ui.geometry.Offset(0f, height * 0.8f),
            androidx.compose.ui.geometry.Offset(width * 0.2f, height * 0.5f),
            androidx.compose.ui.geometry.Offset(width * 0.4f, height * 0.65f),
            androidx.compose.ui.geometry.Offset(width * 0.6f, height * 0.25f),
            androidx.compose.ui.geometry.Offset(width * 0.8f, height * 0.45f),
            androidx.compose.ui.geometry.Offset(width, height * 0.1f)
        )

        // 채워진 영역 그리기 (그라데이션 밑바탕)
        val fillPath = Path().apply {
            moveTo(0f, height)
            lineTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
            lineTo(width, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent)
            )
        )

        // 메인 꺾은선 그리기
        val linePath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        // 마지막 주요 포인트 점 찍기
        drawCircle(
            color = lineColor,
            radius = 7f,
            center = points.last()
        )
    }
}

// ==========================================
// 헬퍼 UI: 브랜드별 개성 넘치는 로고 엠블럼 아이콘
// ==========================================
@Composable
fun ShoppingAppBrandIcon(
    brandName: String,
    modifier: Modifier = Modifier
) {
    // 브랜드별 시그니처 색상 및 첫글자 매칭
    val (bgColor, letter) = remember(brandName) {
        val normalized = brandName.trim().lowercase()
        when {
            normalized.contains("쿠팡") || normalized.contains("coupang") -> {
                Color(0xFFE52521) to "C" // 쿠팡 레드
            }
            normalized.contains("무신사") || normalized.contains("musinsa") -> {
                Color(0xFF111111) to "M" // 무신사 블랙
            }
            normalized.contains("네이버") || normalized.contains("naver") -> {
                Color(0xFF03C75A) to "N" // 네이버 그린
            }
            normalized.contains("11번가") || normalized.contains("11st") -> {
                Color(0xFFFF3B30) to "11" // 11번가 레드
            }
            normalized.contains("지마켓") || normalized.contains("gmarket") -> {
                Color(0xFF0056FF) to "G" // 지마켓 블루
            }
            normalized.contains("옥션") || normalized.contains("auction") -> {
                Color(0xFFE60012) to "A" // 옥션 레드
            }
            normalized.contains("당근") || normalized.contains("karrot") -> {
                Color(0xFFFF7E36) to "당" // 당근 마켓 오렌지
            }
            else -> {
                // 기본값: 파스텔 랜덤 풍
                val hash = java.lang.Math.abs(brandName.hashCode())
                val colorsList = listOf(Color(0xFF5856D6), Color(0xFFFF9500), Color(0xFFFF2D55), Color(0xFF34C759))
                val selectedColor = colorsList[hash % colorsList.size]
                selectedColor to (brandName.firstOrNull()?.toString()?.uppercase() ?: "🛒")
            }
        }
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = Color.White,
            fontSize = if (letter.length > 1) 14.sp else 18.sp,
            fontWeight = FontWeight.Black
        )
    }
}

// ==========================================
// 헬퍼 UI: 캔버스 기반 카테고리별 도넛 차트
// ==========================================
@Composable
fun CategoryDonutChart(
    modifier: Modifier = Modifier,
    categories: List<Pair<String, Double>>,
    colors: List<Color>,
    centerText: String
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val rectSize = androidx.compose.ui.geometry.Size(diameter, diameter)
            val rectOffset = androidx.compose.ui.geometry.Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )

            var startAngle = -90f
            categories.forEachIndexed { index, pair ->
                val sweepAngle = 360f * (pair.second.toFloat() / 100f)
                val color = colors.getOrElse(index) { Color.Gray }
                
                // 도넛 각 조각 사이 경계 구분을 주기 위해 미세한 틈(Gap) 연출
                val gap = if (sweepAngle > 3f) 1.5f else 0f
                drawArc(
                    color = color,
                    startAngle = startAngle + gap / 2f,
                    sweepAngle = sweepAngle - gap,
                    useCenter = false,
                    topLeft = rectOffset,
                    size = rectSize,
                    style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
                )
                startAngle += sweepAngle
            }
        }
        
        // 도넛 한가운데 들어가는 총 지출 텍스트
        Text(
            text = centerText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(10.dp)
        )
    }
}

// ==========================================
// 헬퍼 UI: 캔버스 기반 상세 소비 추이 꺾은선 차트
// ==========================================
@Composable
fun ConsumptionTrendsLineChart(
    modifier: Modifier = Modifier,
    points: List<Float>, // 0f~1f 사이로 정규화된 값
    xLabels: List<String>,
    yLabels: List<String>,
    lineColor: Color = Color(0xFFE52521)
) {
    val textPainter = remember { android.graphics.Paint() }
    
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            val leftPadding = 40.dp.toPx()
            val bottomPadding = 25.dp.toPx()
            val topPadding = 15.dp.toPx()
            val rightPadding = 15.dp.toPx()
            
            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding
            
            // 1. 수평선 및 Y축 눈금선/텍스트 그리기
            val yCount = yLabels.size
            yLabels.forEachIndexed { index, label ->
                val ratio = index.toFloat() / (yCount - 1).toFloat()
                val y = topPadding + (1f - ratio) * chartHeight
                
                // 가이드 실선 그리기 (매우 투명하고 부드러운 회색)
                drawLine(
                    color = Color(0xFFE2E8F0).copy(alpha = 0.8f),
                    start = androidx.compose.ui.geometry.Offset(leftPadding, y),
                    end = androidx.compose.ui.geometry.Offset(width - rightPadding, y),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Y축 글씨 렌더링
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.apply {
                        textPainter.color = android.graphics.Color.parseColor("#94A3B8")
                        textPainter.textSize = 9.sp.toPx()
                        textPainter.textAlign = android.graphics.Paint.Align.RIGHT
                        textPainter.isAntiAlias = true
                        drawText(
                            label,
                            leftPadding - 8.dp.toPx(),
                            y + 3.dp.toPx(),
                            textPainter
                        )
                    }
                }
            }
            
            // 2. 포인트 데이터 좌표 환산
            val xCount = points.size
            val pointOffsets = points.mapIndexed { index, value ->
                val xRatio = index.toFloat() / (xCount - 1).toFloat()
                val x = leftPadding + xRatio * chartWidth
                val y = topPadding + (1f - value) * chartHeight
                androidx.compose.ui.geometry.Offset(x, y)
            }
            
            if (pointOffsets.isNotEmpty()) {
                // 3. 선 아래 영역을 그라데이션 브러시로 채우기
                val fillPath = Path().apply {
                    moveTo(pointOffsets.first().x, topPadding + chartHeight)
                    lineTo(pointOffsets.first().x, pointOffsets.first().y)
                    
                    // Bezier Curve를 사용하여 극강의 매끄러운 S자 곡선 완성
                    for (i in 0 until pointOffsets.size - 1) {
                        val p0 = pointOffsets[i]
                        val p1 = pointOffsets[i + 1]
                        val controlX1 = p0.x + (p1.x - p0.x) / 2f
                        val controlY1 = p0.y
                        val controlX2 = p0.x + (p1.x - p0.x) / 2f
                        val controlY2 = p1.y
                        cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                    }
                    
                    lineTo(pointOffsets.last().x, topPadding + chartHeight)
                    close()
                }
                
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
                
                // 4. 세련된 메인 꺾은선 그리기
                val strokePath = Path().apply {
                    moveTo(pointOffsets.first().x, pointOffsets.first().y)
                    for (i in 0 until pointOffsets.size - 1) {
                        val p0 = pointOffsets[i]
                        val p1 = pointOffsets[i + 1]
                        val controlX1 = p0.x + (p1.x - p0.x) / 2f
                        val controlY1 = p0.y
                        val controlX2 = p0.x + (p1.x - p0.x) / 2f
                        val controlY2 = p1.y
                        cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                    }
                }
                
                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                
                // 5. 포인트마다 세련된 미적 원형 마커 덧칠하기
                pointOffsets.forEach { offset ->
                    // 메인 다홍 원
                    drawCircle(
                        color = lineColor,
                        radius = 5.dp.toPx(),
                        center = offset
                    )
                    // 중심 하얀 점
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = offset
                    )
                }
            }
            
            // 6. X축 눈금 레이블 그리기
            xLabels.forEachIndexed { index, label ->
                val xRatio = index.toFloat() / (xCount - 1).toFloat()
                val x = leftPadding + xRatio * chartWidth
                val y = topPadding + chartHeight + 16.dp.toPx()
                
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.apply {
                        textPainter.color = android.graphics.Color.parseColor("#94A3B8")
                        textPainter.textSize = 9.sp.toPx()
                        textPainter.textAlign = android.graphics.Paint.Align.CENTER
                        textPainter.isAntiAlias = true
                        drawText(
                            label,
                            x,
                            y,
                            textPainter
                        )
                    }
                }
            }
        }
    }
}

// === 실시간 쇼핑 세션 감시 다이얼로그 ===
@Composable
fun ActiveShoppingSessionDialog(
    app: ShoppingApp,
    onAddMinute: () -> Unit,
    onStop: () -> Unit
) {
    val colors = LocalCustomColors.current
    val AppleCardLight = colors.appleCardLight
    val AppleTextDark = colors.appleTextDark
    val AppleTextGrey = colors.appleTextGrey
    val PastelBlue = colors.pastelBlue
    val PastelRed = colors.pastelRed

    val remainingMinutes = (app.dailyLimitMinutes - app.usedMinutesToday).coerceAtLeast(0)
    val progress = if (app.dailyLimitMinutes > 0) {
        (app.usedMinutesToday.toFloat() / app.dailyLimitMinutes.toFloat()).coerceIn(0f, 1f)
    } else 1f

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = AppleCardLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(12.dp, RoundedCornerShape(28.dp))
                .border(2.dp, PastelRed.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 상단 감시 경고 뱃지
                Box(
                    modifier = Modifier
                        .background(PastelRed.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PastelRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "실시간 쇼핑 감시 작동 중",
                            color = PastelRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                ShoppingAppLogo(appName = app.name, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${app.name} 쇼핑 중",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = AppleTextDark
                )
                Text(
                    text = app.category,
                    fontSize = 12.sp,
                    color = AppleTextGrey
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 남은 시간 원형 링
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(120.dp)
                ) {
                    CircularProgressIndicator(
                        progress = 1f,
                        color = Color(0xFFF1F5F9),
                        strokeWidth = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    CircularProgressIndicator(
                        progress = progress,
                        color = if (progress >= 0.8f) PastelRed else PastelBlue,
                        strokeWidth = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$remainingMinutes",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = if (progress >= 0.8f) PastelRed else AppleTextDark
                        )
                        Text(
                            text = "분 남음",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextGrey
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 현재 사용량
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "오늘 누적 사용:",
                        fontSize = 13.sp,
                        color = AppleTextGrey,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${app.usedMinutesToday}분 / ${app.dailyLimitMinutes}분",
                        fontSize = 13.sp,
                        color = AppleTextDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = progress,
                    color = if (progress >= 0.8f) PastelRed else PastelBlue,
                    trackColor = Color(0xFFF1F5F9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 조작 버튼 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 시간 빨리 감기 테스트용 버튼
                    Button(
                        onClick = onAddMinute,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("⏳ +1분 (가상)", color = AppleTextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // 쇼핑 중단하기
                    Button(
                        onClick = onStop,
                        colors = ButtonDefaults.buttonColors(containerColor = PastelRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🛑 쇼핑 종료", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


