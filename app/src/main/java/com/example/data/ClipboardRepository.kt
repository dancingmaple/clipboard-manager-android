package com.example.data

import kotlinx.coroutines.flow.Flow

class ClipboardRepository(private val dao: ClipboardDao) {
    val allItems: Flow<List<ClipboardItem>> = dao.getAllItems()
    val favoriteItems: Flow<List<ClipboardItem>> = dao.getFavoriteItems()

    fun searchItems(query: String): Flow<List<ClipboardItem>> {
        return dao.searchItems(query)
    }

    suspend fun insertOrUpdate(content: String, isAutoDeduplicate: Boolean, maxItems: Int = 1000) {
        if (content.isBlank()) return
        
        val latest = dao.getLatestItem()
        if (isAutoDeduplicate && latest?.content == content) {
            // Already the top item, just update time
            dao.updateItem(latest.copy(updatedTime = System.currentTimeMillis()))
            return
        }
        
        dao.insertItem(
            ClipboardItem(
                content = content,
                createdTime = System.currentTimeMillis(),
                updatedTime = System.currentTimeMillis()
            )
        )
        
        if (maxItems > 0 && dao.getCount() > maxItems) {
            dao.deleteOldestExceeding(maxItems)
        }
    }
    
    suspend fun updateItem(item: ClipboardItem) {
        dao.updateItem(item.copy(updatedTime = System.currentTimeMillis()))
    }
    
    suspend fun deleteItemById(id: Int) {
        dao.deleteItemById(id)
    }
    
    suspend fun deleteItemsByIds(ids: List<Int>) {
        dao.deleteItemsByIds(ids)
    }
    
    suspend fun deleteAll() {
        dao.deleteAllItems()
    }
}
