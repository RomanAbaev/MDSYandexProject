package com.sample.mdsyandexproject.stockitem.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.databinding.FragmentNewsBinding
import com.sample.mdsyandexproject.stockitem.StockItemViewModel
import com.sample.mdsyandexproject.stocklist.EndlessRecyclerViewScrollListener
import kotlinx.coroutines.launch

class NewsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val stockItemViewModel by activityViewModels<StockItemViewModel>()

        val binding: FragmentNewsBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_news,
                container,
                false
            )

        val manager = LinearLayoutManager(activity)
        binding.newsList.layoutManager = manager
        val adapter = NewsItemAdapter(
            newsItemListener = NewsItemListener {
                startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(it)
                    }
                )
            }
        )
        binding.newsList.adapter = adapter

        binding.newsList.addOnScrollListener(
            object : EndlessRecyclerViewScrollListener(manager) {
                override fun onLoadMore() {
                    stockItemViewModel.loadNews()
                }
            })

        stockItemViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            when (isLoading) {
                true -> {
                    binding.linearPb.visibility = View.VISIBLE
                }
                else -> {
                    binding.linearPb.visibility = View.GONE
                }
            }
        }

        stockItemViewModel.loadNews() // init load news when
        stockItemViewModel.news.observe(viewLifecycleOwner, {
            it?.let { news ->
                lifecycleScope.launch {
                    if (news.isEmpty()) binding.rvPb.visibility = View.VISIBLE
                    else binding.rvPb.visibility = View.GONE
                    adapter.submitList(news)
                    adapter.notifyDataSetChanged()
                }
            }
        })

        stockItemViewModel.loadNewsException.observe(viewLifecycleOwner,
            {
                when (it.first) {
                    true -> {
                        Snackbar.make(
                            binding.root,
                            it.second,
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction(getString(R.string.try_again)) {
                            stockItemViewModel.onTriedAgainLoadNewsBtnClick()
                            stockItemViewModel.loadNews()
                        }.show()
                    }
                }
            }
        )


        return binding.root
    }
}