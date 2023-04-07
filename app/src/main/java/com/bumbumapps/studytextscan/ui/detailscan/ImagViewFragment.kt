package com.bumbumapps.studytextscan.ui.detailscan

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.bumbumapps.studytextscan.R
import com.bumbumapps.studytextscan.databinding.ImageViewlayoutBinding
import com.bumbumapps.studytextscan.persistence.database.converters.DateConverter.toBitmap
import com.bumbumapps.studytextscan.util.collectFlow
import com.bumbumapps.studytextscan.util.updateWindowInsets
import com.bumbumapps.studytextscan.util.viewBinding
import org.koin.androidx.viewmodel.ext.android.stateViewModel


class ImagViewFragment : Fragment(R.layout.image_viewlayout) {
    private val binding: ImageViewlayoutBinding by viewBinding(ImageViewlayoutBinding::bind)
    private val viewModel: DetailScanViewModel by stateViewModel(state = { requireArguments() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateWindowInsets(binding.root)
        collectFlow(viewModel.state){ state->
            state.scan.let {
                try {
                    binding.imageViewScanned.setImageBitmap(toBitmap(it!!.scannedImage))
                }catch (e:Exception){
                    Log.d("TAG","bitmap"+e.message.toString())
                }

            }
        }

    }



}