package xyz.mufanc.imagedumper

import android.graphics.Bitmap
import android.graphics.Shader
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import xyz.mufanc.imagedumper.ModuleMain.Companion.TAG
import xyz.mufanc.imagedumper.databinding.DialogPreviewBinding

class PreviewDialog(
    private val iv: View,
    private val bitmap: Bitmap
) {

    private val context = ModuleThemeContext(iv.context)

    private fun performClick() {
        var current: View? = iv
        while (current != null && !current.performClick()) {
            current = current.parent as? View
        }
    }

    private fun performLongClick() {
        var current: View? = iv
        while (current != null && !current.performLongClick()) {
            current = current.parent as? View
        }
    }

    private val dialog = MaterialAlertDialogBuilder(context)
        .setTitle("图片预览")
        .setPositiveButton("👉点击") { _, _ ->
            performClick()
        }
        .setNegativeButton("👉长按") { _, _ ->
            performLongClick()
        }
        .setNeutralButton("保存图片") { _, _ ->
            ImageSaver.save(context, bitmap, iv.contentDescription?.toString())
        }
        .create()
        .apply {
            val binding = DialogPreviewBinding.inflate(layoutInflater)

            binding.preview.setImageBitmap(bitmap)
            binding.preview.background = TileDrawable(
                AppCompatResources.getDrawable(context, R.drawable.bg_transparent)!!,
                Shader.TileMode.REPEAT
            )

            setView(binding.root)
        }

    fun show() {
        dialog.show()
    }
}
