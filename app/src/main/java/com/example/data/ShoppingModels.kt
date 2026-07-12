package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 쇼핑앱 데이터 엔티티
 * @property id 고유 ID
 * @property name 쇼핑 앱 이름 (예: 쿠팡, 무신사)
 * @property dailyLimitMinutes 하루 사용 제한 시간 (분 단위)
 * @property usedMinutesToday 오늘 사용한 시간 (분 단위)
 * @property isLocked 앱별 잠금 활성화 여부
 * @property packageName 앱 패키지명 또는 대표 식별자
 * @property category 쇼핑 카테고리 (종합 쇼핑, 패션, 해외직구 등)
 */
@Entity(tableName = "shopping_apps")
data class ShoppingApp(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dailyLimitMinutes: Int = 30,
    val usedMinutesToday: Int = 0,
    val isLocked: Boolean = false,
    val packageName: String = "",
    val category: String = "일반 쇼핑"
)

/**
 * 충동구매 차단 기록 엔티티 (결제 제어 기록)
 * @property id 고유 ID
 * @property itemName 결제하려던 상품명
 * @property itemPrice 상품 가격 (원 단위)
 * @property resolutionSelected 선택하거나 작성한 나의 다짐 문구
 * @property isBlocked 충동구매 차단 성공 여부 (true = 구매 안 하고 절약 성공, false = 구매 강행)
 * @property timestamp 기록 일시
 */
@Entity(tableName = "blocked_impulses")
data class BlockedImpulse(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemName: String,
    val itemPrice: Int,
    val resolutionSelected: String,
    val isBlocked: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
