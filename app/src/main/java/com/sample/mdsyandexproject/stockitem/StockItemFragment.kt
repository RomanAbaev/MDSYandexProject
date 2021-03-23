package com.sample.mdsyandexproject.stockitem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.databinding.FragmentStockItemBinding
import com.sample.mdsyandexproject.stockitem.StockItemFragmentArgs.fromBundle
import com.sample.mdsyandexproject.stocklist.FavBtnListener


class StockItemFragment : Fragment() {

    private val stockItemViewModel by activityViewModels<StockItemViewModel>()

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

        val arguments = arguments?.let { fromBundle(it) }
        arguments?.let {
            binding.stockItem = it.stockItem
            stockItemViewModel.stockItem = requireNotNull(it.stockItem)
        }

        binding.leftDrawable.setOnClickListener {
            findNavController().navigate(R.id.action_stockItemFragment_to_stockListFragment)
        }

        binding.viewPager.adapter = StockPagerAdapter(this)
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, pos ->
            tab.text = when (pos) {
                0 -> "Chart"
                1 -> "News"
                2 -> "Summary"
                3 -> "Recommendations"
                else -> throw IllegalArgumentException("Illegal tab number")
            }
        }.attach()

        binding.favBtnListener = FavBtnListener {
            stockItemViewModel.onFavouriteButtonClicked(it)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stockItemViewModel.resetNewsInformation()
    }
}