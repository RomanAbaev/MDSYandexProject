package com.sample.mdsyandexproject.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.databinding.FragmentSearchBinding
import com.sample.mdsyandexproject.stocklist.FavBtnListener
import com.sample.mdsyandexproject.stocklist.StockItemAdapter
import com.sample.mdsyandexproject.stocklist.SubscribePriceUpdateListener
import com.sample.mdsyandexproject.stocklist.UnsubscribePriceUpdateListener
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_search,
            container,
            false
        )

        // init viewmodel
        val searchViewModel by viewModels<SearchViewModel>()
        binding.searchViewModel = searchViewModel

        // init recyclerview data
        val manager = LinearLayoutManager(activity)
        binding.searchResultList.layoutManager = manager
        val adapter = StockItemAdapter(
            FavBtnListener {
                searchViewModel.onFavouriteButtonClicked(it)
            },
            SubscribePriceUpdateListener {
                searchViewModel.subscribeToTickerPriceUpdate(it)
            },
            UnsubscribePriceUpdateListener {
                searchViewModel.unsubscribeToTickerPriceUpdate(it)
            }
        )
        binding.searchResultList.adapter = adapter

        binding.searchText.requestFocus()
        binding.searchText.setOnFocusChangeListener { v, hasFocus ->
            searchViewModel.toggleSearch(hasFocus)
            showSoftKeyboard(v)
        }

        binding.searchText.addTextChangedListener { text ->
            text?.let {
                when (text.length) {
                    0 -> {
                        showEmptySearch()
                        searchViewModel.cancelJob()
                        adapter.submitList(null)
                    }
                    else -> {
                        binding.rightSearchDrawable.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.searchText.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    // make request to api
                    adapter.submitList(null)
                    hideSoftKeyboard(v)
                    searchViewModel.submitSearch(v.text.toString())
                    showSearchResult()
                    true
                }
                else -> false
            }
        }

        binding.rightSearchDrawable.setOnClickListener {
            clearSearchText()
            showEmptySearch()
        }

        binding.leftDrawable.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_stockListFragment)
        }

        searchViewModel.searchResultList.observe(viewLifecycleOwner, {
            it?.let {
                binding.rvPb.visibility = View.GONE
                adapter.submitList(it)
            }
        })

        searchViewModel.popularRequestList.observe(viewLifecycleOwner, object :
            Observer<List<String>> {
            override fun onChanged(data: List<String>?) {
                data ?: return
                val chipGroup = binding.searchSuggestView.popularRequestList
                chipGroup.populateChipGroup(data);
            }
        })

        searchViewModel.searchHistoryList.observe(viewLifecycleOwner,
            object :
                Observer<List<String>> {
                override fun onChanged(data: List<String>?) {
                    data ?: return
                    val chipGroup = binding.searchSuggestView.searchHistoryList
                    chipGroup.populateChipGroup(data)
                }
            })

        searchViewModel.isSearchActive.observe(viewLifecycleOwner, { isActive ->
            when {
                isActive -> {
                    binding.leftDrawable.setImageResource(R.drawable.ic_back_arrow)
                    binding.searchResultList.visibility = View.GONE
                }
                else -> {
                    binding.searchText.clearFocus()
                    binding.leftDrawable.setImageResource(R.drawable.ic_search_right)
                    binding.searchResultList.visibility = View.VISIBLE
                }
            }
        })


        return binding.root
    }

    private fun showSearchResult() {
        binding.rightSearchDrawable.visibility = View.VISIBLE
        binding.searchResultContainer.visibility = View.VISIBLE
        binding.searchResultList.visibility = View.VISIBLE
        binding.searchSuggestView.root.visibility = View.GONE
        binding.rvPb.visibility = View.VISIBLE
    }

    private fun showEmptySearch() {
        binding.rightSearchDrawable.visibility = View.GONE
        binding.searchResultContainer.visibility = View.GONE
        binding.rvPb.visibility = View.GONE
        binding.searchSuggestView.root.visibility = View.VISIBLE
    }

    private fun clearSearchText() {
        binding.searchText.setText("")
    }

    private fun ChipGroup.populateChipGroup(data: List<String>) {
        val inflater = LayoutInflater.from(context)
        val children = data.map { regionName ->
            val chip = inflater.inflate(R.layout.chip_item, this, false) as Chip
            chip.text = regionName
            chip.tag = regionName
            chip.setOnClickListener {
                binding.searchText.setText(chip.text);
                binding.searchText.setSelection(chip.text.length)
                showSoftKeyboard(binding.searchText)
            }
            chip
        }
        this.removeAllViews()
        for (chip in children) {
            this.addView(chip)
        }
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm =
                requireNotNull(context).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideSoftKeyboard(view: View) {
        val imm =
            requireNotNull(context).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }
}