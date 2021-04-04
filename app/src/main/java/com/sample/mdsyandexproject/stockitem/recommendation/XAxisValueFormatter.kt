package com.sample.mdsyandexproject.stockitem.recommendation

import com.github.mikephil.charting.formatter.ValueFormatter

class XAxisValueFormatter(private val periods: List<String>) : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        val p = value.toInt()
        return if (p >= periods.size) "No data"
        else periods[p]
    }
}