package com.drbrosdev.studytextscan.persistence.entity

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Scan(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "scan_id") val scanId: Long = 0,
    @ColumnInfo(name = "scan_text") val scanText: String,
    @ColumnInfo(name = "scan_title") val scanTitle: String,
    @ColumnInfo(name = "date_created") val dateCreated: Long,
    @ColumnInfo(name = "date_modified") val dateModified: Long,
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean,
    @ColumnInfo(name="scanned_image")val scannedImage:ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Scan

        if (!scannedImage.contentEquals(other.scannedImage)) return false

        return true
    }

    override fun hashCode(): Int {
        return scannedImage.contentHashCode()
    }
}