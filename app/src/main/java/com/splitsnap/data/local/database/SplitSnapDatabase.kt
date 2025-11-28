package com.splitsnap.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.splitsnap.data.local.dao.PersonDao
import com.splitsnap.data.local.dao.ReceiptDao
import com.splitsnap.data.local.dao.ReceiptItemDao
import com.splitsnap.data.local.dao.ReceiptParticipantDao
import com.splitsnap.data.local.entity.PersonEntity
import com.splitsnap.data.local.entity.ReceiptEntity
import com.splitsnap.data.local.entity.ReceiptItemEntity
import com.splitsnap.data.local.entity.ReceiptParticipantEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        ReceiptEntity::class,
        ReceiptItemEntity::class,
        PersonEntity::class,
        ReceiptParticipantEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SplitSnapDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
    abstract fun receiptItemDao(): ReceiptItemDao
    abstract fun personDao(): PersonDao
    abstract fun receiptParticipantDao(): ReceiptParticipantDao

    companion object {
        @Volatile
        private var INSTANCE: SplitSnapDatabase? = null

        fun getDatabase(context: Context): SplitSnapDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SplitSnapDatabase::class.java,
                    "splitsnap_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val defaultPerson = PersonEntity(
                        id = UUID.randomUUID().toString(),
                        name = "You",
                        initial = "Y",
                        avatarColor = "Primary",
                        isMe = true,
                        relationship = null
                    )
                    database.personDao().insertPerson(defaultPerson)
                }
            }
        }
    }
}
