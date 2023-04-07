package com.drbrosdev.studytextscan.persistence.database.converters

import android.graphics.Bitmap
import android.graphics.BitmapFactory


object DateConverter {

    fun toBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
    }

}
