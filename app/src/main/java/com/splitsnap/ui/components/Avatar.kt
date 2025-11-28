package com.splitsnap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitsnap.domain.model.AvatarColor
import com.splitsnap.domain.model.Person
import com.splitsnap.ui.theme.*

@Composable
fun Avatar(
    person: Person,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val backgroundColor = when (person.avatarColor) {
        AvatarColor.PRIMARY -> Primary
        AvatarColor.SECONDARY -> Secondary
        AvatarColor.TERTIARY -> Tertiary
        AvatarColor.GREEN -> AvatarGreen
        AvatarColor.ORANGE -> AvatarOrange
        AvatarColor.PURPLE -> AvatarPurple
        AvatarColor.PINK -> AvatarPink
        AvatarColor.TEAL -> AvatarTeal
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = person.initial,
            color = Color.White,
            fontSize = (size.value * 0.4f).sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun AvatarSmall(
    person: Person,
    modifier: Modifier = Modifier
) {
    Avatar(
        person = person,
        modifier = modifier,
        size = 32.dp
    )
}

@Composable
fun AvatarLarge(
    person: Person,
    modifier: Modifier = Modifier
) {
    Avatar(
        person = person,
        modifier = modifier,
        size = 56.dp
    )
}
