package xyz.mufanc.imagedumper

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.MemoryFile
import android.os.ParcelFileDescriptor
import android.util.Log
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

    @Suppress("Deprecation")
    private fun doSaveImage(extras: Bundle?): String? {
        val desc = extras?.getString(KEY_DESCRIPTION) ?: return null

        val pfd: ParcelFileDescriptor = extras.getParcelable(KEY_IMAGE_FD) ?: return null
        val data = FileInputStream(pfd.fileDescriptor).readBytes()

        pfd.close()

        val filename = UUID.randomUUID().toString() + (if (desc.isNotEmpty()) "_$desc" else "") + ".png"

        val file = MediaStoreCompat.createImage(context!!, FileDescription(filename, "ImageDumper"))

        file?.openOutputStream()?.write(data) ?: return null

        return file.basePath
    }

    override fun call(method: String, args: String?, extras: Bundle?): Bundle {
        val imagePath = if (method == METHOD_SAVE_BITMAP) {
            doSaveImage(extras)
        } else {
            Log.i(TAG, "unexpected method: $method")
            null
        }

        return Bundle().apply { putString(KEY_RESULT, imagePath) }
    }

    override fun query(p0: Uri, p1: Array<String?>?, p2: String?, p3: Array<String?>?, p4: String?): Cursor? = null
    override fun getType(p0: Uri): String? = null
    override fun insert(p0: Uri, p1: ContentValues?): Uri? = null
    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int = 0
    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int = 0

    companion object {

        private const val METHOD_SAVE_BITMAP = "save_bitmap"

        private const val KEY_IMAGE_FD = "bitmap"

        private const val KEY_DESCRIPTION = "description"

        private const val KEY_RESULT = "result"

        fun save(context: Context, bitmap: Bitmap, filename: String? = ""): String? {
            val resolver = context.applicationContext.contentResolver

            val data = BitmapHelper.encode(bitmap)
            val file = MemoryFile("bitmap", data.size)

            file.writeBytes(data, 0, 0, data.size)

            val fd: FileDescriptor = Reflect.on(file).call("getFileDescriptor").get()
            val pfd = ParcelFileDescriptor.dup(fd)

            val extras = Bundle().apply {
                putParcelable(KEY_IMAGE_FD, pfd)
                putString(KEY_DESCRIPTION, filename ?: "")
            }

            val result = resolver.call(
                Uri.parse("content://${BuildConfig.APPLICATION_ID}.imagesaver"),
                METHOD_SAVE_BITMAP, "", extras
            )
            val savedPath = result?.getString(KEY_RESULT)

            Log.i(TAG, "image saved: $savedPath")

            pfd.close()
            file.close()

            return savedPath
        }
    }
}
