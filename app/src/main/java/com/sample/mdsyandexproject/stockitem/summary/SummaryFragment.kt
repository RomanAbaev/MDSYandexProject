package com.sample.mdsyandexproject.stockitem.summary

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sample.mdsyandexproject.App
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.databinding.FragmentSummaryBinding
import com.sample.mdsyandexproject.stockitem.StockItemViewModel
import com.sample.mdsyandexproject.utils.ViewModelFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class SummaryFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (App.applicationContext() as App)
            .appComponent
            .activityComponent()
            .summaryComponent()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val stockItemViewModel =
            ViewModelProvider(this, viewModelFactory)[StockItemViewModel::class.java]

        val binding: FragmentSummaryBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_summary,
                container,
                false
            )

        binding.stockItem = stockItemViewModel.stockItem

        binding.weburl.setOnClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(stockItemViewModel.stockItem.weburl)
                }
            )
        }

//        It seems Finnhub give us incorrect phones number like - 14089961010.0 (from AAPL company profile request),
//        which I couldn't find in Apple company's site, so it should be researched
//        binding.phone.setOnClickListener {
//            startActivity(
//                Intent(Intent.ACTION_DIAL).apply {
//                    data = Uri.parse(stockItemViewModel.stockItem.phone)
//                }
//            )
//        }

        return binding.root
    }
}