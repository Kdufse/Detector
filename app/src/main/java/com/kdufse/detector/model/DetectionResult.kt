package com.kdufse.detector.model

data class DetectionResult(
    val timestamp: Long = System.currentTimeMillis(),
    val items: List<DetectionItem> = emptyList(),
    val overallStatus: RootStatus = RootStatus.UNKNOWN
) {
    val isRooted: Boolean
        get() = overallStatus == RootStatus.ROOTED
    
    val suspiciousCount: Int
        get() = items.count { it.status == DetectionStatus.DANGER || it.status == DetectionStatus.WARNING }
}