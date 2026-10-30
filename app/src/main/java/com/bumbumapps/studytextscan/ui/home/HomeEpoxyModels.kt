package com.bumbumapps.studytextscan.ui.home

import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.bumbumapps.studytextscan.R
import com.bumbumapps.studytextscan.databinding.*
import com.bumbumapps.studytextscan.epoxy.ViewBindingKotlinModel
import com.bumbumapps.studytextscan.persistence.database.converters.DateConverter.toBitmap
import com.bumbumapps.studytextscan.persistence.entity.Scan
import com.bumbumapps.studytextscan.util.dateAsString

@EpoxyModelClass
abstract class ScanTopBarEpoxyModel :
    ViewBindingKotlinModel<ModelScanTopBarBinding>(R.layout.model_scan_top_bar) {

    @EpoxyAttribute
    lateinit var onInfoClicked: () -> Unit

    override fun ModelScanTopBarBinding.bind() {

    }
}

@EpoxyModelClass
abstract class ScanHeaderEpoxyModel :
    ViewBindingKotlinModel<ModelScanHeaderBinding>(R.layout.model_scan_header) {

    @EpoxyAttribute
    var numOfScans: String = ""

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var onCameraClicked: () -> Unit = {}

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var onGalleryClicked: () -> Unit = {}

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var onInfoClicked: () -> Unit = {}

    override fun ModelScanHeaderBinding.bind() {
        textViewNumOfScans.text = numOfScans
        textViewGreeting.setText(greetingRes(java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)))
        heroCamera.setOnClickListener { onCameraClicked() }
        tileGallery.setOnClickListener { onGalleryClicked() }
        buttonInfo.setOnClickListener { onInfoClicked() }
    }
}

@EpoxyModelClass
abstract class ScanListItemEpoxyModel :
    ViewBindingKotlinModel<ScanListItemBinding>(R.layout.scan_list_item) {

    @EpoxyAttribute
    lateinit var scan: Scan

    @EpoxyAttribute
    lateinit var onScanClicked: (Scan) -> Unit

    @EpoxyAttribute
    lateinit var copyClicked:(Scan)-> Unit

    @EpoxyAttribute
    lateinit var shareIt:(Scan)-> Unit


    override fun ScanListItemBinding.bind() {
     


        textViewDate.text = dateAsString(scan.dateModified)
        textViewDate.setOnClickListener {
            onScanClicked(scan)
        }

        card.setOnClickListener{onScanClicked(scan)}
        root.setOnClickListener { onScanClicked(scan) } // whole Bento card opens the scan
       imageViewCopy.setOnClickListener { copyClicked(scan) }
        textViewContent.text = scan.scanText
        textViewContent.setOnClickListener { onScanClicked(scan) }
        imageViewPinned.isVisible = scan.isPinned
        imageViewShare.setOnClickListener { shareIt(scan) }
        imageViewScanned.setImageBitmap(toBitmap(scan.scannedImage))
    }
}

@EpoxyModelClass
abstract class ListHeaderEpoxyModel :
    ViewBindingKotlinModel<ModelPinnedHeaderBinding>(R.layout.model_pinned_header) {
    @EpoxyAttribute
    lateinit var headerTitle: String

    override fun ModelPinnedHeaderBinding.bind() {
        textViewListHeader.text = headerTitle
    }
}

@EpoxyModelClass
abstract class LoadingScansEpoxyModel :
    ViewBindingKotlinModel<ModelScanLoadingBarBinding>(R.layout.model_scan_loading_bar) {

    override fun ModelScanLoadingBarBinding.bind() {}
}

/** Time-of-day greeting for the Bento header. */
fun greetingRes(hour: Int): Int = when (hour) {
    in 5..11 -> R.string.greet_morning
    in 12..17 -> R.string.greet_afternoon
    else -> R.string.greet_evening
}
