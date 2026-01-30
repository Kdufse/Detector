package com.kdufse.detector.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kdufse.detector.databinding.FragmentMainBinding
import com.kdufse.detector.ui.viewmodel.RootDetectorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment() {
    
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: RootDetectorViewModel by viewModels()
    private lateinit var adapter: ResultAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupObservers()
        
        // 自动开始检测
        viewModel.performDetection()
    }
    
    private fun setupUI() {
        adapter = ResultAdapter()
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MainFragment.adapter
        }
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.performDetection()
        }
        
        binding.fab.setOnClickListener {
            viewModel.performDetection()
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.swipeRefreshLayout.isRefreshing = isLoading
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.detectionResult.collect { result ->
                result?.let {
                    adapter.submitList(it.items)
                    updateOverallStatus(it)
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    binding.errorText.text = it
                    binding.errorText.visibility = View.VISIBLE
                } ?: run {
                    binding.errorText.visibility = View.GONE
                }
            }
        }
    }
    
    private fun updateOverallStatus(result: DetectionResult) {
        val statusText = when (result.overallStatus) {
            com.kdufse.detector.model.RootStatus.NOT_ROOTED -> "设备未Root"
            com.kdufse.detector.model.RootStatus.ROOTED -> "设备已Root"
            com.kdufse.detector.model.RootStatus.SUSPICIOUS -> "设备可疑"
            com.kdufse.detector.model.RootStatus.UNKNOWN -> "状态未知"
        }
        
        val colorRes = when (result.overallStatus) {
            com.kdufse.detector.model.RootStatus.NOT_ROOTED -> android.R.color.holo_green_dark
            com.kdufse.detector.model.RootStatus.ROOTED -> android.R.color.holo_red_dark
            com.kdufse.detector.model.RootStatus.SUSPICIOUS -> android.R.color.holo_orange_dark
            com.kdufse.detector.model.RootStatus.UNKNOWN -> android.R.color.darker_gray
        }
        
        binding.statusText.text = statusText
        binding.statusText.setTextColor(resources.getColor(colorRes, null))
        binding.detectionCount.text = "检测到 ${result.suspiciousCount} 个可疑项目"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}