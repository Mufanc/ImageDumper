package xyz.mufanc.imagedumper

import android.graphics.Bitmap
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import xyz.mufanc.imagedumper.databinding.DialogPreviewBinding

class PreviewDialog(
    private val iv: View,
    private val bitmap: Bitmap?
) {

    private val context = MixedContext(iv.context)

    private val dialog = MaterialAlertDialogBuilder(context)
        .setTitle(R.string.dialog_preview_title)
        .setPositiveButton(R.string.dialog_preview_btn_click) { _, _ ->
            Utils.performClick(iv)
        }
        .setNegativeButton(R.string.dialog_preview_btn_long_click) { _, _ ->
            Utils.performLongClick(iv)
        }
        .apply {
            if (bitmap != null) {
                setNeutralButton(R.string.dialog_preview_btn_save_image) { _, _ ->
                    ImageSaver.save(bitmap, iv.contentDescription?.toString())
                }
            }
        }
        .create()
        .apply {
            val binding = DialogPreviewBinding.inflate(layoutInflater)

            if (bitmap != null) {
                binding.preview.setImageBitmap(bitmap)
            } else {
                binding.preview.visibility = View.GONE
                binding.message.visibility = View.VISIBLE
            }

            setView(binding.root)
        }

    fun show() {
        dialog.show()
    }
}
