package com.example.ui

object Localization {
    fun t(key: String, isEnglish: Boolean): String {
        require(key.isNotEmpty()) { "Translation key cannot be empty" }
        return if (isEnglish) {
            translationsEn[key] ?: key.also {
                android.util.Log.w("Localization", "Missing translation for key: $key (English)")
            }
        } else {
            translationsKo[key] ?: key.also {
                android.util.Log.w("Localization", "Missing translation for key: $key (Korean)")
            }
        }
    }

    private val translationsKo = mapOf(
        "app_name" to "쇼핑그만",
        "tab_dashboard" to "대시보드",
        "tab_apps" to "통계",
        "tab_settings" to "설정",
        
        // Dashboard Screen
        "today_savings_status" to "오늘의 저축 현황",
        "savings_vs_budget" to "이번 달 예산 대비 절약액",
        "total_time_used" to "오늘 총 사용한 시간",
        "locked_apps" to "잠금 상태인 쇼핑앱",
        "weekly_stats" to "주간 쇼핑 제어 통계",
        "recent_history" to "최근 소비 억제 이력",
        "no_history" to "최근 소비 억제 이력이 없습니다.",
        "refresh_quote" to "새로운 자극 받기",
        "active_session" to "실시간 쇼핑 세션 제어",
        "running_session" to "실시간 감시 세션 작동 중",
        "start_monitoring" to "앱 자동 감시 세션 시작하기",
        "stop_monitoring" to "감시 세션 강제 종료하기",
        "save_confirm_msg" to "원어절약 성공!",
        "today_consumption_summary" to "오늘의 소비 요약",
        "more" to "더보기",
        "spent_amount" to "지출 금액",
        "registered_shopping_apps" to "등록된 쇼핑 앱",
        "manage" to "관리",
        "todays_quote" to "오늘의 한마디",
        "budget" to "예산",
        
        // Statistics Screen
        "consumption_stats" to "소비 제어 통계",
        "weekly_savings_stats" to "주간 소비 제어 통계",
        "cum_savings_amt" to "이번 달 누적 절약 금액",
        "shopping_block_counts" to "요일별 쇼핑 방어 횟수",
        "consumption_tendency" to "쇼핑 카테고리별 소비 성향",
        "recent_block_history" to "최근 방어 이력",
        "no_block_history" to "아직 방어된 충동구매 이력이 없습니다.",
        "total_spent" to "총 지출",
        "over_budget_badge" to "+%,d원\n(예산 초과)",
        "avg_daily_spent" to "평균 일 지출",
        "vs_last_month" to "지난 달 대비",
        "shopping_count" to "쇼핑 횟수",
        "times_unit" to "회",
        "category_spent" to "카테고리별 지출",
        "line_chart_tab_day" to "일",
        "line_chart_tab_week" to "주",
        "line_chart_tab_month" to "월",
        "trend_chart_title" to "소비 추이",
        "break_history" to "브레이크 히스토리",
        "reset_all" to "전체 리셋",
        "no_break_history_desc" to "아직 브레이크 내역이 없습니다.",
        "cat_fashion" to "패션/의류",
        "cat_life" to "생활/건강",
        "cat_digital" to "전자기기",
        "cat_food" to "식품",
        "cat_etc" to "기타",
        "history_saved_desc" to "절약 성공",
        "history_spent_desc" to "소비 발생",
        "history_saved_badge" to "절약 성공! 💚",
        "history_spent_badge" to "결제 지름 💸",
        "history_resolution_prefix" to "다짐",
        "dashboard_breaker_advice" to "오늘의 충동구매 브레이커 조언",

        // Settings Screen
        "settings" to "설정",
        "manage_apps" to "쇼핑 앱 관리",
        "no_apps" to "등록된 제한 쇼핑앱이 없습니다.",
        "add_app" to "앱 추가하기",
        "notif_settings" to "알림 설정",
        "monthly_budget" to "월간 쇼핑 권장 예산",
        "budget_limit" to "예산 관리 기준",
        "daily_limit" to "일일 사용 한도 시간",
        "late_night_warning" to "심야 시간 쇼핑 경고 알림",
        "reset_system" to "시스템 초기화",
        "reset_btn" to "초기화하기",
        "select_theme" to "안드로이드 테마 선택",
        "change_theme" to "테마 변경",
        "run_brake_test" to "결제 브레이크 테스트",
        "brake_test_desc" to "가상 결제창 상황에서 충동구매 억제력을 기르는 모의 훈련을 진행합니다.",
        "run_test" to "브레이크 훈련 시작",
        "lang_setting" to "언어 설정 (Language)",
        "lang_toggle_desc" to "한국어와 English 간 언어를 변경합니다.",
        
        // Dialogs & Buttons
        "select_app_category" to "카테고리 선택",
        "app_limit_time" to "제한 시간 설정(분)",
        "cancel" to "취소",
        "confirm" to "확인",
        "add" to "추가",
        "delete" to "삭제",
        "save" to "저장",
        "register" to "등록하기",
        "close" to "닫기",
        
        // General Units & Terms
        "won" to "원",
        "minute" to "분",
        "hour" to "시간",
        "day" to "일",
        "over" to "초과"
    )

