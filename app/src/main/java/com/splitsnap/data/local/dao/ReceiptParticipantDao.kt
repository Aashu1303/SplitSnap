package com.splitsnap.data.local.dao

import androidx.room.*
import com.splitsnap.data.local.entity.PersonEntity
import com.splitsnap.data.local.entity.ReceiptParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptParticipantDao {
    @Query("SELECT * FROM receipt_participants WHERE receiptId = :receiptId")
    fun getParticipantsByReceiptId(receiptId: String): Flow<List<ReceiptParticipantEntity>>

    @Query("SELECT * FROM receipt_participants WHERE receiptId = :receiptId")
    suspend fun getParticipantsByReceiptIdSync(receiptId: String): List<ReceiptParticipantEntity>

    @Query("""
        SELECT p.* FROM people p 
        INNER JOIN receipt_participants rp ON p.id = rp.personId 
        WHERE rp.receiptId = :receiptId
    """)
    fun getPeopleByReceiptId(receiptId: String): Flow<List<PersonEntity>>

    @Query("""
        SELECT p.* FROM people p 
        INNER JOIN receipt_participants rp ON p.id = rp.personId 
        WHERE rp.receiptId = :receiptId
    """)
    suspend fun getPeopleByReceiptIdSync(receiptId: String): List<PersonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: ReceiptParticipantEntity)

    @Delete
    suspend fun deleteParticipant(participant: ReceiptParticipantEntity)

    @Query("DELETE FROM receipt_participants WHERE receiptId = :receiptId AND personId = :personId")
    suspend fun deleteByReceiptAndPerson(receiptId: String, personId: String)

    @Query("DELETE FROM receipt_participants WHERE receiptId = :receiptId")
    suspend fun deleteAllByReceiptId(receiptId: String)
}
