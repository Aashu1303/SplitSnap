package com.splitsnap.domain.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.splitsnap.data.local.dao.PersonDao
import com.splitsnap.data.local.dao.ReceiptDao
import com.splitsnap.data.local.dao.ReceiptItemDao
import com.splitsnap.data.local.dao.ReceiptParticipantDao
import com.splitsnap.data.local.entity.PersonEntity
import com.splitsnap.data.local.entity.ReceiptEntity
import com.splitsnap.data.local.entity.ReceiptItemEntity
import com.splitsnap.data.local.entity.ReceiptParticipantEntity
import com.splitsnap.domain.model.AvatarColor
import com.splitsnap.domain.model.Person
import com.splitsnap.domain.model.PersonSplit
import com.splitsnap.domain.model.Receipt
import com.splitsnap.domain.model.ReceiptItem
import com.splitsnap.domain.model.ReceiptStatus
import com.splitsnap.domain.model.SplitItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

interface SplitSnapRepository {
    suspend fun getAllReceipts(): Flow<List<Receipt>>
    suspend fun getReceiptById(id: String): Receipt?
    suspend fun createReceipt(storeName: String, date: String, total: Int): Receipt
    suspend fun updateReceipt(receipt: Receipt)
    suspend fun deleteReceipt(id: String)
    fun getReceiptItems(receiptId: String): Flow<List<ReceiptItem>>
    suspend fun getReceiptItemsSync(receiptId: String): List<ReceiptItem>
    suspend fun createReceiptItem(
        receiptId: String,
        name: String,
        quantity: Int,
        price: Int
    ): ReceiptItem

    suspend fun updateReceiptItem(item: ReceiptItem)
    suspend fun updateItemAssignments(itemId: String, assignments: Map<String, Int>)
    suspend fun deleteReceiptItem(id: String)
    fun getAllPeople(): Flow<List<Person>>
    suspend fun getAllPeopleSync(): List<Person>
    suspend fun getPersonById(id: String): Person?
    suspend fun getMe(): Person?
    suspend fun createPerson(
        name: String,
        relationship: String? = null,
        avatarColor: AvatarColor = AvatarColor.random()
    ): Person

    suspend fun deletePerson(id: String)
    fun getReceiptParticipants(receiptId: String): Flow<List<Person>>
    suspend fun getReceiptParticipantsSync(receiptId: String): List<Person>
    suspend fun addParticipant(receiptId: String, personId: String)
    suspend fun removeParticipant(receiptId: String, personId: String)
    suspend fun calculateSplits(receiptId: String): List<PersonSplit>
}

