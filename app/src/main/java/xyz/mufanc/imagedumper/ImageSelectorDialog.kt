package xyz.mufanc.imagedumper

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.util.ArraySet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import xyz.mufanc.imagedumper.databinding.DialogImageSelectorBinding
import xyz.mufanc.imagedumper.databinding.ItemDialogImageSelectorBinding

class ImageSelectorDialog(
    context: Context,
    private val images: List<Bitmap>
) {

    private val chosen = ArraySet<Int>()

    private val dialog = MaterialAlertDialogBuilder(MixedContext(context))
        .setTitle(R.string.dialog_image_selector_title)
        .setPositiveButton(R.string.dialog_image_selector_save) { _, _ -> }
        .setNegativeButton(R.string.dialog_image_selector_cancel) { _, _ -> }
        .create()
        .apply {
            val binding = DialogImageSelectorBinding.inflate(layoutInflater)
            setView(binding.root)

            binding.images.run {
                layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
                adapter = Adapter(images, chosen)
            }

            overridePrimaryButton(this)
        }

    private fun overridePrimaryButton(dialog: AlertDialog) = dialog.run {
        setOnShowListener {
            // override listener
            val button = getButton(DialogInterface.BUTTON_POSITIVE)

            button.setOnClickListener {
                if (chosen.isEmpty()) {
                    Toast.makeText(
                        MixedContext.hostAppContext,
                        context.getString(R.string.dialog_image_selector_toast),
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                ImageSaver.save(chosen.map { images[it] })
                dismiss()
            }
        }
    }

    private class Adapter(
        private val images: List<Bitmap>,
        private val chosen: MutableSet<Int>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        fun <T> MutableSet<T>.toggle(item: T) {
            if (contains(item)) {
                remove(item)
            } else {
                add(item)
            }
        }

        override fun getItemCount(): Int = images.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemDialogImageSelectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.preview.run {
                setImageBitmap(images[position])

                setOnClickListener {
                    chosen.toggle(position)
                    isSelected = !isSelected
                }
            }
        }

        class ViewHolder(
            binding: ItemDialogImageSelectorBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            val preview = binding.preview
        }
    }

    fun show() {
        dialog.show()
    }
}
