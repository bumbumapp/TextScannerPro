package com.bumbumapps.studytextscan.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumbumapps.studytextscan.datastore.AppPreferences
import com.bumbumapps.studytextscan.persistence.entity.FilteredTextModel
import com.bumbumapps.studytextscan.persistence.entity.Scan
import com.bumbumapps.studytextscan.persistence.repository.FilteredTextRepository
import com.bumbumapps.studytextscan.persistence.repository.ScanRepository
import com.bumbumapps.studytextscan.util.getCurrentDateTime
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val prefs: AppPreferences,
    private val scanRepo: ScanRepository,
    private val filteredTextModelRepo: FilteredTextRepository,
    private val scanTextFromImageUseCase: ScanTextFromImageUseCase
): ViewModel() {
    private val _events = Channel<HomeEvents>(capacity = 1)
    val events = _events.receiveAsFlow()
    private val isLoading = MutableStateFlow(true)

    private val scans = scanRepo.allScans
        .distinctUntilChanged()
        .onEach { isLoading.value = false }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    private val listOfScans = scans
        .map { list -> list.filter { !it.isPinned } }

    private val listOfPinnedScans = scans
        .map { list -> list.filter { it.isPinned } }


    val state = combine(
        isLoading,
        listOfScans,
        listOfPinnedScans
    ) { loading, scans, pinnedScans ->
        HomeUiState(
            isLoading = loading,
            scans = scans,
            pinnedScans = pinnedScans
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), HomeUiState())



    fun handlePermissionDenied() = viewModelScope.launch {
        _events.send(HomeEvents.ShowPermissionInfo)
    }


    private fun createScan(text: String, filteredTextList: List<Pair<String,String>>,byteArray: ByteArray) = viewModelScope.launch {
        if (text.isNotEmpty() or text.isNotBlank()) {
            val scan = Scan(
                scanText = text,
                dateCreated = getCurrentDateTime(),
                dateModified = getCurrentDateTime(),
                scanTitle = "",
                isPinned = false,
                scannedImage =byteArray
            )

            val result = scanRepo.insertScan(scan)
            val scanId = Integer.parseInt(result.toString())

            filteredTextList.forEach {
                val model = FilteredTextModel(scanId = scanId, type = it.first, content = it.second)
                filteredTextModelRepo.insertModel(model)
                Log.d("DEBUGn", "createScan: model inserted ${model.content}")
            }

            _events.send(HomeEvents.ShowCurrentScanSaved(scanId)).also {
                prefs.incrementSupportCount()
            }
        } else {
            _events.send(HomeEvents.ShowScanEmpty)
        }

    }

    private fun showLoadingDialog() = viewModelScope.launch {
        _events.send(HomeEvents.ShowLoadingDialog)
    }

    fun deleteScan(scan: Scan) = viewModelScope.launch {
        scanRepo.deleteScan(scan)
        _events.send(HomeEvents.ShowUndoDeleteScan(scan))
    }

    fun insertScan(scan: Scan) = viewModelScope.launch {
        scanRepo.insertScan(scan)
    }



    fun handleScan(image: InputImage,byteArray: ByteArray) {
        showLoadingDialog()
        viewModelScope.launch {
            try {
                val (completeText,filteredText) = scanTextFromImageUseCase(image)
                createScan(completeText, filteredText,byteArray)
            } catch (e: Exception) {
                _events.send(HomeEvents.ShowErrorWhenScanning)
            }
        }
    }



}