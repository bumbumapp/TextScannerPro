package com.bumbumapps.studytextscan.ui.detailscan

import com.bumbumapps.studytextscan.persistence.entity.FilteredTextModel
import com.bumbumapps.studytextscan.persistence.entity.Scan

data class DetailScanUiState(
    val scan: Scan? = null,
    val filteredTextModels: List<FilteredTextModel> = emptyList(),
    val isLoading: Boolean = true,
)
