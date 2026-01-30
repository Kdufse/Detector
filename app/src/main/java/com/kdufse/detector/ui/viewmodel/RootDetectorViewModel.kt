package com.kdufse.detector.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kdufse.detector.detector.RootDetector
import com.kdufse.detector.model.DetectionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RootDetectorViewModel(private val rootDetector: RootDetector) : ViewModel() {
    
    private val _detectionResult = MutableStateFlow<DetectionResult?>(null)
    val detectionResult: StateFlow<DetectionResult?> = _detectionResult.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun performDetection() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = rootDetector.performDetection()
                _detectionResult.value = result
            } catch (e: Exception) {
                _error.value = "检测失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearResults() {
        _detectionResult.value = null
        _error.value = null
    }
}