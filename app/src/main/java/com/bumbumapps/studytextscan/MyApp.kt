package com.bumbumapps.studytextscan

import android.app.Application
import com.bumbumapps.studytextscan.di.appModule
import com.bumbumapps.studytextscan.di.repoModule
import com.bumbumapps.studytextscan.di.viewModelModule
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(listOf(appModule, repoModule, viewModelModule))
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        }
    }
}