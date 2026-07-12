package com.example.data

import kotlinx.coroutines.flow.Flow

class ShoppingRepository(private val shoppingDao: ShoppingDao) {

    // === 쇼핑앱 관련 ===
    val allShoppingApps: Flow<List<ShoppingApp>> = shoppingDao.getAllShoppingApps()

    suspend fun insertShoppingApp(app: ShoppingApp) {
        shoppingDao.insertShoppingApp(app)
    }

    suspend fun updateShoppingApp(app: ShoppingApp) {
        shoppingDao.updateShoppingApp(app)
    }

    suspend fun deleteShoppingApp(app: ShoppingApp) {
        shoppingDao.deleteShoppingApp(app)
    }

    suspend fun updateUsedMinutes(id: Int, minutes: Int) {
        shoppingDao.updateUsedMinutes(id, minutes)
    }

    suspend fun updateAppLockState(id: Int, isLocked: Boolean) {
        shoppingDao.updateAppLockState(id, isLocked)
    }

    // === 충동구매 차단 기록 관련 ===
    val allBlockedImpulses: Flow<List<BlockedImpulse>> = shoppingDao.getAllBlockedImpulses()

    suspend fun insertBlockedImpulse(impulse: BlockedImpulse) {
        shoppingDao.insertBlockedImpulse(impulse)
    }

    suspend fun deleteBlockedImpulseById(id: Int) {
        shoppingDao.deleteBlockedImpulseById(id)
    }

    suspend fun clearAllBlockedImpulses() {
        shoppingDao.clearAllBlockedImpulses()
    }
}
