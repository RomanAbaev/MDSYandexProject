package com.sample.mdsyandexproject.stockitem.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sample.mdsyandexproject.App
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.databinding.FragmentNewsBinding
import com.sample.mdsyandexproject.stockitem.StockItemViewModel
import com.sample.mdsyandexproject.stocklist.EndlessRecyclerViewScrollListener
import com.sample.mdsyandexproject.utils.ViewModelFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class NewsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (App.applicationContext() as App)
            .appComponent
            .activityComponent()
            .newsComponent()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val stockItemViewModel =
            ViewModelProvider(this, viewModelFactory)[StockItemViewModel::class.java]

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
            newsItemListener = NewsItemListener { url ->
                startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(url)
                    }
                )
            },
            shareItemListener = ShareItemListener { url ->
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, url)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
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

        stockItemViewModel.loadNews()
        stockItemViewModel.news.observe(viewLifecycleOwner) {
            it?.let { news ->
                lifecycleScope.launch {
                    if (news.isEmpty()) binding.rvPb.visibility = View.VISIBLE
                    else binding.rvPb.visibility = View.GONE
                    adapter.submitList(news)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        stockItemViewModel.loadNewsException.observe(viewLifecycleOwner) {
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
                false -> { /* to do nothing */
                }
            }
        }


        return binding.root
    }
}