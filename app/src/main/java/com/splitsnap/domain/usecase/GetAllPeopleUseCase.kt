package com.splitsnap.domain.usecase

import com.splitsnap.domain.model.Person
import com.splitsnap.domain.repository.SplitSnapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPeopleUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    operator fun invoke(): Flow<List<Person>> = repository.getAllPeople()
}
