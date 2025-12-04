package com.alex.yang.alex.alex.livestream.compose.feature.stream.presentation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.alex.yang.alex.alex.livestream.compose.R
import com.alex.yang.alex.alex.livestream.compose.ui.theme.AlexLiveStreamComposeTheme
import com.pedro.encoder.utils.gl.AspectRatioMode
import com.pedro.library.view.OpenGlView


/**
 * Created by AlexYang on 2025/12/4.
 *
 *
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamScreen(
    modifier: Modifier = Modifier,
    uiState: StreamViewModel.UiState,
    onUrlChanged: (String) -> Unit = {},
    onAttachOpenGlView: (OpenGlView) -> Unit = {},
    onStartStreamClick: () -> Unit = {},
    onStopStreamClick: () -> Unit = {},
    onBeautyClick: () -> Unit = {},
    onCartoonClick: () -> Unit = {},
    onBlurClick: () -> Unit = {},
    onSwitchCameraClick: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                OpenGlView(context).apply {
                    // 設定滿版畫面
                    setAspectRatioMode(AspectRatioMode.Fill)
                }.also(onAttachOpenGlView)
            }
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, top = 48.dp)
                .align(Alignment.TopEnd)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = onSwitchCameraClick
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(R.drawable.ic_camera_switch),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        // ───── 浮在底部的控制列 ─────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.rtmpUrl,
                onValueChange = onUrlChanged,
                label = { Text("RTMP URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onStartStreamClick,
                    enabled = !uiState.isStreaming && !uiState.isConnecting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (uiState.isConnecting) "連線中..." else "START"
                    )
                }

                OutlinedButton(
                    onClick = onStopStreamClick,
                    enabled = uiState.isStreaming,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("STOP")
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 美顏按鈕
                OutlinedButton(
                    onClick = onBeautyClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("BEAUTY")
                }

                // 卡通按鈕
                OutlinedButton(
                    onClick = onCartoonClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("CARTOON")
                }

                // Blur 按鈕
                OutlinedButton(
                    onClick = onBlurClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("BLUR")
                }
            }

            Text(
                text = buildString {
                    append("Preview: ")
                    append(if (uiState.isPreviewOn) "ON" else "OFF")
                    append("   |   Stream: ")
                    append(if (uiState.isStreaming) "ON" else "OFF")
                },
                style = MaterialTheme.typography.bodySmall
            )
        }

        // ───── 浮在上方的錯誤訊息 ─────
        uiState.errorMessage?.let { msg ->
            Text(
                text = msg,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode"
)
@Preview(
    showBackground = true,
    showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun StreamScreenPreview() {
    AlexLiveStreamComposeTheme {
        StreamScreen(
            uiState = StreamViewModel.UiState(
                rtmpUrl = "rtmp://your.streaming.server/app/streamkey",
                isPreviewOn = true,
                isStreaming = false,
                isConnecting = false,
                errorMessage = null,
            )
        )
    }
}