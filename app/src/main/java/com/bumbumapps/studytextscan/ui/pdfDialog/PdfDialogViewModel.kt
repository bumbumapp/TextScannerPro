package com.bumbumapps.studytextscan.ui.pdfDialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumbumapps.studytextscan.persistence.entity.Scan
import com.bumbumapps.studytextscan.persistence.repository.ScanRepository
import kotlinx.coroutines.launch

class PdfDialogViewModel(
    val savedStateHandle: SavedStateHandle,
    private val scanRepo: ScanRepository
) : ViewModel() {

    private val scanId = savedStateHandle.get<Int>("pdf_scan_id") ?: 0

    fun getScan(action: (Scan?) -> Unit) {
        viewModelScope.launch {
            scanRepo.getScanById(scanId).collect(action)
        }
    }
}