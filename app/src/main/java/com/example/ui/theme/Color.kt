package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.staticCompositionLocalOf

// === Theme Style Enum ===
enum class ThemeStyle(val title: String, val desc: String, val emoji: String) {
    PASTEL("크리미 파스텔", "포근한 아이보리 & 인디고 블루 감성", "🌸"),
    CYBERPUNK("네온 다크", "화려하고 몰입감 넘치는 사이버 펑크", "⚡"),
    SAGE("미니멀 세이지", "북유럽풍의 차분한 세이지 그린 힐링", "🌿"),
    YELLOW("화사한 개나리", "봄날 개나리처럼 따스한 노란빛 감성", "🌼"),
    SKY("맑은 스카이", "맑고 청량한 가을 하늘빛 감성", "☁️")
}

// === Custom Theme Colors Struct ===
data class CustomColors(
    val appleBgLight: Color,
    val appleCardLight: Color,
    val appleTextDark: Color,
    val appleTextGrey: Color,
    val pastelBlue: Color,
    val pastelPeach: Color,
    val pastelMint: Color,
    val pastelYellow: Color,
    val pastelRed: Color,
    val bgGradientStart: Color,
    val bgGradientEnd: Color,
    val slate100: Color,
    val slate200: Color
)

// === Predefined Theme Color Configs ===
val PastelThemeColors = CustomColors(
    appleBgLight = Color(0xFFFDFCFB),
    appleCardLight = Color(0xFFFFFFFF),
    appleTextDark = Color(0xFF1E293B),
    appleTextGrey = Color(0xFF64748B),
    pastelBlue = Color(0xFF6366F1),
    pastelPeach = Color(0xFFF43F5E),
    pastelMint = Color(0xFF10B981),
    pastelYellow = Color(0xFFF59E0B),
    pastelRed = Color(0xFFEF4444),
    bgGradientStart = Color(0xFFEEF2FF),
    bgGradientEnd = Color(0xFFFFF1F2),
    slate100 = Color(0xFFF1F5F9),
    slate200 = Color(0xFFE2E8F0)
)

val CyberpunkThemeColors = CustomColors(
    appleBgLight = Color(0xFF0F172A),
    appleCardLight = Color(0xFF1E293B),
    appleTextDark = Color(0xFFF8FAFC),
    appleTextGrey = Color(0xFF94A3B8),
    pastelBlue = Color(0xFF38BDF8),
    pastelPeach = Color(0xFFF43F5E),
    pastelMint = Color(0xFF4ADE80),
    pastelYellow = Color(0xFFFACC15),
    pastelRed = Color(0xFFF87171),
    bgGradientStart = Color(0xFF1E1B4B),
    bgGradientEnd = Color(0xFF311042),
    slate100 = Color(0xFF334155),
    slate200 = Color(0xFF475569)
)

val SageThemeColors = CustomColors(
    appleBgLight = Color(0xFFF4F6F4),
    appleCardLight = Color(0xFFFFFFFF),
    appleTextDark = Color(0xFF2C3E35),
    appleTextGrey = Color(0xFF7A8D84),
    pastelBlue = Color(0xFF6B8A7A),
    pastelPeach = Color(0xFFD3756B),
    pastelMint = Color(0xFF8CA194),
    pastelYellow = Color(0xFFE5BA73),
    pastelRed = Color(0xFFC85C5C),
    bgGradientStart = Color(0xFFEAF0EC),
    bgGradientEnd = Color(0xFFF3EFE9),
    slate100 = Color(0xFFE6EAE7),
    slate200 = Color(0xFFD6DDD8)
)

val YellowThemeColors = CustomColors(
    appleBgLight = Color(0xFFFFFDF0),
    appleCardLight = Color(0xFFFFFFFF),
    appleTextDark = Color(0xFF451A03),
    appleTextGrey = Color(0xFF78350F),
    pastelBlue = Color(0xFFEAB308), // 화사한 노란 개나리빛
    pastelPeach = Color(0xFFF43F5E),
    pastelMint = Color(0xFF10B981),
    pastelYellow = Color(0xFFF59E0B),
    pastelRed = Color(0xFFEF4444),
    bgGradientStart = Color(0xFFFEFEE8),
    bgGradientEnd = Color(0xFFFFFBEB),
    slate100 = Color(0xFFFEF08A),
    slate200 = Color(0xFFFDE047)
)

val SkyThemeColors = CustomColors(
    appleBgLight = Color(0xFFF0F9FF),
    appleCardLight = Color(0xFFFFFFFF),
    appleTextDark = Color(0xFF0369A1),
    appleTextGrey = Color(0xFF0284C7),
    pastelBlue = Color(0xFF0EA5E9), // 시원한 스카이 블루
    pastelPeach = Color(0xFFF43F5E),
    pastelMint = Color(0xFF10B981),
    pastelYellow = Color(0xFFF59E0B),
    pastelRed = Color(0xFFEF4444),
    bgGradientStart = Color(0xFFE0F2FE),
    bgGradientEnd = Color(0xFFF0F9FF),
    slate100 = Color(0xFFBAE6FD),
    slate200 = Color(0xFF7DD3FC)
)

// === CompositionLocal for Custom Colors ===
val LocalCustomColors = staticCompositionLocalOf { PastelThemeColors }

// === Default static references (maintained for backwards-compatibility & preview fallback) ===
val AppleBgLight = Color(0xFFFDFCFB)     // 따뜻한 샌드/크림 아이폰 홈 배경색
val AppleCardLight = Color(0xFFFFFFFF)   // 아이폰 스타일의 무결점 화이트 카드
val AppleTextDark = Color(0xFF1E293B)    // Slate-800 가독성 최고조의 텍스트색
val AppleTextGrey = Color(0xFF64748B)    // Slate-500 부제목/설명 보조 텍스트색

val PastelBlue = Color(0xFF6366F1)       // Indigo-500 메인 테마 컬러
val PastelPeach = Color(0xFFF43F5E)      // Rose-500 경고/한도초과 포인트 컬러
val PastelMint = Color(0xFF10B981)       // Emerald-500 절약 및 성공 컬러
val PastelYellow = Color(0xFFF59E0B)     // Amber-500 조작 경고 알림 컬러
val PastelRed = Color(0xFFEF4444)        // Red-500 긴급 경고 컬러

val Indigo50 = Color(0xFFEEF2FF)         // 소프트 인디고 배경용 그라데이션 시작
val Rose50 = Color(0xFFFFF1F2)           // 소프트 로즈 배경용 그라데이션 끝
val Slate100 = Color(0xFFF1F5F9)         // 테두리용 미세한 슬레이트 그레이
val Slate200 = Color(0xFFE2E8F0)         // 구분선용 슬레이트 그레이

val Purple80 = Color(0xFF6366F1)
val PurpleGrey80 = Color(0xFF94A3B8)
val Pink80 = Color(0xFFF43F5E)

val Purple40 = Color(0xFF6366F1)
val PurpleGrey40 = Color(0xFF64748B)
val Pink40 = Color(0xFFF43F5E)


