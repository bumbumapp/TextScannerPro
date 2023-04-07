package com.bumbumapps.studytextscan.di

import android.content.Context
import androidx.room.Room
import com.bumbumapps.studytextscan.datastore.AppPreferences
import com.bumbumapps.studytextscan.datastore.datastore
import com.bumbumapps.studytextscan.persistence.database.ApplicationDatabase
import com.bumbumapps.studytextscan.service.pdfExport.PdfExportServiceImpl
import com.bumbumapps.studytextscan.service.textFilter.TextFilterServiceImpl
import com.bumbumapps.studytextscan.service.textFilter.TextFilterService
import com.bumbumapps.studytextscan.ui.home.ScanTextFromImageUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

private fun provideDatabase(context: Context) =
    Room.databaseBuilder(
        context,
        ApplicationDatabase::class.java,
        "posts_database"
    ).fallbackToDestructiveMigration().build()

private fun providePdfExportService() =
    PdfExportServiceImpl()

private fun providePreferences(context: Context) = AppPreferences(context.datastore)

private fun provideFilterTextService() =
    TextFilterServiceImpl()

private fun provideScanTextFromImageUseCase(filterTextService: TextFilterService) =
    ScanTextFromImageUseCase(filterTextService)


val appModule = module {
    single { provideDatabase(context = androidContext()) }
    single { providePdfExportService() }
    factory { providePreferences(androidContext()) }
    single { provideFilterTextService() } bind TextFilterService::class
    factory { provideScanTextFromImageUseCase(get()) }
}