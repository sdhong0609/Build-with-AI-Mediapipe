package com.sdhong.buildwithaimediapipe

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.sdhong.buildwithaimediapipe.ui.theme.BuildWithAIMediapipeTheme

class MainActivity : ComponentActivity() {

    private lateinit var faceStylizationHelper: FaceStylizationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO
        // Activity 레벨에서 Helper 생성
        faceStylizationHelper = FaceStylizationHelper(
            modelPosition = 0, // 기본 모델 포지션 설정 (예: Color Sketch)
            context = this,
            faceStylizerListener = object : FaceStylizationHelper.FaceStylizerListener {
                override fun onError(error: String, errorCode: Int) {
                    Log.e("MainActivity", "FaceStylizer Error: $error (code: $errorCode)")
                    // 필요하면 Toast 띄우거나 UI 업데이트 가능
                }
            }
        )

        enableEdgeToEdge()

        setContent {
            BuildWithAIMediapipeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // TODO
                    // MainScreen에 helper를 넘긴다!
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        helper = faceStylizationHelper
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Activity 종료 시 리소스 해제
        faceStylizationHelper.close()
    }
}