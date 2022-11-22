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
    //Serializable -> Parcelable로 하는 이유 : 속도처리, 용량처리
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
    //앨범 Uri 가져오기
    fun getAlbumUri(): Uri {
        return Uri.parse("content://media/external/audio/albumart/" + albumId)
    }
    //음악 Uri 가져오기
    fun getMusicUri(): Uri {
        //음악의 위치(이미지 데이터)를 가져와서 화면에 출력하기 위한 방법
        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
    }
    //음악앨범의 비트맵을 가져와서 원하는 사이즈로 비트맵을 만든다.
    //음악파일에 앨범이미지가 따로 없을 수 있으니 리턴값인 Bitmap에 null safety(?)을 넣는다
    fun getAlbumImage(context: Context, albumImageSize: Int): Bitmap? {
        val contentResolver: ContentResolver = context.contentResolver
        //모듈화했던 앨범 uri(경로) 가져오기
        val uri = getAlbumUri()
        //비트맵 옵션
        val options = BitmapFactory.Options()

        if (uri != null) {
            //파일 생성
            var parceFileDescriptor: ParcelFileDescriptor? = null
            //외부에서 가져오기때문에 try,catch 사용
            try {
                //앨범uri를 파일로 가져온다.
                parceFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                var bitmap = BitmapFactory.decodeFileDescriptor(
                    parceFileDescriptor!!.fileDescriptor,
                    null,
                    options
                )

                //비트맵을 가져왔는데 원하는 사이즈가 아닐 경우
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
