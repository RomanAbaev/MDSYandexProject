package com.sample.mdsyandexproject.stockitem.pager_screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.databinding.FragmentForecastsBinding

class ForecastsFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentForecastsBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_forecasts,
                container,
                false
            )

        return binding.root
    }
}