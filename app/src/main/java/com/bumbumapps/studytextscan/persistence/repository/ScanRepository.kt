package com.bumbumapps.studytextscan.persistence.repository

import com.bumbumapps.studytextscan.persistence.database.ApplicationDatabase
import com.bumbumapps.studytextscan.persistence.entity.Scan

class ScanRepository(
    database: ApplicationDatabase
) {
    private val dao = database.scanDao

    val allScans = dao.getAllScans()

    suspend fun insertScan(scan: Scan) = dao.insertScan(scan)
    suspend fun deleteScan(scan: Scan) = dao.deleteScan(scan)
    suspend fun updateScan(scan: Scan) = dao.updateScan(scan)

    fun getScanById(id: Int) = dao.getScanById(id)
}