package com.sample.mdsyandexproject.stocklist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.databinding.FragmentStockListBinding
import com.sample.mdsyandexproject.domain.StockItem
import com.sample.mdsyandexproject.repository.limit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class StockListFragment : Fragment() {

    lateinit var binding: FragmentStockListBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_stock_list,
            container,
            false
        )

        // init viewmodel
        val stockListViewModel by viewModels<StockListViewModel>()
        binding.stockListViewModel = stockListViewModel

        // init recyclerview data
        val manager = LinearLayoutManager(activity)
        binding.stockList.layoutManager = manager
        val adapter = StockItemAdapter(
            favBtnListener = FavBtnListener {
                stockListViewModel.onFavouriteButtonClicked(it)
            },
            subscribeListener = SubscribePriceUpdateListener {
                stockListViewModel.subscribeToTickerPriceUpdate(it)
            },
            unsubscribeListener = UnsubscribePriceUpdateListener {
                stockListViewModel.unsubscribeToTickerPriceUpdate(it)
            },
            updateStockItemInformationListener = UpdateStockItemInformationListener {
                stockListViewModel.updateStockItemInformation(it)
            },
            stockItemListener = StockItemListener {
                navigateToStockItem(it)
            }
        )
        binding.stockList.adapter = adapter

        binding.stockList.addOnScrollListener(
            object : EndlessRecyclerViewScrollListener(manager) {
                override fun onLoadMore() {
                    if (stockListViewModel.showFavouriteList.value != true) stockListViewModel.loadNextChunks()
                }
            })

        stockListViewModel.stockItemLoading.observe(viewLifecycleOwner, { isLoading ->
            when (isLoading) {
                true -> {
                    binding.linearPb.visibility = View.VISIBLE
                }
                else -> {
                    binding.linearPb.visibility = View.GONE
                }
            }
        })

        stockListViewModel.stockList.observe(viewLifecycleOwner, {
            it?.let { stockList ->
                lifecycleScope.launch {
                    if (stockList.isEmpty() && stockListViewModel.showFavouriteList.value == false)
                        stockListViewModel.loadNextChunks()
                    adapter.submitList(stockList)
                }
            }
        })

        val navigateToSearchFragment = OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    findNavController().navigate(R.id.action_stockListFragment_to_searchFragment)
                }
            }
            false
        }

        binding.searchLayout.setOnTouchListener(navigateToSearchFragment)
        binding.searchText.setOnTouchListener(navigateToSearchFragment)


        // TODO исходя из источников ниже поменять стиль динамически не возможно,
        //  поэтому временное решение в том чтобы в ручную менять ранее заготовленные вьюшки с нужными стилями
        //  надо изучить вопрос позже глубже и убедиться в этом
        //  https://stackoverflow.com/questions/31980342/android-data-binding-style
        //  https://code.google.com/archive/p/android-developer-preview/issues/2613
        stockListViewModel.showFavouriteList.observe(viewLifecycleOwner,
            { show ->
                when {
                    show -> {
                        binding.stocksMenuTitle.visibility = View.GONE
                        binding.favouriteMenuTitleUnselected.visibility = View.GONE
                        binding.stocksMenuTitleUnselected.visibility = View.VISIBLE
                        binding.favouriteMenuTitle.visibility = View.VISIBLE
                        binding.stockList.scrollToPosition(0)
                    }
                    else -> {
                        binding.stocksMenuTitle.visibility = View.VISIBLE
                        binding.favouriteMenuTitleUnselected.visibility = View.VISIBLE
                        binding.stocksMenuTitleUnselected.visibility = View.GONE
                        binding.favouriteMenuTitle.visibility = View.GONE
                        binding.stockList.scrollToPosition(0)
                    }
                }
            }
        )

        stockListViewModel.loadNextChunksException.observe(viewLifecycleOwner,
            {
                when (it.first) {
                    true -> {
                        // show snackbar with message and try button with callback = loadNextChunks()
                        Snackbar.make(
                            binding.root,
                            it.second,
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction(getString(R.string.try_again)) {
                            stockListViewModel.onTriedAgainBtnClick()
                            stockListViewModel.loadNextChunks()
                        }.show()
                    }
                }
            }
        )

        stockListViewModel.isDataPrepopulating.observe(viewLifecycleOwner, {
            if (it) binding.prepopulateView.visibility = View.VISIBLE
            else binding.prepopulateView.visibility = View.GONE
        })

        binding.prepopulateView.setOnTouchListener { _, _ -> true }

        binding.pb.isIndeterminate = false
        binding.pb.min = 0
        binding.pb.max = limit + 2
        binding.pb.progress = 0

        stockListViewModel.stockItemLoadingProgress.observe(viewLifecycleOwner, {
            it?.let {
                binding.pb.progress += 1
                binding.progressPersentage.text = getString(R.string.loading_data_percentage, 100 * binding.pb.progress / binding.pb.max)
            }
        })

        return binding.root
    }

    private fun navigateToStockItem(stockItem: StockItem?) {
        findNavController().navigate(
            StockListFragmentDirections.actionStockListFragmentToStockItemFragment()
                .setStockItem(stockItem)
        )
    }
}