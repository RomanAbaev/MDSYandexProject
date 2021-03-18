package com.sample.mdsyandexproject.stockitem

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sample.mdsyandexproject.stockitem.pagerscreens.ChartFragment
import com.sample.mdsyandexproject.stockitem.pagerscreens.ForecastsFragment
import com.sample.mdsyandexproject.stockitem.pagerscreens.NewsFragment
import com.sample.mdsyandexproject.stockitem.pagerscreens.SummaryFragment

class StockPagerAdapter(fragment: StockItemFragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ChartFragment()
            1 -> NewsFragment()
            2 -> SummaryFragment()
            3 -> ForecastsFragment()
            else -> throw IllegalArgumentException("Illegal tab number")
        }
    }
}