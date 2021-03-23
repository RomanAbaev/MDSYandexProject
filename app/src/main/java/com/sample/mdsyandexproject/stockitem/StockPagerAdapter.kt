package com.sample.mdsyandexproject.stockitem

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sample.mdsyandexproject.stockitem.pager_screens.ChartFragment
import com.sample.mdsyandexproject.stockitem.recommendation.RecommendationsFragment
import com.sample.mdsyandexproject.stockitem.news.NewsFragment
import com.sample.mdsyandexproject.stockitem.summary.SummaryFragment

class StockPagerAdapter(fragment: StockItemFragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ChartFragment()
            1 -> NewsFragment()
            2 -> SummaryFragment()
            3 -> RecommendationsFragment()
            else -> throw IllegalArgumentException("Illegal tab number")
        }
    }
}