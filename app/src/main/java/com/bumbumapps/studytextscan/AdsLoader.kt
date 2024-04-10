package com.bumbumapps.studytextscan

import android.content.Context

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdsLoader {
    var mInterstitialAd: InterstitialAd? = null
    private var TAG = "TAG"
    fun displayInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context, context.getString(R.string.interstial_id), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    mInterstitialAd = interstitialAd
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                }
            })

    }
}