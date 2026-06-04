package re.melchior.saviomobile.ui.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Base64
import re.melchior.saviomobile.ui.screen.intervention.cloture.DrawPoint
import java.io.ByteArrayOutputStream
import java.io.File

fun encodeSignaturePointsToBase64(
    points: List<DrawPoint>,
    width: Int,
    height: Int,
): String? {
    if (points.isEmpty() || width <= 0 || height <= 0) return null
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    val paint =
        Paint().apply {
            color = android.graphics.Color.BLACK
            strokeWidth = 4f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
    val path = Path()
    points.forEach { point ->
        if (point.isStart) path.moveTo(point.x, point.y)
        else path.lineTo(point.x, point.y)
    }
    canvas.drawPath(path, paint)
    val out = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
}

fun saveSignatureBase64ToFile(
    filesDir: File,
    base64: String,
    fileName: String,
): String? {
    return runCatching {
        val bytes = Base64.decode(base64, Base64.NO_WRAP)
        val dir = File(filesDir, "signatures").apply { mkdirs() }
        val file = File(dir, fileName)
        file.writeBytes(bytes)
        file.absolutePath
    }.getOrNull()
}
