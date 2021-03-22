package com.sample.mdsyandexproject.stockitem.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.databinding.FragmentSummaryBinding

class SummaryFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentSummaryBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_summary,
                container,
                false
            )

        return binding.root
    }
}