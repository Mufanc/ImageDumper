package xyz.mufanc.imagedumper

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.MemoryFile
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import com.anggrayudi.storage.media.FileDescription
import com.anggrayudi.storage.media.MediaStoreCompat
import org.joor.Reflect
import xyz.mufanc.imagedumper.ModuleMain.Companion.TAG
import java.io.FileDescriptor
import java.io.FileInputStream
import java.util.UUID

class ImageSaver : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    @Suppress("Deprecation", "Unchecked_Cast")
    private fun doSaveImage(extras: Bundle?): Boolean {
        if (extras == null) return false

        extras.classLoader = javaClass.classLoader

        val images = extras.getParcelableArray(KEY_IMAGE_DATA) ?: return false

        var success = true

        images.forEach { image ->
            image as ImageData

            Log.i(TAG, "data: ${image.data}, len: ${image.data.size}")

            val desc = image.description
            val filename = UUID.randomUUID().toString() + (if (desc.isNotEmpty()) "_$desc" else "") + ".png"

            val file = MediaStoreCompat.createImage(context!!, FileDescription(filename, "ImageDumper"))
            if (file?.openOutputStream()?.write(image.data) == null) {
                success = false
            }

            if (file != null) {
                Log.i(TAG, "image saved to: ${file.basePath}")
            }
        }

        return success
    }

    override fun call(method: String, args: String?, extras: Bundle?): Bundle {
        val success = if (method == METHOD_SAVE_IMAGES) {
            try {
                doSaveImage(extras)
            } catch (err: Throwable) {
                Log.e(TAG, "", err)
                false
            }
        } else {
            Log.i(TAG, "unexpected method: $method")
            false
        }

        return Bundle().apply { putBoolean(KEY_SUCCESS, success) }
    }

    override fun query(p0: Uri, p1: Array<String?>?, p2: String?, p3: Array<String?>?, p4: String?): Cursor? = null
    override fun getType(p0: Uri): String? = null
    override fun insert(p0: Uri, p1: ContentValues?): Uri? = null
    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int = 0
    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int = 0

    companion object {

        private const val METHOD_SAVE_IMAGES = "save_images"

        private const val KEY_IMAGE_DATA = "image_data"

        private const val KEY_SUCCESS = "success"

        private val targetUri = Uri.parse("content://${BuildConfig.APPLICATION_ID}.imagesaver")

        private val resolver by lazy { MixedContext.hostAppContext.contentResolver }

        private fun isSuccess(result: Bundle?): Boolean {
            return result?.getBoolean(KEY_SUCCESS) ?: false
        }

        private fun saveImages(dataArr: List<ImageData>): Boolean {
            val success = isSuccess(
                resolver.call(
                    targetUri,
                    METHOD_SAVE_IMAGES,
                    "",
                    Bundle().apply { putParcelableArray(KEY_IMAGE_DATA, dataArr.toTypedArray()) }
                )
            )

            if (success) {
                Toast.makeText(
                    MixedContext.hostAppContext,
                    MixedContext.modulePackageContext.getString(R.string.message_save_image_success),
                    Toast.LENGTH_SHORT
                ).show()
            }

            return success
        }

        fun save(bitmap: Bitmap, desc: String? = ""): Boolean {
            ImageData(bitmap, desc).use { data ->
                return saveImages(listOf(data))
            }
        }

        fun save(images: List<Bitmap>): Boolean {
            val imageDataList = images.map { ImageData(it) }
            val success = saveImages(imageDataList)

            imageDataList.map(ImageData::close)

            return success
        }
    }

    class ImageData private constructor(
        val data: ByteArray,
        val description: String
    ) : Parcelable, AutoCloseable {

        private var mfile: MemoryFile? = null
        private var fd: FileDescriptor? = null
        private var pfd: ParcelFileDescriptor? = null

        constructor(bitmap: Bitmap, description: String? = null) : this(
            Utils.encodeImage(bitmap),
            description ?: ""
        ) {
            mfile = MemoryFile("bitmap@${bitmap.hashCode().toString(16)}", data.size)
            fd = Reflect.on(mfile).call("getFileDescriptor").get()
            pfd = ParcelFileDescriptor.dup(fd)

            mfile!!.writeBytes(data, 0, 0, data.size)
        }

        @Suppress("Deprecation")
        constructor(parcel: Parcel) : this(
            parcel.readParcelable<ParcelFileDescriptor>(ParcelFileDescriptor::class.java.classLoader)
                .use { FileInputStream(it!!.fileDescriptor).readBytes() },
            parcel.readString()!!
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(pfd, flags)
            parcel.writeString(description)
        }

        override fun describeContents(): Int {
            return Parcelable.CONTENTS_FILE_DESCRIPTOR
        }

        override fun close() {
            pfd?.close()
            mfile?.close()
        }

        companion object CREATOR : Parcelable.Creator<ImageData> {
            override fun createFromParcel(parcel: Parcel): ImageData {
                return ImageData(parcel)
            }

            override fun newArray(size: Int): Array<ImageData?> {
                return arrayOfNulls(size)
            }
        }
    }
}