class SplitSnapRepositoryImpl @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val receiptItemDao: ReceiptItemDao,
    private val personDao: PersonDao,
    private val receiptParticipantDao: ReceiptParticipantDao
) : SplitSnapRepository {
    private val gson = Gson()

    // Receipt operations
    override suspend fun getAllReceipts(): Flow<List<Receipt>> {
        return receiptDao.getAllReceipts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getReceiptById(id: String): Receipt? {
        return receiptDao.getReceiptById(id)?.toDomain()
    }

    override suspend fun createReceipt(storeName: String, date: String, total: Int): Receipt {
        val receipt = ReceiptEntity(
            id = UUID.randomUUID().toString(),
            storeName = storeName,
            date = date,
            total = total,
            status = "draft"
        )
        receiptDao.insertReceipt(receipt)
        return receipt.toDomain()
    }

    override suspend fun updateReceipt(receipt: Receipt) {
        receiptDao.updateReceipt(receipt.toEntity())
    }

    override suspend fun deleteReceipt(id: String) {
        receiptDao.deleteReceiptById(id)
    }

    // Receipt Item operations
    override fun getReceiptItems(receiptId: String): Flow<List<ReceiptItem>> {
        return receiptItemDao.getItemsByReceiptId(receiptId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getReceiptItemsSync(receiptId: String): List<ReceiptItem> {
        return receiptItemDao.getItemsByReceiptIdSync(receiptId).map { it.toDomain() }
    }

    override suspend fun createReceiptItem(
        receiptId: String,
        name: String,
        quantity: Int,
        price: Int
    ): ReceiptItem {
        val item = ReceiptItemEntity(
            id = UUID.randomUUID().toString(),
            receiptId = receiptId,
            name = name,
            quantity = quantity,
            price = price,
            assignments = "{}"
        )
        receiptItemDao.insertItem(item)
        return item.toDomain()
    }

    override suspend fun updateReceiptItem(item: ReceiptItem) {
        receiptItemDao.updateItem(item.toEntity())
    }

    override suspend fun updateItemAssignments(itemId: String, assignments: Map<String, Int>) {
        val item = receiptItemDao.getItemById(itemId) ?: return
        val updatedItem = item.copy(assignments = gson.toJson(assignments))
        receiptItemDao.updateItem(updatedItem)
    }

    override suspend fun deleteReceiptItem(id: String) {
        receiptItemDao.deleteItemById(id)
    }

    // Person operations
    override fun getAllPeople(): Flow<List<Person>> {
        return personDao.getAllPeople().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAllPeopleSync(): List<Person> {
        return personDao.getAllPeopleSync().map { it.toDomain() }
    }

    override suspend fun getPersonById(id: String): Person? {
        return personDao.getPersonById(id)?.toDomain()
    }

    override suspend fun getMe(): Person? {
        return personDao.getMe()?.toDomain()
    }

    override suspend fun createPerson(
        name: String,
        relationship: String?,
        avatarColor: AvatarColor
    ): Person {
        val initial = name.firstOrNull()?.uppercase() ?: "?"
        val person = PersonEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            initial = initial,
            avatarColor = avatarColor.colorName,
            isMe = false,
            relationship = relationship
        )
        personDao.insertPerson(person)
        return person.toDomain()
    }

    override suspend fun deletePerson(id: String) {
        personDao.deletePersonById(id)
    }

    // Receipt Participant operations
    override fun getReceiptParticipants(receiptId: String): Flow<List<Person>> {
        return receiptParticipantDao.getPeopleByReceiptId(receiptId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getReceiptParticipantsSync(receiptId: String): List<Person> {
        return receiptParticipantDao.getPeopleByReceiptIdSync(receiptId).map { it.toDomain() }
    }

    override suspend fun addParticipant(receiptId: String, personId: String) {
        val participant = ReceiptParticipantEntity(
            id = UUID.randomUUID().toString(),
            receiptId = receiptId,
            personId = personId
        )
        receiptParticipantDao.insertParticipant(participant)
    }

    override suspend fun removeParticipant(receiptId: String, personId: String) {
        receiptParticipantDao.deleteByReceiptAndPerson(receiptId, personId)
    }

    // Calculate splits
    override suspend fun calculateSplits(receiptId: String): List<PersonSplit> {
        val items = getReceiptItemsSync(receiptId)
        val participants = getReceiptParticipantsSync(receiptId)

        return participants.map { person ->
            val splitItems = items.mapNotNull { item ->
                val qty = item.getAssignedQuantity(person.id)
                if (qty > 0) {
                    SplitItem(
                        itemId = item.id,
                        name = item.name,
                        quantity = qty,
                        unitPrice = item.price,
                        totalPrice = qty * item.price
                    )
                } else null
            }
            PersonSplit(
                person = person,
                items = splitItems,
                total = splitItems.sumOf { it.totalPrice }
            )
        }
    }

    // Extension functions for entity conversion
    private fun ReceiptEntity.toDomain() = Receipt(
        id = id,
        storeName = storeName,
        date = date,
        total = total,
        status = ReceiptStatus.fromString(status),
        createdAt = createdAt
    )

    private fun Receipt.toEntity() = ReceiptEntity(
        id = id,
        storeName = storeName,
        date = date,
        total = total,
        status = status.toString(),
        createdAt = createdAt
    )

    private fun ReceiptItemEntity.toDomain(): ReceiptItem {
        val assignmentType = object : TypeToken<Map<String, Int>>() {}.type
        val assignmentMap: Map<String, Int> = try {
            gson.fromJson(assignments, assignmentType) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
        return ReceiptItem(
            id = id,
            receiptId = receiptId,
            name = name,
            quantity = quantity,
            price = price,
            assignments = assignmentMap
        )
    }

    private fun ReceiptItem.toEntity() = ReceiptItemEntity(
        id = id,
        receiptId = receiptId,
        name = name,
        quantity = quantity,
        price = price,
        assignments = gson.toJson(assignments)
    )

    private fun PersonEntity.toDomain() = Person(
        id = id,
        name = name,
        initial = initial,
        avatarColor = AvatarColor.fromString(avatarColor),
        isMe = isMe,
        relationship = relationship
    )
}
