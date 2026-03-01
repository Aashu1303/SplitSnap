package com.splitsnap.di

import android.content.Context
import android.content.SharedPreferences
import com.splitsnap.domain.api.GeminiReceiptService
import com.splitsnap.domain.util.RateLimitManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("splitsnap_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideRateLimitManager(sharedPreferences: SharedPreferences): RateLimitManager {
        return RateLimitManager(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideGeminiReceiptService(rateLimitManager: RateLimitManager): GeminiReceiptService {
        return GeminiReceiptService(rateLimitManager)
    }
}
