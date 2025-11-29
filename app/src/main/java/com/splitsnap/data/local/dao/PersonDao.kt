package com.splitsnap.data.local.dao

import androidx.room.*
import com.splitsnap.data.local.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM people")
    fun getAllPeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people")
    suspend fun getAllPeopleSync(): List<PersonEntity>

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getPersonById(id: String): PersonEntity?

    @Query("SELECT * FROM people WHERE isMe = 1 LIMIT 1")
    suspend fun getMe(): PersonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity)

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Delete
    suspend fun deletePerson(person: PersonEntity)

    @Query("DELETE FROM people WHERE id = :id")
    suspend fun deletePersonById(id: String)
}
