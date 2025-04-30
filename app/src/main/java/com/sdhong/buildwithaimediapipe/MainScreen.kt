package com.sdhong.buildwithaimediapipe

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    helper: FaceStylizationHelper
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var stylizedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 갤러리 열기 버튼
        GalleryPicker(
            onImageSelected = { uri ->
                selectedImageUri = uri
            }
        )

        // 선택한 이미지 표시
        selectedImageUri?.let { uri ->
            SelectedImage(uri)

            StylizeButton(
                uri = uri,
                context = context,
                helper = helper,
                onStylized = { stylizedBitmap = it }
            )

            // 스타일 적용된 이미지 표시
            stylizedBitmap?.let { styledBitmap ->
                StylizedImage(styledBitmap)
            }
        }
    }
}

@Composable
private fun SelectedImage(uri: Uri) {
    AsyncImage(
        model = uri,
        contentDescription = "Selected Image",
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1f)
    )
}

@Composable
private fun StylizedImage(bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Stylized Image",
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1f)
    )
}