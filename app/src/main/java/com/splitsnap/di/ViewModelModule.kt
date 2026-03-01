package com.splitsnap.di

import com.splitsnap.viewmodel.BillScanViewModel
import com.splitsnap.viewmodel.BillScanViewModelImpl
import com.splitsnap.viewmodel.CameraViewModel
import com.splitsnap.viewmodel.CameraViewModelImpl
import com.splitsnap.viewmodel.EditReceiptViewModel
import com.splitsnap.viewmodel.EditReceiptViewModelImpl
import com.splitsnap.viewmodel.HomeViewModel
import com.splitsnap.viewmodel.HomeViewModelImpl
import com.splitsnap.viewmodel.SplitSummaryViewModel
import com.splitsnap.viewmodel.SplitSummaryViewModelImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
interface ViewModelModule {
    @Binds
    @ViewModelScoped
    fun bindHomeViewModel(impl: HomeViewModelImpl): HomeViewModel

    @Binds
    @ViewModelScoped
    fun bindCameraViewModel(impl: CameraViewModelImpl): CameraViewModel

    @Binds
    @ViewModelScoped
    fun bindEditReceiptViewModel(impl: EditReceiptViewModelImpl): EditReceiptViewModel

    @Binds
    @ViewModelScoped
    fun bindSplitSummaryViewModel(impl: SplitSummaryViewModelImpl): SplitSummaryViewModel

    @Binds
    @ViewModelScoped
    fun bindBillScanViewModel(impl: BillScanViewModelImpl): BillScanViewModel
}