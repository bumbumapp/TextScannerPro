package com.bumbumapps.studytextscan.ui.detailscan

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumbumapps.studytextscan.Constant.BOTH
import com.bumbumapps.studytextscan.Constant.HORIZONTAL
import com.bumbumapps.studytextscan.Constant.VERTICAL
import com.bumbumapps.studytextscan.R
import com.bumbumapps.studytextscan.anim.InsetsWithKeyboardAnimationCallback
import com.bumbumapps.studytextscan.anim.InsetsWithKeyboardCallback
import com.bumbumapps.studytextscan.databinding.FragmentScanDetailBinding
import com.bumbumapps.studytextscan.datastore.AppPreferences
import com.bumbumapps.studytextscan.datastore.datastore
import com.bumbumapps.studytextscan.persistence.database.converters.DateConverter.toBitmap
import com.bumbumapps.studytextscan.service.pdfExport.PdfExportServiceImpl
import com.bumbumapps.studytextscan.util.*
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import java.util.*


class DetailScanFragment : Fragment(R.layout.fragment_scan_detail) {
    private val binding: FragmentScanDetailBinding by viewBinding(FragmentScanDetailBinding::bind)
    private val viewModel: DetailScanViewModel by stateViewModel(state = { requireArguments() })
    private val pdfExportService: PdfExportServiceImpl by inject()
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateWindowInsets(binding.root)
        val datastore = AppPreferences(requireContext().datastore)

