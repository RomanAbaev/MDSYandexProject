package com.sample.mdsyandexproject.stockitem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.sample.mdsyandexproject.App
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.databinding.FragmentStockItemBinding
import com.sample.mdsyandexproject.stockitem.StockItemFragmentArgs.fromBundle
import com.sample.mdsyandexproject.stockitem.TabNames.*
import com.sample.mdsyandexproject.stocklist.FavBtnListener
import com.sample.mdsyandexproject.utils.ViewModelFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class StockItemFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    lateinit var stockItemViewModel: StockItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (App.applicationContext() as App)
            .appComponent
            .activityComponent()
            .stockItemComponent()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentStockItemBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_stock_item,
                container,
                false
            )

        stockItemViewModel =
            ViewModelProvider(this, viewModelFactory)[StockItemViewModel::class.java]

        arguments?.let {
            fromBundle(it).stockItem
        }?.let { stockItem ->
            binding.stockItem = stockItem
            stockItemViewModel.stockItem = stockItem
        }

        binding.leftDrawable.setOnClickListener {
            findNavController().navigate(R.id.action_stockItemFragment_to_stockListFragment)
        }

        binding.viewPager.adapter = StockPagerAdapter(this)
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, pos ->
            tab.text = when (pos) {
                CHART.ordinal -> App.applicationContext().getString(R.string.chart_tab_name)
                NEWS.ordinal -> App.applicationContext().getString(R.string.news_tab_name)
                SUMMARY.ordinal -> App.applicationContext().getString(R.string.summary_tab_name)
                RECOMMENDATIONS.ordinal -> App.applicationContext()
                    .getString(R.string.recommendations_tab_name)
                else -> throw IllegalArgumentException("Illegal tab number")
            }
        }.attach()

        binding.favBtnListener = FavBtnListener {
            stockItemViewModel.onFavouriteButtonClicked(it)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_searchFragment_to_stockListFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stockItemViewModel.resetStockItemInformationInformation()
    }
}

enum class TabNames {
    CHART,
    NEWS,
    SUMMARY,
    RECOMMENDATIONS
}