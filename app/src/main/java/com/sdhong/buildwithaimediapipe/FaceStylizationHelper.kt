package com.sdhong.buildwithaimediapipe

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.createBitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.ByteBufferExtractor
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.facestylizer.FaceStylizer
import com.google.mediapipe.tasks.vision.facestylizer.FaceStylizer.FaceStylizerOptions
import com.google.mediapipe.tasks.vision.facestylizer.FaceStylizerResult
import kotlin.jvm.optionals.getOrNull

class FaceStylizationHelper(
    private val modelPosition: Int,
    private val context: Context,
    var faceStylizerListener: FaceStylizerListener? = null
) {

    private var faceStylizer: FaceStylizer? = null

    init {
        setupFaceStylizer()
    }

    /**
     * FaceStylizer를 초기화합니다.
     * 선택된 modelPosition에 따라 다른 모델 파일을 로드합니다.
     */
    private fun setupFaceStylizer() {
        val baseOptionsBuilder = BaseOptions.builder()
        // Sets the model selection.
        baseOptionsBuilder.setModelAssetPath(
            when (modelPosition) {
                0 -> MODEL_PATH_COLOR_SKETCH
                1 -> MODEL_PATH_COLOR_INK
                2 -> MODEL_PATH_OIL_PAINTING
                else -> throw Throwable("Invalid model type position")
            }
        )

        try {
            // 옵션 빌드 및 FaceStylizer 인스턴스 생성
            val baseOptions = baseOptionsBuilder.build()
            val optionsBuilder = FaceStylizerOptions.builder()
                .setBaseOptions(baseOptions)

            val options = optionsBuilder.build()
            faceStylizer = FaceStylizer.createFromOptions(context, options)
        } catch (e: IllegalStateException) {
            // 일반적인 초기화 오류 처리
            faceStylizerListener?.onError(
                "Face stylizer failed to initialize. See error logs for " +
                        "details"
            )
            Log.e(
                TAG,
                "Face stylizer failed to load model with error: " + e.message
            )
        } catch (e: RuntimeException) {
            // GPU 미지원 기기에서 발생할 수 있는 오류 처리
            // This occurs if the model being used does not support GPU
            faceStylizerListener?.onError(
                "Face stylizer failed to initialize. See error logs for " +
                        "details", GPU_ERROR
            )
            Log.e(
                TAG,
                "Face stylizer failed to load model with error: " + e.message
            )
        }
    }

    /**
     * 주어진 Bitmap을 스타일 변환하여 결과를 반환합니다.
     * - 변환 시간(inference time)을 함께 측정합니다.
     */
    fun stylize(bitmap: Bitmap): ResultBundle {
        val mpImage = BitmapImageBuilder(bitmap).build()
        var timestampMs = System.currentTimeMillis()
        val result = faceStylizer?.stylize(mpImage)
        timestampMs = System.currentTimeMillis() - timestampMs

        return ResultBundle(result, timestampMs)
    }

    /**
     * 스타일링 결과를 Bitmap으로 변환합니다.
     *
     * @param result 스타일링된 얼굴 정보와 추론 시간을 포함한 ResultBundle
     * @return 스타일링된 얼굴 이미지의 Bitmap, 또는 스타일링 결과가 없는 경우 null
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun convertStylizedFaceToBitmap(result: ResultBundle): Bitmap? {
        // 결과가 없거나 스타일링된 이미지가 없는 경우 null 반환
        if (result.stylizedFace == null || result.stylizedFace.stylizedImage().getOrNull() == null) {
            return null
        }

        // 스타일링된 얼굴 이미지 정보 가져오기
        val image = result.stylizedFace
        // 이미지 데이터를 ByteBuffer로 추출
        val byteBuffer = ByteBufferExtractor.extract(image.stylizedImage().get())

        // 이미지 크기 정보 가져오기
        val width = image.stylizedImage().get().width
        val height = image.stylizedImage().get().height

        // 추출한 정보로 새 Bitmap 생성
        val bitmap = createBitmap(width, height)
        // ByteBuffer의 픽셀 데이터를 Bitmap에 복사
        bitmap.copyPixelsFromBuffer(byteBuffer)
        return bitmap
    }


    /**
     * 사용이 끝난 후 FaceStylizer 리소스를 해제합니다.
     */
    fun close() {
        faceStylizer?.close()
    }

    /**
     * 스타일링 결과와 처리 시간을 묶어 반환하는 데이터 클래스입니다.
     */
    data class ResultBundle(
        val stylizedFace: FaceStylizerResult?, // 스타일링된 결과 (FaceStylizerResult)
        val inferenceTime: Long,  // 처리 소요 시간 (ms 단위)
    )

    companion object {
        // 모델 파일 경로 상수
        const val MODEL_PATH_OIL_PAINTING = "face_stylizer_oil_painting.task"
        const val MODEL_PATH_COLOR_INK = "face_stylizer_color_ink.task"
        const val MODEL_PATH_COLOR_SKETCH = "face_stylizer_color_sketch.task"
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1
        private const val TAG = "FaceStylizationHelper"
    }


    /**
     * FaceStylizer 오류 발생 시 호출되는 리스너 인터페이스
     */
    interface FaceStylizerListener {
        fun onError(error: String, errorCode: Int = OTHER_ERROR)
    }
}