        val insetsWithKeyboardCallback = InsetsWithKeyboardCallback(requireActivity().window)
        ViewCompat.setWindowInsetsAnimationCallback(binding.root, insetsWithKeyboardCallback)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, insetsWithKeyboardCallback)

        val insetsWithKeyboardAnimationCallback = InsetsWithKeyboardAnimationCallback(binding.bottomBar)
        ViewCompat.setWindowInsetsAnimationCallback(binding.bottomBar, insetsWithKeyboardAnimationCallback)

        collectFlow(viewModel.state) { state ->
            state.scan?.let { scan ->
                binding.apply {
                    textViewDateCreated.text =
                        getString(R.string.text_date_created, dateAsString(scan.dateCreated))
                    textViewDateModified.text =
                        getString(R.string.text_date_modified, dateAsString(scan.dateModified))


                    editTextScanContent.setText(scan.scanText, TextView.BufferType.EDITABLE)
                    editTextScanContent.movementMethod=null
                    editTextScanContent2.setText(scan.scanText, TextView.BufferType.EDITABLE)
                    imageViewScanned.setImageBitmap(toBitmap(scan.scannedImage))
                    lifecycleScope.launch{
                        if(datastore.isHorizontal.first()== BOTH) {
                            binding.scrollView.visibility=View.VISIBLE
                            binding.editTextScanContent2.visibility=View.INVISIBLE
                        }
                        if(datastore.isHorizontal.first()== HORIZONTAL) {
                            binding.scrollView.visibility=View.INVISIBLE
                            binding.editTextScanContent2.visibility=View.VISIBLE
                           editTextScanContent2.setHorizontallyScrolling(true)
                        }
                        if(datastore.isHorizontal.first()== VERTICAL){
                            binding.scrollView.visibility=View.INVISIBLE
                            binding.editTextScanContent2.visibility=View.VISIBLE
                            editTextScanContent2.setHorizontallyScrolling(false)
                        }
                    }


                    val pinColor = if (scan.isPinned) getColor(R.color.heavy_blue)
                        else getColor(R.color.light_blue)
                    imageViewPin.setColorFilter(pinColor)
                    binding.cardImageViewScanned.setOnClickListener {
                        val arg = bundleOf("scan_id" to scan.scanId.toInt())
                        findNavController().navigate(R.id.action_detailScanFragment_to_imagViewFragment,arg)
                    }
                    binding.settings.setOnClickListener {
                        val popup =PopupMenu(requireContext(), it);
                        popup.setOnMenuItemClickListener(object:PopupMenu.OnMenuItemClickListener{
                            override fun onMenuItemClick(menu: MenuItem?): Boolean {
                                when (menu?.itemId) {
                                    R.id.horizontol -> {
                                        binding.scrollView.visibility=View.INVISIBLE
                                        binding.editTextScanContent2.visibility=View.VISIBLE
                                        binding.editTextScanContent2.setHorizontallyScrolling(true)
                                        lifecycleScope.launch{
                                            datastore.setViewNotHorizontal(HORIZONTAL)
                                        }
                                        viewModel.updateScan(editTextScanContent.text.toString())
                                        return true
                                    }
                                    R.id.vertical -> {
                                        binding.scrollView.visibility=View.INVISIBLE
                                        binding.editTextScanContent2.visibility=View.VISIBLE
                                        binding.editTextScanContent2.setHorizontallyScrolling(false)
                                        lifecycleScope.launch{
                                            datastore.setViewNotHorizontal(VERTICAL)
                                        }
                                        viewModel.updateScan(editTextScanContent2.text.toString())

                                        return true
                                    }
                                    R.id.both-> {
                                        binding.scrollView.visibility=View.VISIBLE
                                        binding.editTextScanContent2.visibility=View.INVISIBLE
                                        lifecycleScope.launch{
                                            datastore.setViewNotHorizontal(BOTH)
                                        }
                                        viewModel.updateScan(editTextScanContent2.text.toString())

                                        return true
                                    }

                                }
                                return true
                            }

                        });
                        popup.inflate(R.menu.view);
                        popup.show()
                    }
                    recyclerViewChips.withModels {
                        state.filteredTextModels.let {
                            it.forEach { model ->
                                chip {
                                    id(model.filteredTextModelId)
                                    onModelClick {
                                        processFilteredModelIntent(model.type, model.content)
                                    }
                                    model(model)
                                    initCard { cardView ->
                                        when (model.type) {
                                            "phone" -> {
                                                cardView.setCardBackgroundColor(getColor(R.color.chip_green))
                                            }
                                            "email" -> {
                                                cardView.setCardBackgroundColor(getColor(R.color.chip_orange))
                                            }
                                            "link" -> {
                                                cardView.setCardBackgroundColor(getColor(R.color.chip_yellow))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        collectFlow(viewModel.events) {
            when (it) {
                is DetailScanEvents.ShowSoftwareKeyboardOnFirstLoad -> {
//                    showKeyboardOnEditText(binding.editTextScanTitle)
                }
                is DetailScanEvents.ShowScanUpdated -> {
                    showSnackbarShort(
                        getString(R.string.scan_updated),
                        anchor = binding.imageViewCopy
                    )
                }
                is DetailScanEvents.ShowUnsavedChanges -> {
                    showConfirmDialog(
                        title = getString(R.string.save_changes),
                        message = getString(R.string.unsaved_changes),
                        onPositiveClick = {
                            binding.apply {
                                var editText: String? =null
                                lifecycleScope.launch{

                                    editText = if (datastore.isHorizontal.first()== VERTICAL){
                                        editTextScanContent2.text.toString()
                                    } else{
                                        editTextScanContent.text.toString()
                                    }
                                }

                                viewModel.updateScan(
                                    content = editText!!
                                )
                            }
                            hideKeyboard()
                            findNavController().navigateUp()
                        },
                        onNegativeClick = {
                            hideKeyboard()
                            findNavController().navigateUp()
                        }
                    )
                }
                is DetailScanEvents.NavigateUp -> {
                    hideKeyboard()
                    findNavController().navigateUp()
                }
            }
        }

        /*
        Attach a callback when the back button is pressed to act the same way as the
        imageViewBack does.
         */
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            binding.apply {
                var editText1: String? =null
                lifecycleScope.launch{

                    editText1 = if (datastore.isHorizontal.first()== VERTICAL){
                        editTextScanContent2.text.toString()
                    } else{
                        editTextScanContent.text.toString()
                    }
                }
                viewModel.onNavigateUp(
                    content = editText1!!
                )
            }
        }


        /*
        Click events
         */
        binding.apply {
            imageViewPin.setOnClickListener {
                viewModel.updateScanPinned()
                imageViewPin.setColorFilter(getColor(R.color.light_blue))
            }

            imageViewSave.setOnClickListener {
                var editText: String? =null
                lifecycleScope.launch{

                    editText = if (datastore.isHorizontal.first()== VERTICAL){
                        editTextScanContent2.text.toString()
                    } else{
                        editTextScanContent.text.toString()
                    }
                }
                viewModel.updateScan(
                    content = editText!!
                )
                hideKeyboard()
            }

            imageViewBack.setOnClickListener {
                var editText: String? =null
                lifecycleScope.launch{

                    editText = if (datastore.isHorizontal.first()== VERTICAL){
                        editTextScanContent2.text.toString()
                    } else{
                        editTextScanContent.text.toString()
                    }
                }
                viewModel.onNavigateUp(
                    content = editText!!
                )
            }

            imageViewDelete.setOnClickListener {
                showConfirmDialog(
                    message = getString(R.string.delete_scanned_text),
                    onPositiveClick = {
                        viewModel.deleteScan()
                        hideKeyboard()
                        findNavController().navigateUp()
                    }
                )
            }

            imageViewCopy.setOnClickListener {
                val clipboardManager =
                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                var editText: String? =null
                lifecycleScope.launch{

                    editText = if (datastore.isHorizontal.first()== VERTICAL){
                        editTextScanContent2.text.toString()
                    } else{
                        editTextScanContent.text.toString()
                    }
                }
                val clip = ClipData.newPlainText("raw_data", editText)
                clipboardManager.setPrimaryClip(clip)
                showSnackbarShort(
                    message = getString(R.string.copied_clip),
                    anchor = binding.imageViewTranslate
                )
            }

            imageViewShare.setOnClickListener {
                var editText: String? =null
                lifecycleScope.launch{

                    editText = if (datastore.isHorizontal.first()== VERTICAL){
                        editTextScanContent2.text.toString()
                    } else{
                        editTextScanContent.text.toString()
                    }
                }
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, editText)
                    type = "text/plain"
                }
                val intent = Intent.createChooser(shareIntent, null)
                startActivity(intent)
            }

            imageViewVoice.setOnClickListener {
                textToSpeech = TextToSpeech(requireContext()) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        val hasLanguage = textToSpeech.setLanguage(Locale.US)
                        if (hasLanguage == TextToSpeech.LANG_MISSING_DATA || hasLanguage == TextToSpeech.LANG_NOT_SUPPORTED) {
                            showSnackbarShort(
                                getString(R.string.unsupported_language),
                                anchor = imageViewShare
                            )
                        } else {
                            showSnackbarShort(getString(R.string.loading), anchor = imageViewShare)
                            textToSpeech.speak(
                                editTextScanContent.text.toString(),
                                TextToSpeech.QUEUE_ADD,
                                null,
                                viewModel.scanId.toString()
                            )
                        }
                    }
                }
            }

            imageViewTranslate.setOnClickListener {
                try {
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.putExtra(Intent.EXTRA_TEXT, editTextScanContent.text.toString().trim())
                    intent.putExtra("key_text_input", editTextScanContent.text.toString().trim())
                    intent.putExtra("key_text_output", "")
                    intent.putExtra("key_language_from", "en")
                    intent.putExtra("key_language_to", "mal")
                    intent.putExtra("key_suggest_translation", "")
                    intent.putExtra("key_from_floating_window", false)
                    intent.component = ComponentName(
                        "com.google.android.apps.translate",
                        "com.google.android.apps.translate.TranslateActivity"
                    )
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    showSnackbarShort(
                        message = getString(R.string.no_google_translate),
                        anchor = binding.imageViewShare
                    )
                }
            }

            imageViewPdf.setOnClickListener {
                val arg = bundleOf("pdf_scan_id" to viewModel.scanId)
                findNavController().navigate(
                    R.id.action_detailScanFragment_to_pdfDialogFragment,
                    arg
                )
            }
        }
    }

    override fun onDestroyView() {
        /*
        First check if TTS is initialized and if it is (this means its reading)
        stop reading once fragment is closed.
         */
        if (this::textToSpeech.isInitialized) textToSpeech.stop()
        super.onDestroyView()
    }

    private fun processFilteredModelIntent(type: String, content: String) {
        try {
            when (type) {
                "phone" -> {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$content")
                    }
                    if (requireActivity().packageManager != null)
                        startActivity(dialIntent)
                }
                "email" -> {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(content))
                    }
                    if (requireActivity().packageManager != null)
                        startActivity(emailIntent)
                }
                "link" -> {
                    val urlIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(content)
                    }
                    if (requireActivity().packageManager != null)
                        startActivity(urlIntent)
                }
            }
        } catch (e: ActivityNotFoundException) {
            showSnackbarShort(
                message = getString(R.string.something_went_wrong),
                anchor = binding.imageViewCopy
            )
        }
    }
}


