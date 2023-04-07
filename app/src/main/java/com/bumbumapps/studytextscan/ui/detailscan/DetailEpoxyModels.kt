package com.bumbumapps.studytextscan.ui.detailscan

import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.bumbumapps.studytextscan.R
import com.bumbumapps.studytextscan.databinding.ModelChipBinding
import com.bumbumapps.studytextscan.epoxy.ViewBindingKotlinModel
import com.bumbumapps.studytextscan.persistence.entity.FilteredTextModel
import com.google.android.material.card.MaterialCardView

@EpoxyModelClass
abstract class ChipEpoxyModel : ViewBindingKotlinModel<ModelChipBinding>(R.layout.model_chip) {

    @EpoxyAttribute
    lateinit var model: FilteredTextModel

    @EpoxyAttribute
    lateinit var onModelClick: () -> Unit

    @EpoxyAttribute
    lateinit var initCard: (MaterialCardView) -> Unit

    override fun ModelChipBinding.bind() {
        initCard(chipCard)
        chipCard.setOnClickListener { onModelClick() }
        textViewChip.text = model.content


    }
}