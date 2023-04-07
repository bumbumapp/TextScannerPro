package com.bumbumapps.studytextscan.ui.detailscan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumbumapps.studytextscan.persistence.repository.FilteredTextRepository
import com.bumbumapps.studytextscan.persistence.repository.ScanRepository
import com.bumbumapps.studytextscan.util.getCurrentDateTime
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DetailScanViewModel(
    val savedStateHandle: SavedStateHandle,
    private val scanRepository: ScanRepository,
    private val filteredModelsRepository: FilteredTextRepository
) : ViewModel() {

    val scanId = savedStateHandle.get<Int>("scan_id") ?: 0
    private val isJustCreated = savedStateHandle.get<Int>("is_created") ?: 0

    private val _events = Channel<DetailScanEvents>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val loading = MutableStateFlow(true)
    private val toShowKeyboard = MutableStateFlow(true)

    private val scan = scanRepository.getScanById(scanId)
        .onEach {
            loading.value = false
            delay(100)
            if (isJustCreated == 1 && toShowKeyboard.value) _events.send(DetailScanEvents.ShowSoftwareKeyboardOnFirstLoad)
            toShowKeyboard.value = false
        }

    private val filteredModels = scan
        .flatMapLatest { filteredModelsRepository.getModelsByScanId(it.scanId.toInt()) }

    val state = combine(
        loading,
        scan,
        filteredModels
    ) { isLoading, scan, textModels ->
        DetailScanUiState(
            isLoading = isLoading,
            scan = scan,
            filteredTextModels = textModels
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), DetailScanUiState())


    fun deleteScan() = viewModelScope.launch {
        val current = state.value.scan
        current?.let { scanRepository.deleteScan(it) }
    }

    fun onNavigateUp(content: String) {
        state.value.scan?.let {
            if ( it.scanText != content)
                viewModelScope.launch {
                    _events.send(DetailScanEvents.ShowUnsavedChanges)
                }
            else
                viewModelScope.launch {
                    _events.send(DetailScanEvents.NavigateUp)
                }
        }
    }

    fun updateScan(content: String) {
        viewModelScope.launch {
            state.value.scan?.let { scan ->
                val updated = scan.copy(
//                    scanTitle = title,
                    scanText = content,
                    dateModified = getCurrentDateTime()
                )
                scanRepository.updateScan(updated)
                _events.send(DetailScanEvents.ShowScanUpdated)
                return@launch
            }
        }
    }

    fun updateScanPinned() = viewModelScope.launch {
        state.value.scan?.let {
            val updated = it.copy(isPinned = !it.isPinned)
            scanRepository.updateScan(updated)
        }
    }
}