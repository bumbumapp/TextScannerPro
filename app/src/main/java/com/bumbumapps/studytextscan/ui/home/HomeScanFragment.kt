package com.bumbumapps.studytextscan.ui.home

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyTouchHelper
import com.bumbumapps.studytextscan.AdsLoader
import com.bumbumapps.studytextscan.Globals
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.bumbumapps.studytextscan.R
import com.bumbumapps.studytextscan.Timers
import com.bumbumapps.studytextscan.databinding.FragmentScanHomeBinding
import com.bumbumapps.studytextscan.util.*
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.material.transition.MaterialSharedAxis
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.ByteArrayOutputStream
import java.io.InputStream


class HomeScanFragment : Fragment(R.layout.fragment_scan_home) {
    private val binding: FragmentScanHomeBinding by viewBinding(FragmentScanHomeBinding::bind)
    private val viewModel: HomeViewModel by sharedViewModel()
    private val selectImageRequest = registerForActivityResult(CropImageContract()) {
        if (it.isSuccessful) {
            it.uriContent?.let { handleImage(it) }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            //permission granted. Continue workflow
            selectImageRequest.launch(cropImageCameraOptions)
        } else {
            //Provide explanation on why the permission is needed. AlertDialog maybe?
            viewModel.handlePermissionDenied()
        }
    }

    private val cropImageGalleryOptions = options {
        setGuidelines(CropImageView.Guidelines.ON)
        setImageSource(includeGallery = true, includeCamera = false)
    }

