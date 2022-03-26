package com.sample.mdsyandexproject.stockitem.recommendation.di

import com.sample.mdsyandexproject.stockitem.recommendation.RecommendationsFragment
import dagger.Subcomponent

@Subcomponent
interface RecommendationComponent {
    fun inject(fragment: RecommendationsFragment)
}