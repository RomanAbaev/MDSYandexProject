package com.sample.mdsyandexproject.stockitem.pager_screens

class ChartBtnListener(val chartBtnListener: (period: Int) -> Unit) {
    fun onClick(period: Int) = chartBtnListener(period)
}