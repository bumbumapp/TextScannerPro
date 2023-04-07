package com.bumbumapps.studytextscan.di

import com.bumbumapps.studytextscan.ui.detailscan.DetailScanViewModel
import com.bumbumapps.studytextscan.ui.home.HomeViewModel
import com.bumbumapps.studytextscan.ui.pdfDialog.PdfDialogViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        HomeViewModel(
            scanRepo = get(),
            filteredTextModelRepo = get(),
            prefs = get(),
            scanTextFromImageUseCase = get()
        )
    }

    viewModel {
        DetailScanViewModel(
            savedStateHandle = get(),
            scanRepository = get(),
            filteredModelsRepository = get()
        )
    }
    viewModel {
        PdfDialogViewModel(savedStateHandle = get(), scanRepo = get())
    }
}