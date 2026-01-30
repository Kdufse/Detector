package com.kdufse.detector.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kdufse.detector.databinding.ItemDetectionResultBinding
import com.kdufse.detector.model.DetectionItem
import com.kdufse.detector.model.DetectionStatus

class ResultAdapter : ListAdapter<DetectionItem, ResultAdapter.ViewHolder>(DiffCallback()) {
    
    class ViewHolder(private val binding: ItemDetectionResultBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: DetectionItem) {
            binding.titleText.text = item.title
            binding.descriptionText.text = item.description
            binding.detailsText.text = item.details
            
            // 根据状态设置颜色
            val colorRes = when (item.status) {
                DetectionStatus.SAFE -> android.R.color.holo_green_dark
                DetectionStatus.DANGER -> android.R.color.holo_red_dark
                DetectionStatus.WARNING -> android.R.color.holo_orange_dark
                DetectionStatus.UNKNOWN -> android.R.color.darker_gray
            }
            
            binding.statusIndicator.setBackgroundResource(colorRes)
            binding.iconView.setImageResource(item.iconRes)
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetectionResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class DiffCallback : DiffUtil.ItemCallback<DetectionItem>() {
        override fun areItemsTheSame(oldItem: DetectionItem, newItem: DetectionItem): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: DetectionItem, newItem: DetectionItem): Boolean {
            return oldItem == newItem
        }
    }
}