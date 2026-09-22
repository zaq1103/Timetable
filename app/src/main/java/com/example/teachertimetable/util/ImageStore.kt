package com.example.teachertimetable.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ImageStore {

    fun memoDir(context: Context): File =
        File(context.filesDir, "memo_images").apply { if (!exists()) mkdirs() }

    fun newImageFile(context: Context): File =
        File(memoDir(context), "IMG_" + System.currentTimeMillis() + ".jpg")

    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)

    /** 把外部 Uri 拷贝进内部存储，返回绝对路径 */
    fun importFromUri(context: Context, uri: Uri): String? = runCatching {
        val dest = newImageFile(context)
        context.contentResolver.openInputStream(uri)!!.use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
        dest.absolutePath
    }.getOrNull()

    fun decodeScaled(path: String, maxSide: Int = 1600): Bitmap? = runCatching {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, opts)
        var scale = 1
        while (opts.outWidth / scale > maxSide || opts.outHeight / scale > maxSide) scale *= 2
        val opts2 = BitmapFactory.Options().apply { inSampleSize = scale }
        BitmapFactory.decodeFile(path, opts2)
    }.getOrNull()

    fun saveBitmap(bitmap: Bitmap, path: String): Boolean = runCatching {
        FileOutputStream(path).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        true
    }.getOrDefault(false)

    fun saveBitmapAsNew(context: Context, bitmap: Bitmap): String? {
        val f = File(memoDir(context), "EDIT_" + System.currentTimeMillis() + ".jpg")
        return if (saveBitmap(bitmap, f.absolutePath)) f.absolutePath else null
    }
}
