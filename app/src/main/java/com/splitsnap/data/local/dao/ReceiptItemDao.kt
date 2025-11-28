package com.splitsnap.data.local.dao

import androidx.room.*
import com.splitsnap.data.local.entity.ReceiptItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptItemDao {
    @Query("SELECT * FROM receipt_items WHERE receiptId = :receiptId")
    fun getItemsByReceiptId(receiptId: String): Flow<List<ReceiptItemEntity>>

    @Query("SELECT * FROM receipt_items WHERE receiptId = :receiptId")
    suspend fun getItemsByReceiptIdSync(receiptId: String): List<ReceiptItemEntity>

    @Query("SELECT * FROM receipt_items WHERE id = :id")
    suspend fun getItemById(id: String): ReceiptItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ReceiptItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ReceiptItemEntity>)

    @Update
    suspend fun updateItem(item: ReceiptItemEntity)

    @Delete
    suspend fun deleteItem(item: ReceiptItemEntity)

    @Query("DELETE FROM receipt_items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("DELETE FROM receipt_items WHERE receiptId = :receiptId")
    suspend fun deleteItemsByReceiptId(receiptId: String)
}
