package com.sample.mdsyandexproject.stocklist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sample.mdsyandexproject.databinding.StockListItemDarkBinding
import com.sample.mdsyandexproject.databinding.StockListItemLightBinding
import com.sample.mdsyandexproject.domain.StockItem
import com.sample.mdsyandexproject.utils.isCurrentPriceValid
import com.sample.mdsyandexproject.utils.isPreviousClosePriceValid


class StockItemAdapter(
    private val favBtnListener: FavBtnListener,
    private val subscribeListener: SubscribePriceUpdateListener,
    private val unsubscribeListener: UnsubscribePriceUpdateListener,
    private val updateStockItemInformationListener: UpdateStockItemInformationListener? = null
) :
    ListAdapter<StockItem, RecyclerView.ViewHolder>(StockListDiffCallback()) {

    var subscribedTickers = mutableSetOf<String>()

    private val DARK_ITEM_VIEW_TYPE = 0
    private val LIGHT_ITEM_VIEW_TYPE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            DARK_ITEM_VIEW_TYPE -> DarkViewHolder.from(parent)
            LIGHT_ITEM_VIEW_TYPE -> LightViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: StockItem? = getItem(position)
        when (holder) {
            is DarkViewHolder -> {
                holder.bind(favBtnListener, item)
            }
            is LightViewHolder -> {
                holder.bind(favBtnListener, item)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is DarkViewHolder -> {
                holder.stockItem?.let {
                    if (it.ticker !in subscribedTickers) {
                        subscribeListener.subscribe(it)
                        subscribedTickers.add(it.ticker)
                        if (!isCurrentPriceValid(it)
                            || !isPreviousClosePriceValid(stockItem = it) && it.error != "403"
                        ) updateStockItemInformationListener?.updateStockItemInformation(it)
                    }
                }

            }
            is LightViewHolder -> {
                holder.stockItem?.let {
                    if (it.ticker !in subscribedTickers) {
                        subscribeListener.subscribe(it)
                        subscribedTickers.add(it.ticker)
                        if (!isCurrentPriceValid(it)
                            || !isPreviousClosePriceValid(stockItem = it) && it.error != "403"
                        ) updateStockItemInformationListener?.updateStockItemInformation(it)
                    }
                }
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is DarkViewHolder -> {
                holder.stockItem?.let {
                    if (it.ticker in subscribedTickers) {
                        unsubscribeListener.unsubscribe(it)
                        subscribedTickers.remove(it.ticker)
                    }
                }
            }
            is LightViewHolder -> {
                holder.stockItem?.let {
                    if (it.ticker in subscribedTickers) {
                        unsubscribeListener.unsubscribe(it)
                        subscribedTickers.remove(it.ticker)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position % 2) {
            0 -> DARK_ITEM_VIEW_TYPE
            else -> LIGHT_ITEM_VIEW_TYPE
        }
    }

    class LightViewHolder private constructor(private val binding: StockListItemLightBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var stockItem: StockItem? = null

        fun bind(favBtnListener: FavBtnListener, stockItem: StockItem?) {
            this.stockItem = stockItem
            binding.stockItem = stockItem
            binding.favBtnListener = favBtnListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): LightViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = StockListItemLightBinding.inflate(layoutInflater, parent, false)

                return LightViewHolder(binding)
            }
        }
    }

    class DarkViewHolder private constructor(private val binding: StockListItemDarkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var stockItem: StockItem? = null

        fun bind(favBtnListener: FavBtnListener, stockItem: StockItem?) {
            this.stockItem = stockItem
            binding.stockItem = stockItem
            binding.favBtnListener = favBtnListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): DarkViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = StockListItemDarkBinding.inflate(layoutInflater, parent, false)

                return DarkViewHolder(binding)
            }
        }
    }
}


class StockListDiffCallback : DiffUtil.ItemCallback<StockItem>() {
    override fun areItemsTheSame(oldItem: StockItem, newItem: StockItem): Boolean {
        return oldItem.ticker == newItem.ticker
    }

    override fun areContentsTheSame(oldItem: StockItem, newItem: StockItem): Boolean {
        return oldItem == newItem
    }
}

class FavBtnListener(val favBtnListener: (stockItem: StockItem) -> Unit) {
    fun onClick(stockItem: StockItem) = favBtnListener(stockItem)
}

class SubscribePriceUpdateListener(
    val subscribeListener: (ticker: String) -> Unit,
) {
    fun subscribe(stockItem: StockItem) {
        subscribeListener(stockItem.ticker)
    }
}

class UnsubscribePriceUpdateListener(
    val unsubscribeListener: (ticker: String) -> Unit
) {
    fun unsubscribe(stockItem: StockItem) {
        unsubscribeListener(stockItem.ticker)
    }
}

class UpdateStockItemInformationListener(
    val updateStockItemInformationListener: (stockItem: StockItem) -> Unit
) {
    fun updateStockItemInformation(stockItem: StockItem) {
        updateStockItemInformationListener(stockItem)
    }
}