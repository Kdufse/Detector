package com.kdufse.detector.model

import androidx.annotation.DrawableRes

data class DetectionItem(
    val id: String,
    val title: String,
    val description: String,
    val status: DetectionStatus,
    val details: String = "",
    @DrawableRes val iconRes: Int
)

enum class DetectionStatus {
    SAFE,
    DANGER,
    WARNING,
    UNKNOWN
}