package com.splitsnap.domain.model

/**
 * Friend/Person model for bill splitting
 * Maps to existing Person entity in the database
 */
data class Friend(
    val id: String,
    val name: String,
    val avatar: String? = null
)

/**
 * Extension function to convert Person to Friend
 */
fun Person.toFriend(): Friend {
    return Friend(
        id = id,
        name = name,
        avatar = null  // Can be extended to use avatarColor
    )
}

/**
 * Extension function to convert Friend to Person
 */
fun Friend.toPerson(avatarColor: AvatarColor = AvatarColor.random()): Person {
    return Person(
        id = id,
        name = name,
        initial = name.firstOrNull()?.uppercase() ?: "?",
        avatarColor = avatarColor,
        isMe = false
    )
}
