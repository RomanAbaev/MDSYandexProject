package com.sample.mdsyandexproject.stockitem.pager_screens

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.google.android.material.snackbar.Snackbar
import com.sample.mdsyandexproject.App
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.databinding.FragmentChartBinding
import com.sample.mdsyandexproject.stockitem.StockItemViewModel
import com.sample.mdsyandexproject.stockitem.chart.CandleChartDataPeriods
import com.sample.mdsyandexproject.utils.ViewModelFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class ChartFragment : Fragment() {

    var checkedPeriod: Int = -1

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (App.applicationContext() as App)
            .appComponent
            .activityComponent()
            .chartComponent()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val stockItemViewModel =
            ViewModelProvider(this, viewModelFactory)[StockItemViewModel::class.java]

        val binding: FragmentChartBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_chart,
                container,
                false
            )

        binding.stockItem = stockItemViewModel.stockItem
        binding.chartBtnListener = ChartBtnListener {
            stockItemViewModel.onCandlePeriodClick(it)
        }

        stockItemViewModel.checkedPeriod.observe(viewLifecycleOwner) { period ->
            if (checkedPeriod != -1) {
                val v = binding.chartButtonGroup.getChildAt(checkedPeriod) as TextView
                v.setBackgroundResource(R.drawable.shape_chart_btn_unselected)
                v.setTextColor(resources.getColor(R.color.black))
            }
            checkedPeriod = period
            when (period) {
                CandleChartDataPeriods.ALL.ordinal -> selectChartBtn(binding.all)
                CandleChartDataPeriods.DAY.ordinal -> selectChartBtn(binding.day)
                CandleChartDataPeriods.WEEK.ordinal -> selectChartBtn(binding.week)
                CandleChartDataPeriods.MONTH.ordinal -> selectChartBtn(binding.month)
                CandleChartDataPeriods.SIX_MONTH.ordinal -> selectChartBtn(binding.sixMonth)
                CandleChartDataPeriods.ONE_YEAR.ordinal -> selectChartBtn(binding.year)
            }
        }

        stockItemViewModel.chartLoading.observe(viewLifecycleOwner) { isLoading ->
            when (isLoading) {
                true -> {
                    binding.chart.visibility = View.GONE
                    binding.chartPb.visibility = View.VISIBLE
                }
                else -> {
                    binding.chartPb.visibility = View.GONE
                    binding.chart.visibility = View.VISIBLE
                }
            }
        }

        stockItemViewModel.onCandlePeriodClick(CandleChartDataPeriods.ALL.ordinal)

        stockItemViewModel.candlesData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val cds = CandleDataSet(it, "")
                cds.color = Color.rgb(80, 80, 80)
                cds.shadowColor = Color.DKGRAY
                cds.shadowWidth = 0.7f
                cds.decreasingColor = Color.RED
                cds.decreasingPaintStyle = Paint.Style.FILL
                cds.increasingColor = Color.rgb(122, 242, 84)
                cds.increasingPaintStyle = Paint.Style.STROKE
                cds.neutralColor = Color.BLUE
                cds.valueTextColor = Color.RED
                val cd = CandleData(cds)
                binding.chart.data = cd
                binding.chart.invalidate()
            }
        }

        stockItemViewModel.loadCandleInfoException.observe(viewLifecycleOwner) {
            when (it.first) {
                true -> {
                    Snackbar.make(
                        binding.root,
                        it.second,
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(getString(R.string.try_again)) {
                        stockItemViewModel.onTriedAgainLoadCandlesBtnClick()
                        stockItemViewModel.onCandlePeriodClick(checkedPeriod)
                    }.show()
                }
                false -> { /* to do nothing */
                }
            }
        }

        return binding.root
    }

    private fun selectChartBtn(v: TextView) {
        v.setBackgroundResource(R.drawable.shape_chart_btn_selected)
        v.setTextColor(resources.getColor(R.color.white))
    }
}

