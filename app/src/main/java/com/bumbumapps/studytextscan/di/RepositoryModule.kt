package com.bumbumapps.studytextscan.di

import com.bumbumapps.studytextscan.persistence.repository.FilteredTextRepository
import com.bumbumapps.studytextscan.persistence.repository.ScanRepository
import org.koin.dsl.module

val repoModule = module {
    single { ScanRepository(database = get()) }
    single { FilteredTextRepository(database = get()) }
}