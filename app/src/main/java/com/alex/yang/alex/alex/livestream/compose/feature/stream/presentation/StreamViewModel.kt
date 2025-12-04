package com.alex.yang.alex.alex.livestream.compose.feature.stream.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Created by AlexYang on 2025/12/4.
 *
 *
 */
@HiltViewModel
class StreamViewModel @Inject constructor(

) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onUrlChanged(url: String) {
        reduce { copy(rtmpUrl = url, errorMessage = null) }
    }

    fun onPreviewStarted() {
        reduce { copy(isPreviewOn = true, errorMessage = null) }
    }

    fun onPreviewStopped() {
        reduce { copy(isPreviewOn = false) }
    }

    fun onStreamConnecting() {
        reduce { copy(isConnecting = true, errorMessage = null) }
    }

    fun onStreamStarted() {
        reduce { copy(isConnecting = false, isStreaming = true, errorMessage = null) }
    }

    fun onStreamStopped() {
        reduce { copy(isConnecting = false, isStreaming = false) }
    }

    fun onStreamError(msg: String) {
        reduce {
            copy(
                isConnecting = false,
                isStreaming = false,
                isPreviewOn = false,
                errorMessage = msg
            )
        }
    }

    private inline fun reduce(block: UiState.() -> UiState) {
        _uiState.update { current -> current.block() }
    }

    data class UiState(
        val rtmpUrl: String = "rtmp://192.168.0.140:1935/live/test",
        val isPreviewOn: Boolean = false,
        val isStreaming: Boolean = false,
        val isConnecting: Boolean = false,
        val errorMessage: String? = null,
    )
}

