package com.sample.mdsyandexproject.stockitem.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.databinding.StockNewsListItemBinding
import com.sample.mdsyandexproject.domain.NewsItem

class NewsItemAdapter(
    private val newsItemListener: NewsItemListener?) : ListAdapter<NewsItem, RecyclerView.ViewHolder>(NewsListDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return NewsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NewsViewHolder -> {
                holder.bind(getItem(position), newsItemListener)
            }
        }
    }

    class NewsViewHolder private constructor(private val binding: StockNewsListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var newsItem: NewsItem

        fun bind(newsItem: NewsItem, newsItemListener: NewsItemListener?) {
            this.newsItem = newsItem
            binding.newsItem = newsItem
            binding.newsItemListener = newsItemListener
            if (adapterPosition % 2 == 0) binding.root.setBackgroundResource(R.drawable.rv_item_shape_dark)
            else binding.root.setBackgroundResource(R.drawable.rv_item_shape_light)
            binding.root.setOnClickListener {
                newsItemListener?.onClick(newsItem.url)
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): NewsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = StockNewsListItemBinding.inflate(layoutInflater, parent, false)

                return NewsViewHolder(binding)
            }
        }
    }
}

class NewsItemListener(val newsItemListener: (url: String) -> Unit) {
    fun onClick(url: String) = newsItemListener(url)
}

class NewsListDiffCallback : DiffUtil.ItemCallback<NewsItem>() {
    override fun areItemsTheSame(oldItem: NewsItem, newItem: NewsItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NewsItem, newItem: NewsItem): Boolean {
        return oldItem == newItem
    }
}