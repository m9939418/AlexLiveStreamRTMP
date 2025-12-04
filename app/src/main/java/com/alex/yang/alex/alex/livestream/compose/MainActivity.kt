package com.alex.yang.alex.alex.livestream.compose

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alex.yang.alex.alex.livestream.compose.feature.stream.presentation.StreamScreen
import com.alex.yang.alex.alex.livestream.compose.feature.stream.presentation.StreamViewModel
import com.alex.yang.alex.alex.livestream.compose.ui.theme.AlexLiveStreamComposeTheme
import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.gl.render.filters.BeautyFilterRender
import com.pedro.encoder.input.gl.render.filters.BlurFilterRender
import com.pedro.encoder.input.gl.render.filters.CartoonFilterRender
import com.pedro.library.rtmp.RtmpCamera2
import com.pedro.library.view.OpenGlView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var rtmpCamera2: RtmpCamera2? = null
    private var openGlView: OpenGlView? = null
    private var isBeautyOn: Boolean = false
    private var isCartoonOn: Boolean = false
    private var isBlurOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlexLiveStreamComposeTheme {
                val viewModel = hiltViewModel<StreamViewModel>()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                // 權限請求
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = { }
                )

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                        )
                    )
                }

                StreamScreen(
                    uiState = state,
                    onUrlChanged = viewModel::onUrlChanged,
                    onAttachOpenGlView = { view ->
                        if (openGlView == null) {
                            openGlView = view
                            rtmpCamera2 = RtmpCamera2(
                                view,
                                object : ConnectChecker {
                                    override fun onConnectionStarted(url: String) {
                                        viewModel.onStreamConnecting()
                                    }

                                    override fun onConnectionSuccess() {
                                        viewModel.onStreamStarted()
                                    }

                                    override fun onConnectionFailed(reason: String) {
                                        viewModel.onStreamError(reason)
                                        rtmpCamera2?.stopStream()
                                    }

                                    override fun onDisconnect() {
                                        viewModel.onStreamStopped()
                                    }

                                    override fun onAuthError() {
                                        viewModel.onStreamError("Auth error")
                                    }

                                    override fun onAuthSuccess() {}
                                }
                            )
                        }
                    },
                    onStartStreamClick = { startStreamAndCamera(viewModel, state) },
                    onStopStreamClick = { stopStreamAndCamera(viewModel) },
                    onBeautyClick = { toggleBeautyFilter() },
                    onCartoonClick = { toggleCartoonFilter() },
                    onBlurClick = { toggleBlurFilter() },
                    onSwitchCameraClick = { switchCamera() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rtmpCamera2?.let { camera ->
            if (camera.isStreaming) camera.stopStream()
            if (camera.isOnPreview) camera.stopPreview()
        }
        rtmpCamera2 = null
        openGlView = null
    }

    private fun startStreamAndCamera(vm: StreamViewModel, state: StreamViewModel.UiState) {
        val url = state.rtmpUrl.trim()
        val camera = rtmpCamera2
        val preview = openGlView

        if (camera == null || preview == null) {
            vm.onStreamError("預覽尚未初始化，請稍後再試")
            return
        }

        if (url.isBlank()) {
            vm.onStreamError("請先輸入 RTMP URL")
            return
        }

        // 確保 Surface 已 ready，避免 IllegalArgumentException
        val surfaceValid = preview.holder.surface?.isValid ?: false
        if (!surfaceValid) {
            vm.onStreamError("預覽視圖尚未就緒，請稍後再試")
            return
        }

        if (!camera.isStreaming) {
            val audioOk = camera.prepareAudio()
            val videoOk = camera.prepareVideo()

            if (audioOk && videoOk) {
                camera.startStream(url)
            } else {
                vm.onStreamError("Encoder 初始化失敗")
            }
        }
    }

    private fun stopStreamAndCamera(vm: StreamViewModel) {
        rtmpCamera2?.let { camera ->
            // 先停推流
            if (camera.isStreaming) {
                camera.stopStream()
            }

            // 再停預覽（關掉相機）
            if (camera.isOnPreview) {
                camera.stopPreview()
                vm.onPreviewStopped()
            }
        }

        vm.onStreamStopped()
    }

    private fun toggleBeautyFilter() {
        val camera = rtmpCamera2 ?: return

        isBeautyOn = !isBeautyOn

        if (isBeautyOn) {
            // 套用 BeautyFilterRender
            camera.glInterface.setFilter(BeautyFilterRender())
        } else {
            // 清除所有濾鏡
            camera.glInterface.clearFilters()
        }
    }

    private fun toggleCartoonFilter() {
        val camera = rtmpCamera2 ?: return

        isCartoonOn = !isCartoonOn

        if (isCartoonOn) {
            // 套用 CartoonFilterRender
            camera.glInterface.setFilter(CartoonFilterRender())
        } else {
            // 清除所有濾鏡
            camera.glInterface.clearFilters()
        }
    }

    private fun toggleBlurFilter() {
        val camera = rtmpCamera2 ?: return

        isBlurOn = !isBlurOn

        if (isBlurOn) {
            // 套用 BlurFilterRender
            camera.glInterface.setFilter(BlurFilterRender())
        } else {
            // 清除所有濾鏡
            camera.glInterface.clearFilters()
        }
    }

    private fun switchCamera() {
        val camera = rtmpCamera2 ?: return

        try {
            camera.switchCamera()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}