package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_items ORDER BY updatedTime DESC")
    fun getAllItems(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE isFavorite = 1 ORDER BY updatedTime DESC")
    fun getFavoriteItems(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE content LIKE '%' || :query || '%' ORDER BY updatedTime DESC")
    fun searchItems(query: String): Flow<List<ClipboardItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ClipboardItem): Long

    @Update
    suspend fun updateItem(item: ClipboardItem)

    @Query("DELETE FROM clipboard_items WHERE id = :id")
    suspend fun deleteItemById(id: Int)

    @Query("DELETE FROM clipboard_items WHERE id IN (:ids)")
    suspend fun deleteItemsByIds(ids: List<Int>)

    @Query("DELETE FROM clipboard_items")
    suspend fun deleteAllItems()
    
    @Query("SELECT * FROM clipboard_items ORDER BY updatedTime DESC LIMIT 1")
    suspend fun getLatestItem(): ClipboardItem?
    
    @Query("SELECT content FROM clipboard_items ORDER BY updatedTime DESC LIMIT 1")
    fun getLatestContentFlow(): Flow<String?>
    
    @Query("SELECT COUNT(*) FROM clipboard_items")
    suspend fun getCount(): Int
    
    @Query("DELETE FROM clipboard_items WHERE id NOT IN (SELECT id FROM clipboard_items ORDER BY updatedTime DESC LIMIT :limit)")
    suspend fun deleteOldestExceeding(limit: Int)
}
