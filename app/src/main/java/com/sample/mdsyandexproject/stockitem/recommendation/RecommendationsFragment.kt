package com.sample.mdsyandexproject.stockitem.recommendation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.databinding.FragmentRecomendationsBinding
import com.sample.mdsyandexproject.stockitem.StockItemViewModel

class RecommendationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val stockItemViewModel by activityViewModels<StockItemViewModel>()

        val binding: FragmentRecomendationsBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_recomendations,
                container,
                false
            )

        stockItemViewModel.getRecommendations()
        stockItemViewModel.recommendationData.observe(viewLifecycleOwner, { data ->
            data?.let {
                binding.recommendationChart.data = data
                binding.recommendationChart.notifyDataSetChanged()
                binding.recommendationChart.invalidate()
            }
        })

        stockItemViewModel.recommendationDataLoading.observe(viewLifecycleOwner, { isLoading ->
            when (isLoading) {
                true -> {
                    binding.recommendationChart.visibility = View.GONE
                    binding.recommendationChartPb.visibility = View.VISIBLE
                }
                else -> {
                    binding.recommendationChartPb.visibility = View.GONE
                    binding.recommendationChart.visibility = View.VISIBLE
                }
            }
        })

        stockItemViewModel.loadRecommendationsException.observe(viewLifecycleOwner,
            {
                when (it.first) {
                    true -> {
                        Snackbar.make(
                            binding.root,
                            it.second,
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction(getString(R.string.try_again)) {
                            stockItemViewModel.onTriedAgainGetRecommendationBtnClick()
                            stockItemViewModel.getRecommendations()
                        }.show()
                    }
                }
            }
        )

        return binding.root
    }
}