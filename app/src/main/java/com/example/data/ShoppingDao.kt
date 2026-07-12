package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    // === 쇼핑앱 관련 쿼리 ===
    @Query("SELECT * FROM shopping_apps ORDER BY name ASC")
    fun getAllShoppingApps(): Flow<List<ShoppingApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingApp(app: ShoppingApp)

    @Update
    suspend fun updateShoppingApp(app: ShoppingApp)

    @Delete
    suspend fun deleteShoppingApp(app: ShoppingApp)

    @Query("UPDATE shopping_apps SET usedMinutesToday = :minutes WHERE id = :id")
    suspend fun updateUsedMinutes(id: Int, minutes: Int)

    @Query("UPDATE shopping_apps SET isLocked = :isLocked WHERE id = :id")
    suspend fun updateAppLockState(id: Int, isLocked: Boolean)

    // === 충동구매 차단 기록 관련 쿼리 ===
    @Query("SELECT * FROM blocked_impulses ORDER BY timestamp DESC")
    fun getAllBlockedImpulses(): Flow<List<BlockedImpulse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedImpulse(impulse: BlockedImpulse)

    @Query("DELETE FROM blocked_impulses WHERE id = :id")
    suspend fun deleteBlockedImpulseById(id: Int)

    @Query("DELETE FROM blocked_impulses")
    suspend fun clearAllBlockedImpulses()
}