    private val translationsEn = mapOf(
        "app_name" to "Stop Shopping",
        "tab_dashboard" to "Dashboard",
        "tab_apps" to "Statistics",
        "tab_settings" to "Settings",
        
        // Dashboard Screen
        "today_savings_status" to "Today's Savings",
        "savings_vs_budget" to "Savings vs. Budget This Month",
        "total_time_used" to "Total Time Used Today",
        "locked_apps" to "Locked Shopping Apps",
        "weekly_stats" to "Weekly Control Stats",
        "recent_history" to "Recent Impulse Blocks",
        "no_history" to "No recent block history.",
        "refresh_quote" to "Get New Motivation",
        "active_session" to "Active Shopping Session Control",
        "running_session" to "Active monitoring session running",
        "start_monitoring" to "Start App Auto-Monitoring Session",
        "stop_monitoring" to "Force Terminate Monitoring Session",
        "save_confirm_msg" to "Saved successfully!",
        "today_consumption_summary" to "Today's Spent Summary",
        "more" to "More",
        "spent_amount" to "Spent Amount",
        "registered_shopping_apps" to "Registered Shopping Apps",
        "manage" to "Manage",
        "todays_quote" to "Daily Quote",
        "budget" to "Budget",
        
        // Statistics Screen
        "consumption_stats" to "Consumption Control Stats",
        "weekly_savings_stats" to "Weekly Savings Stats",
        "cum_savings_amt" to "Cumulative Savings (This Month)",
        "shopping_block_counts" to "Shopping Block Counts by Day",
        "consumption_tendency" to "Shopping Category Tendencies",
        "recent_block_history" to "Recent Block History",
        "no_block_history" to "No impulse purchase blocked yet.",
        "total_spent" to "Total Spent",
        "over_budget_badge" to "+%,d KRW\n(Over Budget)",
        "avg_daily_spent" to "Avg. Daily Spent",
        "vs_last_month" to "vs Last Month",
        "shopping_count" to "Shopping Trips",
        "times_unit" to "times",
        "category_spent" to "Spent by Category",
        "line_chart_tab_day" to "Day",
        "line_chart_tab_week" to "Week",
        "line_chart_tab_month" to "Month",
        "trend_chart_title" to "Spending Trend",
        "break_history" to "Brake History",
        "reset_all" to "Reset All",
        "no_break_history_desc" to "No brake history yet.",
        "cat_fashion" to "Fashion/Apparel",
        "cat_life" to "Life/Health",
        "cat_digital" to "Electronics",
        "cat_food" to "Food",
        "cat_etc" to "Others",
        "history_saved_desc" to "Savings Success",
        "history_spent_desc" to "Spent",
        "history_saved_badge" to "Savings Success! 💚",
        "history_spent_badge" to "Paid Impulse 💸",
        "history_resolution_prefix" to "Resolution",
        "dashboard_breaker_advice" to "Impulse Breaker Advice",

        // Settings Screen
        "settings" to "Settings",
        "manage_apps" to "Manage Shopping Apps",
        "no_apps" to "No restricted shopping apps registered.",
        "add_app" to "Add Shopping App",
        "notif_settings" to "Notification Settings",
        "monthly_budget" to "Monthly Shopping Budget",
        "budget_limit" to "Budget Alert Setting",
        "daily_limit" to "Daily Usage Limit",
        "late_night_warning" to "Late Night Shopping Warning",
        "reset_system" to "Reset System",
        "reset_btn" to "Reset Now",
        "select_theme" to "Select Android Theme",
        "change_theme" to "Change Theme",
        "run_brake_test" to "Run Payment Brake Test",
        "brake_test_desc" to "Practice resisting impulsive purchases under mock checkout situations.",
        "run_test" to "Start Brake Training",
        "lang_setting" to "Language Settings",
        "lang_toggle_desc" to "Switch language between English and Korean.",
        
        // Dialogs & Buttons
        "select_app_category" to "Select Category",
        "app_limit_time" to "Limit Time (Minutes)",
        "cancel" to "Cancel",
        "confirm" to "Confirm",
        "add" to "Add",
        "delete" to "Delete",
        "save" to "Save",
        "register" to "Register",
        "close" to "Close",
        
        // General Units & Terms
        "won" to "KRW",
        "minute" to "m",
        "hour" to "h",
        "day" to "day",
        "over" to "over"
    )
}
