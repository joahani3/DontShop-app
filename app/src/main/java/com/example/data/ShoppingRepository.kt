package com.example.data

import android.util.Log
import kotlinx.coroutines.flow.Flow

class ShoppingRepository(private val shoppingDao: ShoppingDao) {
    private val TAG = "ShoppingRepository"

    // === 쇼핑앱 관련 ===
    val allShoppingApps: Flow<List<ShoppingApp>> = shoppingDao.getAllShoppingApps()

    suspend fun insertShoppingApp(app: ShoppingApp) {
        try {
            require(app.name.isNotEmpty()) { "App name cannot be empty" }
            require(app.dailyLimitMinutes > 0) { "Daily limit must be positive" }
            shoppingDao.insertShoppingApp(app)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert shopping app", e)
            throw e
        }
    }

    suspend fun updateShoppingApp(app: ShoppingApp) {
        try {
            require(app.name.isNotEmpty()) { "App name cannot be empty" }
            shoppingDao.updateShoppingApp(app)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update shopping app", e)
            throw e
        }
    }

    suspend fun deleteShoppingApp(app: ShoppingApp) {
        try {
            shoppingDao.deleteShoppingApp(app)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete shopping app", e)
            throw e
        }
    }

    suspend fun updateUsedMinutes(id: Int, minutes: Int) {
        try {
            require(id > 0) { "Invalid app id" }
            require(minutes >= 0) { "Minutes cannot be negative" }
            shoppingDao.updateUsedMinutes(id, minutes)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update used minutes for id: $id", e)
            throw e
        }
    }

    suspend fun updateAppLockState(id: Int, isLocked: Boolean) {
        try {
            require(id > 0) { "Invalid app id" }
            shoppingDao.updateAppLockState(id, isLocked)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update lock state for id: $id", e)
            throw e
        }
    }

    // === 충동구매 차단 기록 관련 ===
    val allBlockedImpulses: Flow<List<BlockedImpulse>> = shoppingDao.getAllBlockedImpulses()

    suspend fun insertBlockedImpulse(impulse: BlockedImpulse) {
        try {
            require(impulse.itemName.isNotEmpty()) { "Item name cannot be empty" }
            require(impulse.itemPrice > 0) { "Item price must be positive" }
            require(impulse.resolutionSelected.isNotEmpty()) { "Resolution cannot be empty" }
            shoppingDao.insertBlockedImpulse(impulse)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert blocked impulse", e)
            throw e
        }
    }

    suspend fun deleteBlockedImpulseById(id: Int) {
        try {
            require(id > 0) { "Invalid impulse id" }
            shoppingDao.deleteBlockedImpulseById(id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete blocked impulse id: $id", e)
            throw e
        }
    }

    suspend fun clearAllBlockedImpulses() {
        try {
            shoppingDao.clearAllBlockedImpulses()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all blocked impulses", e)
            throw e
        }
    }
}
