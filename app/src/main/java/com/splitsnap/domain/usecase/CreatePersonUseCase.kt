package com.splitsnap.domain.usecase

import com.splitsnap.domain.model.AvatarColor
import com.splitsnap.domain.model.Person
import com.splitsnap.domain.repository.SplitSnapRepository
import javax.inject.Inject

class CreatePersonUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    suspend operator fun invoke(
        name: String,
        relationship: String? = null,
        avatarColor: AvatarColor = AvatarColor.random()
    ): Person = repository.createPerson(name, relationship, avatarColor)
}
