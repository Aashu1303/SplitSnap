package com.splitsnap

import android.app.Application
import com.splitsnap.data.local.database.SplitSnapDatabase
import com.splitsnap.data.repository.SplitSnapRepository

class SplitSnapApplication : Application() {
    
    val database by lazy { SplitSnapDatabase.getDatabase(this) }
    
    val repository by lazy {
        SplitSnapRepository(
            receiptDao = database.receiptDao(),
            receiptItemDao = database.receiptItemDao(),
            personDao = database.personDao(),
            receiptParticipantDao = database.receiptParticipantDao()
        )
    }
}
