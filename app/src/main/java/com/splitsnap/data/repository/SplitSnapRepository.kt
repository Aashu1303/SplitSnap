package com.splitsnap.data.repository

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
import com.splitsnap.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class SplitSnapRepository(
    private val receiptDao: ReceiptDao,
    private val receiptItemDao: ReceiptItemDao,
    private val personDao: PersonDao,
    private val receiptParticipantDao: ReceiptParticipantDao
) {
    private val gson = Gson()

    // Receipt operations
    fun getAllReceipts(): Flow<List<Receipt>> {
        return receiptDao.getAllReceipts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getReceiptById(id: String): Receipt? {
        return receiptDao.getReceiptById(id)?.toDomain()
    }

    suspend fun createReceipt(storeName: String, date: String, total: Int): Receipt {
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

    suspend fun updateReceipt(receipt: Receipt) {
        receiptDao.updateReceipt(receipt.toEntity())
    }

    suspend fun deleteReceipt(id: String) {
        receiptDao.deleteReceiptById(id)
    }

    // Receipt Item operations
    fun getReceiptItems(receiptId: String): Flow<List<ReceiptItem>> {
        return receiptItemDao.getItemsByReceiptId(receiptId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getReceiptItemsSync(receiptId: String): List<ReceiptItem> {
        return receiptItemDao.getItemsByReceiptIdSync(receiptId).map { it.toDomain() }
    }

    suspend fun createReceiptItem(
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

    suspend fun updateReceiptItem(item: ReceiptItem) {
        receiptItemDao.updateItem(item.toEntity())
    }

    suspend fun updateItemAssignments(itemId: String, assignments: Map<String, Int>) {
        val item = receiptItemDao.getItemById(itemId) ?: return
        val updatedItem = item.copy(assignments = gson.toJson(assignments))
        receiptItemDao.updateItem(updatedItem)
    }

    suspend fun deleteReceiptItem(id: String) {
        receiptItemDao.deleteItemById(id)
    }

    // Person operations
    fun getAllPeople(): Flow<List<Person>> {
        return personDao.getAllPeople().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getAllPeopleSync(): List<Person> {
        return personDao.getAllPeopleSync().map { it.toDomain() }
    }

    suspend fun getPersonById(id: String): Person? {
        return personDao.getPersonById(id)?.toDomain()
    }

    suspend fun getMe(): Person? {
        return personDao.getMe()?.toDomain()
    }

    suspend fun createPerson(
        name: String,
        relationship: String? = null,
        avatarColor: AvatarColor = AvatarColor.random()
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

    suspend fun deletePerson(id: String) {
        personDao.deletePersonById(id)
    }

    // Receipt Participant operations
    fun getReceiptParticipants(receiptId: String): Flow<List<Person>> {
        return receiptParticipantDao.getPeopleByReceiptId(receiptId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getReceiptParticipantsSync(receiptId: String): List<Person> {
        return receiptParticipantDao.getPeopleByReceiptIdSync(receiptId).map { it.toDomain() }
    }

    suspend fun addParticipant(receiptId: String, personId: String) {
        val participant = ReceiptParticipantEntity(
            id = UUID.randomUUID().toString(),
            receiptId = receiptId,
            personId = personId
        )
        receiptParticipantDao.insertParticipant(participant)
    }

    suspend fun removeParticipant(receiptId: String, personId: String) {
        receiptParticipantDao.deleteByReceiptAndPerson(receiptId, personId)
    }

    // Calculate splits
    suspend fun calculateSplits(receiptId: String): List<PersonSplit> {
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
