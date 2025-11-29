package com.splitsnap.domain.model

data class Person(
    val id: String,
    val name: String,
    val initial: String,
    val avatarColor: AvatarColor,
    val isMe: Boolean = false,
    val relationship: String? = null
)

enum class AvatarColor(val colorName: String) {
    PRIMARY("Primary"),
    SECONDARY("Secondary"),
    TERTIARY("Tertiary"),
    GREEN("Green"),
    ORANGE("Orange"),
    PURPLE("Purple"),
    PINK("Pink"),
    TEAL("Teal");

    companion object {
        fun fromString(value: String): AvatarColor {
            return entries.find { it.colorName.equals(value, ignoreCase = true) } ?: PRIMARY
        }

        fun random(): AvatarColor {
            return entries.filter { it != PRIMARY }.random()
        }
    }
}
