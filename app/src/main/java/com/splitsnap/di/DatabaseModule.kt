package com.splitsnap.di

import android.content.Context
import com.splitsnap.data.local.dao.PersonDao
import com.splitsnap.data.local.dao.ReceiptDao
import com.splitsnap.data.local.dao.ReceiptItemDao
import com.splitsnap.data.local.dao.ReceiptParticipantDao
import com.splitsnap.data.local.database.SplitSnapDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SplitSnapDatabase {
        return SplitSnapDatabase.getDatabase(context)
    }

    @Provides
    fun provideReceiptDao(database: SplitSnapDatabase): ReceiptDao {
        return database.receiptDao()
    }

    @Provides
    fun provideReceiptItemDao(database: SplitSnapDatabase): ReceiptItemDao {
        return database.receiptItemDao()
    }

    @Provides
    fun providePersonDao(database: SplitSnapDatabase): PersonDao {
        return database.personDao()
    }

    @Provides
    fun provideReceiptParticipantDao(database: SplitSnapDatabase): ReceiptParticipantDao {
        return database.receiptParticipantDao()
    }
}
