package com.sample.mdsyandexproject.stockitem.recommendation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.sample.mdsyandexproject.App
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.databinding.FragmentRecomendationsBinding
import com.sample.mdsyandexproject.stockitem.StockItemViewModel
import com.sample.mdsyandexproject.utils.ViewModelFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class RecommendationsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (App.applicationContext() as App)
            .appComponent
            .activityComponent()
            .recommendationComponent()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val stockItemViewModel =
            ViewModelProvider(this, viewModelFactory)[StockItemViewModel::class.java]

        val binding: FragmentRecomendationsBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_recomendations,
                container,
                false
            )

        binding.viewModel = stockItemViewModel

        stockItemViewModel.recommendations.observe(viewLifecycleOwner) { data ->
            data?.let {
                binding.recommendationChart.xAxis.apply {
                    this.position = data.second.position
                    this.setDrawGridLines(true)
                    this.granularity = data.second.granularity
                    this.labelCount = data.second.labelCount
                    this.valueFormatter = data.second.valueFormatter
                    this.typeface =
                        ResourcesCompat.getFont(App.applicationContext(), R.font.montserrat_regular)
                }
                binding.recommendationChart.legend
                binding.recommendationChart.data = data.first
                binding.recommendationChart.setPinchZoom(false)
                binding.recommendationChart.setDrawValueAboveBar(false)
                binding.recommendationChart.isHighlightFullBarEnabled = false
                binding.recommendationChart.notifyDataSetChanged()
                binding.recommendationChart.invalidate()
            }
        }

        stockItemViewModel.updateRecommendations()
        stockItemViewModel.getRecommendationCount()
        stockItemViewModel.recommendationDataLoading.observe(viewLifecycleOwner) { isLoading ->
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
        }

        stockItemViewModel.recommendationOffset.observe(viewLifecycleOwner) { offset ->
            if (offset == 0) {
                binding.next.isEnabled = false
                binding.next.setImageResource(R.drawable.ic_next_disabled)
            } else {
                binding.next.isEnabled = true
                binding.next.setImageResource(R.drawable.ic_next)
                if (stockItemViewModel.recommendationCount != -1
                    &&
                    offset + stockItemViewModel.recommendationLimit >= stockItemViewModel.recommendationCount
                ) {
                    binding.previous.isEnabled = false
                    binding.previous.setImageResource(R.drawable.ic_previous_disabled)
                } else {
                    binding.previous.isEnabled = true
                    binding.previous.setImageResource(R.drawable.ic_previous)
                }
            }
        }

        stockItemViewModel.loadRecommendationsException.observe(viewLifecycleOwner) {
            when (it.first) {
                true -> {
                    Snackbar.make(
                        binding.root,
                        it.second,
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(getString(R.string.try_again)) {
                        stockItemViewModel.onTriedAgainGetRecommendationBtnClick()
                        stockItemViewModel.updateRecommendations()
                    }.show()
                }
                false -> { /* to do nothing */
                }
            }
        }

        return binding.root
    }
}