package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ShoppingApp::class, BlockedImpulse::class], version = 1, exportSchema = false)
abstract class ShoppingDatabase : RoomDatabase() {
    abstract fun shoppingDao(): ShoppingDao

    companion object {
        @Volatile
        private var INSTANCE: ShoppingDatabase? = null

        fun getDatabase(context: Context, coroutineScope: CoroutineScope): ShoppingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShoppingDatabase::class.java,
                    "shopping_stop_database"
                )
                .enableWriteAheadLogging()
                .addCallback(ShoppingDatabaseCallback(coroutineScope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class ShoppingDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.shoppingDao())
                }
            }
        }

        suspend fun populateDatabase(dao: ShoppingDao) {
            // 기본 한국 인기 쇼핑앱 데이터 프리로드 (충동구매 제한 대상)
            dao.insertShoppingApp(ShoppingApp(name = "쿠팡 (Coupang)", dailyLimitMinutes = 20, usedMinutesToday = 8, isLocked = false, packageName = "com.coupang.mobile", category = "종합 쇼핑"))
            dao.insertShoppingApp(ShoppingApp(name = "무신사 (Musinsa)", dailyLimitMinutes = 15, usedMinutesToday = 14, isLocked = false, packageName = "com.musinsa.store", category = "패션/뷰티"))
            dao.insertShoppingApp(ShoppingApp(name = "네이버 쇼핑", dailyLimitMinutes = 30, usedMinutesToday = 25, isLocked = false, packageName = "com.nhn.android.search", category = "종합 쇼핑"))
            dao.insertShoppingApp(ShoppingApp(name = "알리익스프레스", dailyLimitMinutes = 10, usedMinutesToday = 3, isLocked = true, packageName = "com.alibaba.aliexpress", category = "해외 직구"))
            dao.insertShoppingApp(ShoppingApp(name = "에이블리 (Ably)", dailyLimitMinutes = 15, usedMinutesToday = 0, isLocked = false, packageName = "com.ably.retail", category = "패션/뷰티"))

            val now = System.currentTimeMillis()
            val oneDay = 86400000L

            // --- 5월 역사 데이터 (약 60일 전) ---
            dao.insertBlockedImpulse(BlockedImpulse(
                itemName = "갤럭시 탭 S10 Ultra",
                itemPrice = 1250000,
                resolutionSelected = "내 미래를 위해 이 돈을 저축하거나 투자하자.",
                isBlocked = true,
                timestamp = now - oneDay * 62
            ))
            dao.insertBlockedImpulse(BlockedImpulse(
                itemName = "디자이너 브랜드 봄 자켓",
                itemPrice = 180000,
                resolutionSelected = "장바구니에 담아두고 24시간 생각하자.",
                isBlocked = false, // 구매 강행 (5월 소비)
                timestamp = now - oneDay * 60
            ))
            dao.insertBlockedImpulse(BlockedImpulse(
                itemName = "가정용 오메가3 영양제 세트",
                itemPrice = 85000,
                resolutionSelected = "진짜 건강에 필수적인지 냉정하게 따지자.",
                isBlocked = false, // 구매 강행 (5월 소비)
                timestamp = now - oneDay * 58
            ))

            // --- 6월 역사 데이터 (약 30일 전) ---
            dao.insertBlockedImpulse(BlockedImpulse(
                itemName = "노이즈캔슬링 무선 헤드폰",
                itemPrice = 380000,
                resolutionSelected = "물건을 사서 느껴지는 도파민 행복은 단 3일뿐이다.",
                isBlocked = true,
                timestamp = now - oneDay * 32
            ))
            dao.insertBlockedImpulse(BlockedImpulse(
                itemName = "프리미엄 런닝화",
                itemPrice = 169000,
                resolutionSelected = "이번 달 예산을 초과하면 다음 달의 내가 괴롭다.",
                isBlocked = false, // 구매 강행 (6월 소비)
                timestamp = now - oneDay * 28
            ))
            dao.insertBlockedImpulse(BlockedImpulse(
                itemName = "헬스 단백질 파우더 대용량",
                itemPrice = 72000,
                resolutionSelected = "집에 남은 식자재와 단백질 식품을 먼저 소비하자.",
                isBlocked = false, // 구매 강행 (6월 소비)
                timestamp = now - oneDay * 25
            ))

            // --- 7월 역사 데이터 (최근 일주일) ---
            dao.insertBlockedImpulse(BlockedImpulse(
                itemName = "아이폰 17 프로",
                itemPrice = 1550000,
                resolutionSelected = "내 미래를 위해 이 돈을 저축하거나 투자하자.",
                isBlocked = true,
                timestamp = now - oneDay * 2
            ))
            dao.insertBlockedImpulse(BlockedImpulse(
                itemName = "충동구매 크롭 가죽 자켓",
                itemPrice = 189000,
                resolutionSelected = "진짜 필요한 것인지 24시간 뒤에 다시 생각하자.",
                isBlocked = true,
                timestamp = now - oneDay * 1
            ))
            dao.insertBlockedImpulse(BlockedImpulse(
                itemName = "배달 야식 피자세트",
                itemPrice = 32000,
                resolutionSelected = "물건을 사서 느끼는 행복은 단 3일뿐이다.",
                isBlocked = false, // 구매 강행 (7월 소비)
                timestamp = now - 3600000L * 4
            ))
            dao.insertBlockedImpulse(BlockedImpulse(
                itemName = "커스텀 기계식 마우스",
                itemPrice = 110000,
                resolutionSelected = "현재 마우스가 멀쩡히 작동하므로 더 참자.",
                isBlocked = false, // 구매 강행 (7월 소비)
                timestamp = now - oneDay * 4
            ))
            dao.insertBlockedImpulse(BlockedImpulse(
                itemName = "브랜드 여름 반팔티 3장",
                itemPrice = 75000,
                resolutionSelected = "옷장에 안 입는 여름 옷들을 먼저 정리하자.",
                isBlocked = false, // 구매 강행 (7월 소비)
                timestamp = now - oneDay * 5
            ))
        }
    }
}
