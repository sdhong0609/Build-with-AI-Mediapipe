package com.sdhong.buildwithaimediapipe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun StylizeButton(
    uri: Uri,
    context: Context,
    helper: FaceStylizationHelper,
    onStylized: (Bitmap?) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Button(onClick = {
            val bitmap = loadBitmapFromUri(context, uri)
            bitmap?.let {
                val result = helper.stylize(it)
                onStylized(helper.convertStylizedFaceToBitmap(result))
            }
        }) {
            Text(text = "스타일 적용하기")
        }
    }
}

/**
 * Helper 함수: Uri를 Bitmap으로 변환
 */
fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        }
    } catch (e: Exception) {
        Log.e("MainScreen", "Failed to load Bitmap from Uri", e)
        null
    }
}