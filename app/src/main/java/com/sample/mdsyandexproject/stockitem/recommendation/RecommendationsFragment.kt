package com.sample.mdsyandexproject.stockitem.recommendation

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
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

        stockItemViewModel.getRecommendations(stockItemViewModel.stockItem.ticker)
        stockItemViewModel.recommendation.observe(viewLifecycleOwner, {
            it?.let {
                val values = mutableListOf<BarEntry>()
                for ((index, item) in it.withIndex()) {
                    val data = floatArrayOf(
                        item.strongSell.toFloat(),
                        item.sell.toFloat(),
                        item.hold.toFloat(),
                        item.buy.toFloat(),
                        item.strongBuy.toFloat()
                    )
                    values.add(
                        BarEntry(
                            index.toFloat(),
                            data
                        )
                    )
                }
                val barDataSet = BarDataSet(values, "Recommend")
                barDataSet.colors =
                    listOf(
                        Color.rgb(129, 49, 49),
                        Color.rgb(244, 91, 91),
                        Color.rgb(185, 139, 29),
                        Color.rgb(29, 185, 84),
                        Color.rgb(23, 111, 55)
                    )
                barDataSet.stackLabels =
                    arrayOf(
                        "Strong Buy",
                        "Buy",
                        "Hold",
                        "Sell",
                        "Strong sell",
                    )
                val dataSet = listOf<IBarDataSet>(barDataSet)
                val data = BarData(dataSet)

                binding.recommendationChart.data = data
                binding.recommendationChart.notifyDataSetChanged()
                binding.recommendationChart.invalidate()
            }
        })

        return binding.root
    }
}