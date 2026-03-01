package com.splitsnap.di

import com.splitsnap.domain.repository.GeminiReceiptRepository
import com.splitsnap.domain.repository.GeminiReceiptRepositoryImpl
import com.splitsnap.domain.repository.SplitSnapRepository
import com.splitsnap.domain.repository.SplitSnapRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    @Singleton
    fun bindSplitSnapRepository(impl: SplitSnapRepositoryImpl): SplitSnapRepository

    @Binds
    @Singleton
    fun bindGeminiReceiptRepository(impl: GeminiReceiptRepositoryImpl): GeminiReceiptRepository
}