    private val cropImageCameraOptions = options {
        setGuidelines(CropImageView.Guidelines.ON)
        setImageSource(includeGallery = false, includeCamera = true)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateWindowInsets(binding.root)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        val loadingDialog = createLoadingDialog()

        binding.info.setOnClickListener {
            showAboutDialog()
        }

        collectFlow(viewModel.state) { state ->
            binding.apply {
                linearLayoutEmpty.isVisible = state.isEmpty

                recyclerViewScans.withModels {
                    scanTopBar {
                        id("scan_top_bar")
                        onInfoClicked {
                            exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

                        }
                    }
                    scanHeader {
                        id("scan_header")
                        numOfScans(getString(R.string.num_of_scans, state.itemCount))
                    }

                    if (state.isLoading)
                        loadingScans { id("loading_scans") }

                    if (state.pinnedScans.isNotEmpty()) {
                        listHeader {
                            id("pinned_header")
                            headerTitle(getString(R.string.header_pinned))
                        }
                        state.pinnedScans.forEach {
                            scanListItem {
                                id(it.scanId)
                                scan(it)
                                onScanClicked {
                                    if (Globals.TIMER_FINISHED && AdsLoader.mInterstitialAd != null) {
                                        AdsLoader.mInterstitialAd?.show(context as Activity)
                                        AdsLoader.mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                                            override fun onAdDismissedFullScreenContent() {

                                                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                                                    reenterTransition =
                                                        MaterialSharedAxis(MaterialSharedAxis.X, false)
                                                    val arg = bundleOf("scan_id" to it.scanId.toInt())
                                                    findNavController().navigate(
                                                        R.id.action_homeScanFragment_to_detailScanFragment,
                                                        arg
                                                    )
                                                    Globals.TIMER_FINISHED =false
                                                    Timers.timer().start()
                                                    AdsLoader.displayInterstitial(requireContext())
                                                }


                                        }
                                    }else{
                                        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                                        reenterTransition =
                                            MaterialSharedAxis(MaterialSharedAxis.X, false)
                                        val arg = bundleOf("scan_id" to it.scanId.toInt())
                                        findNavController().navigate(
                                            R.id.action_homeScanFragment_to_detailScanFragment,
                                            arg
                                        ) }

                                }
                                copyClicked {
                                    val clipboardManager =
                                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("raw_data", it.scanText)
                                    clipboardManager.setPrimaryClip(clip)
                                    showSnackbarShort(
                                        message = getString(R.string.copied_clip),

                                        )
                                }
                                shareIt {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, it.scanText)
                                        type = "text/plain"
                                    }
                                    val intent = Intent.createChooser(shareIntent, null)
                                    startActivity(intent)
                                }

                            }
                        }
                    }


                    if (state.scans.isNotEmpty()) {
                        listHeader {
                            id("others_header")
                            headerTitle(getString(R.string.headers_other))
                        }
                        state.scans.forEach {
                            scanListItem {
                                id(it.scanId)
                                scan(it)
                                onScanClicked {
                                    if (Globals.TIMER_FINISHED && AdsLoader.mInterstitialAd != null) {
                                        AdsLoader.mInterstitialAd?.show(context as Activity)
                                        AdsLoader.mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                                            override fun onAdDismissedFullScreenContent() {
                                                exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                                                reenterTransition =
                                                    MaterialSharedAxis(MaterialSharedAxis.X, false)
                                                val arg = bundleOf("scan_id" to it.scanId.toInt())
                                                findNavController().navigate(
                                                    R.id.action_homeScanFragment_to_detailScanFragment,
                                                    arg
                                                )
                                                Globals.TIMER_FINISHED =false
                                                Timers.timer().start()
                                                AdsLoader.displayInterstitial(requireContext())
                                            }
                                        }
                                    }else{
                                        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                                        reenterTransition =
                                            MaterialSharedAxis(MaterialSharedAxis.X, false)
                                        val arg = bundleOf("scan_id" to it.scanId.toInt())
                                        findNavController().navigate(
                                            R.id.action_homeScanFragment_to_detailScanFragment,
                                            arg
                                        )                                    }

                                }
                                copyClicked {
                                    val clipboardManager =
                                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("raw_data", it.scanText)
                                    clipboardManager.setPrimaryClip(clip)
                                    showSnackbarShort(
                                        message = getString(R.string.copied_clip),

                                        )
                                }
                                shareIt {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, it.scanText)
                                        type = "text/plain"
                                    }
                                    val intent = Intent.createChooser(shareIntent, null)
                                    startActivity(intent)
                                }
                            }
                        }

                    }
                }
            }
        }

        binding.apply {
            recyclerViewScans.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) {
                        buttonCameraScan.hide()
                        buttonGalleryScan.hide()
                    }
                    if (dy < 0) {
                        buttonCameraScan.show()
                        buttonGalleryScan.show()
                    }
                }
            })

            val delete = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_round_delete_white_24
            )
            EpoxyTouchHelper.initSwiping(recyclerViewScans)
                .left()
                .withTarget(ScanListItemEpoxyModel::class.java)
                .andCallbacks(object : EpoxyTouchHelper.SwipeCallbacks<ScanListItemEpoxyModel>() {
                    override fun onSwipeCompleted(
                        model: ScanListItemEpoxyModel?,
                        itemView: View?,
                        position: Int,
                        direction: Int
                    ) {
                        model?.let {
                            viewModel.deleteScan(it.scan)
                        }
                    }

                    override fun onSwipeProgressChanged(
                        model: ScanListItemEpoxyModel?,
                        itemView: View?,
                        swipeProgress: Float,
                        canvas: Canvas?
                    ) {
                        itemView?.let { view ->
                            view.alpha = swipeProgress + 1
                            val itemHeight = view.bottom - view.top
                            delete?.setTint(getColor(R.color.error_red))

                            val iconTop = view.top + (itemHeight - delete!!.intrinsicHeight) / 2
                            val iconMargin = (itemHeight - delete.intrinsicHeight) / 2
                            val iconLeft = view.right - iconMargin - delete.intrinsicWidth
                            val iconRight = view.right - iconMargin
                            val iconBottom = iconTop + delete.intrinsicHeight

                            delete.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                            delete.draw(canvas!!)
                        }
                    }
                })
        }

        collectFlow(viewModel.events) { homeEvents ->
            when (homeEvents) {
                is HomeEvents.ShowCurrentScanSaved -> {
                    if (Globals.TIMER_FINISHED && AdsLoader.mInterstitialAd != null) {
                        AdsLoader.mInterstitialAd?.show(context as Activity)
                        AdsLoader.mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                dialogDismiss(loadingDialog,homeEvents)
                                Globals.TIMER_FINISHED =false
                                Timers.timer().start()
                                AdsLoader.displayInterstitial(requireContext())
                            }
                        }
                    }else{
                        dialogDismiss(loadingDialog,homeEvents)
                    }

                }
                is HomeEvents.ShowLoadingDialog -> {
                    loadingDialog.show()
                }
                is HomeEvents.ShowScanEmpty -> {
                    loadingDialog.dismiss()
                    showSnackbarShort(
                        message = getString(R.string.no_text_found),
                        anchor = binding.buttonCameraScan
                    )
                }
                is HomeEvents.ShowUndoDeleteScan -> {
                    showSnackbarLongWithAction(
                        message = getString(R.string.scan_deleted),
                        anchor = binding.buttonCameraScan,
                        actionText = getString(R.string.undo)
                    ) {
                        viewModel.insertScan(homeEvents.scan)
                    }
                }
                is HomeEvents.ShowOnboarding -> {
                }
                is HomeEvents.ShowErrorWhenScanning -> {
                    loadingDialog.dismiss()
                    showSnackbarShort(
                        message = getString(R.string.something_went_wrong),
                        anchor = binding.buttonCameraScan
                    )
                }
                is HomeEvents.ShowPermissionInfo -> {
                    showCameraPermissionInfoDialog()
                }
                is HomeEvents.ShowSupportDialog -> {
                }
            }
        }



        binding.apply {
            recyclerViewScans.apply {
                layoutAnimation =
                    AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_anim)

                buttonCameraScan.setOnClickListener {
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)

                    when {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED -> {
                            //use the api that needs the permission
                            selectImageRequest.launch(cropImageCameraOptions)
                        }
                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }

                buttonGalleryScan.setOnClickListener {
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
                    selectImageRequest.launch(cropImageGalleryOptions)
                }
                animationView.repeatCount = 2

            }

        }

        requireActivity().apply {
            if (intent.action == Intent.ACTION_SEND) {
                if (intent.type?.startsWith("image") == true) {
                    handleIntent(intent)
                }
            }
        }
    }

    private fun dialogDismiss(
        loadingDialog: AlertDialog,
        homeEvents: HomeEvents.ShowCurrentScanSaved
    ) {
        loadingDialog.dismiss()
        val arg = bundleOf("scan_id" to homeEvents.id, "is_created" to 1)
        findNavController().navigate(
            R.id.action_homeScanFragment_to_detailScanFragment,
            arg
        )
    }

    private fun handleIntent(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            handleImage(it)
        }
        /*
        After the intent is handled, set the action to "" so it does not trigger again.
         */
        intent.action = ""
    }

    private fun handleImage(uri: Uri) {
        val image = InputImage.fromFilePath(requireContext(), uri)

        lifecycleScope.launch{
            try {
                viewModel.handleScan(image,getBytesArrayFromURI(uri)!!)
            }catch (e:Exception){
                Log.d("TAG","bitmap__"+e.message.toString())
            }

        }
    }
    private fun getBytesArrayFromURI(uri: Uri?): ByteArray? {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri!!)
            val byteBuffer = ByteArrayOutputStream()
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var len = 0
            while (inputStream!!.read(buffer).also { len = it } != -1) {
                byteBuffer.write(buffer, 0, len)
            }
            return byteBuffer.toByteArray()
        } catch (e:Exception) {
            Log.d("TAG","bitmap_"+e.message.toString())
        }
        return null
    }
    private fun showAboutDialog(){
        Log.d("jbsfsld","sdjsd")
        val dialog= android.app.Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_about);
        dialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)

        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        val back = ColorDrawable(Color.TRANSPARENT)
        val margin = 20
        val inset = InsetDrawable(back, margin)
        dialog.window?.setBackgroundDrawable(inset)
        dialog.findViewById<Button>(R.id.bt_licence).setOnClickListener {
            val url = "https://github.com/nikolaDrljaca/scannerate/blob/main/LICENSE"
            val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(urlIntent)
        }
        dialog.findViewById<ImageButton>(R.id.bt_close).setOnClickListener {
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.app_source_code).setOnClickListener {
            val url = "https://github.com/bumbumapp/TextScannerPro"
            val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(urlIntent)
        }
        dialog.window?.attributes = lp;
        dialog.show()
    }
}