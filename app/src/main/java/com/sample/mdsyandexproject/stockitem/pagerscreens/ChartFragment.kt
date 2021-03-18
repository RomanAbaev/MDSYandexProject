package com.sample.mdsyandexproject.stockitem.pagerscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.databinding.FragmentChartBinding

class ChartFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentChartBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_chart,
                container,
                false
            )

        return binding.root
    }
}