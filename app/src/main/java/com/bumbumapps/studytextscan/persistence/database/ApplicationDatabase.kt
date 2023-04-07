package com.bumbumapps.studytextscan.persistence.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bumbumapps.studytextscan.persistence.dao.FilteredTextModelDao
import com.bumbumapps.studytextscan.persistence.dao.ScanDao
import com.bumbumapps.studytextscan.persistence.entity.FilteredTextModel
import com.bumbumapps.studytextscan.persistence.entity.Scan

@Database(
    entities = [
        Scan::class,
        FilteredTextModel::class
    ],
    version = 4
)

abstract class ApplicationDatabase : RoomDatabase() {
    abstract val scanDao: ScanDao
    abstract val filteredTextModelDao: FilteredTextModelDao
}