package com.splitsnap.domain.usecase

import com.splitsnap.domain.model.Person
import com.splitsnap.domain.repository.SplitSnapRepository
import javax.inject.Inject

class GetMeUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    suspend operator fun invoke(): Person? = repository.getMe()
}
