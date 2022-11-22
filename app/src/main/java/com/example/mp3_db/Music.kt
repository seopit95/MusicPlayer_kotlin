package com.example.mp3_db

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
class Music(val id: String?, val title: String?, val artist: String?, val albumId: String?, val duration: Int?, var likes: Int?, var repeat: Int?): Parcelable {
    companion object : Parceler<Music> {
        override fun create(parcel: Parcel): Music {
            return Music(parcel)
        }

        override fun Music.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(title)
            parcel.writeString(artist)
            parcel.writeString(albumId)
            parcel.writeInt(duration!!)
            parcel.writeInt(likes!!)
            parcel.writeInt(repeat!!)
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    fun getAlbumUri(): Uri {
        return Uri.parse("content://media/external/audio/albumart/" + albumId)
    }

    fun getMusicUri(): Uri {
        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
    }

    fun getAlbumImage(context: Context, albumImageSize: Int): Bitmap? {
        val contentResolver: ContentResolver = context.contentResolver
        val uri = getAlbumUri()
        val options = BitmapFactory.Options()

        if (uri != null) {
            var parceFileDescriptor: ParcelFileDescriptor? = null
            try {
                parceFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                var bitmap = BitmapFactory.decodeFileDescriptor(
                    parceFileDescriptor!!.fileDescriptor,
                    null,
                    options
                )

                if (bitmap != null) {
                    val tempBitmap =
                        Bitmap.createScaledBitmap(bitmap, albumImageSize, albumImageSize, true)
                    bitmap.recycle()
                    bitmap = tempBitmap
                }
                return bitmap
            } catch (e: java.lang.Exception) {
                Log.d("mp3_db", "getAlbumImage() ${e.toString()}")
            } finally {
                try {
                    parceFileDescriptor?.close()
                } catch (e: java.lang.Exception) {
                    Log.d("mp3_db", "getAlbumImage() parceFileDescriptor ${e.toString()}")
                }
            }
        }
        return null
    }

}
