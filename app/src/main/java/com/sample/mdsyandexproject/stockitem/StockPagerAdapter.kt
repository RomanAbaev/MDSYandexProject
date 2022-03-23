package com.sample.mdsyandexproject.stockitem

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sample.mdsyandexproject.stockitem.TabNames.*
import com.sample.mdsyandexproject.stockitem.news.NewsFragment
import com.sample.mdsyandexproject.stockitem.pager_screens.ChartFragment
import com.sample.mdsyandexproject.stockitem.recommendation.RecommendationsFragment
import com.sample.mdsyandexproject.stockitem.summary.SummaryFragment
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi

@DelicateCoroutinesApi
@ExperimentalCoroutinesApi
class StockPagerAdapter(fragment: StockItemFragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            CHART.ordinal -> ChartFragment()
            NEWS.ordinal -> NewsFragment()
            SUMMARY.ordinal -> SummaryFragment()
            RECOMMENDATIONS.ordinal -> RecommendationsFragment()
            else -> throw IllegalArgumentException("Illegal tab number")
        }
    }
}