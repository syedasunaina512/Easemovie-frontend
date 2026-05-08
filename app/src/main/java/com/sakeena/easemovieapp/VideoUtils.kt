package com.sakeena.easemovieapp

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast

fun downloadVideo(context: Context, url: String) {
    if (url.isEmpty()) {
        Toast.makeText(context, "Video URL is missing", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("EaseMovie_Video.mp4")
            .setDescription("Downloading your AI Generated Video")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "EaseMovie_${System.currentTimeMillis()}.mp4")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun shareVideo(context: Context, url: String) {
    if (url.isEmpty()) {
        Toast.makeText(context, "Video URL is missing", Toast.LENGTH_SHORT).show()
        return
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Check out my AI Movie!")
        putExtra(Intent.EXTRA_TEXT, "I created this amazing video using EaseMovie AI: $url")
    }
    context.startActivity(Intent.createChooser(intent, "Share Video Via"))
}
