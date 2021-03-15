package com.sample.mdsyandexproject

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.sample.mdsyandexproject.domain.StockItem
import com.sample.mdsyandexproject.utils.isCurrentPriceValid
import com.sample.mdsyandexproject.utils.isEodValid
import java.text.DecimalFormat
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.sign

@BindingAdapter("isFavourite")
fun ImageView.isFavourite(item: StockItem?) {
    item?.let {
        when {
            item.isFavourite -> {
                setBackgroundResource(R.drawable.ic_favour)
            }
            else -> {
                setBackgroundResource(R.drawable.ic_favour_not)
            }
        }
    }
}

@BindingAdapter("imageUrl")
fun ImageView.bindImage(imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(this.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image)
                    .transforms(RoundedCorners(24))
            )
            .into(this)
    }
}

@BindingAdapter("showMessage")
fun ImageView.showMessage(stockItem: StockItem) {
    this.setOnClickListener {
        Snackbar.make(
            this,
            resources.getString(
                R.string.error_message,
                stockItem.error ?: "-",
                stockItem.errorMessage ?: "-"
            ),
            Snackbar.LENGTH_LONG
        ).show()
    }
}

@BindingAdapter("errorPrice")
fun ImageView.errorPrice(stockItem: StockItem) {
    when {
        stockItem.error != null || stockItem.errorMessage != null -> {
            this.setImageResource(R.drawable.ic_error)
            this.visibility = View.VISIBLE
        }
        else -> {
            this.visibility = View.GONE
        }
    }
}

@BindingAdapter("loadingPrice")
fun ImageView.loadingPrice(stockItem: StockItem) {
    when {
        stockItem.error != null || stockItem.errorMessage != null -> {
            this.visibility = View.GONE
        }
        !isCurrentPriceValid(stockItem)
                || !isEodValid(stockItem = stockItem) -> {
            this.setImageResource(R.drawable.loading_animation)
            this.visibility = View.VISIBLE
        }
        else -> {
            this.visibility = View.GONE
        }
    }
}

@BindingAdapter("currentPrice")
fun TextView.currentPrice(stockItem: StockItem) {
    // check if price is outdated
    val isCurValid = isCurrentPriceValid(stockItem)
    val isEodValid = isEodValid(stockItem = stockItem)
    when {
        stockItem.error != null || stockItem.errorMessage != null -> {
            this.visibility = View.GONE
        }
        !isCurValid
                || !isEodValid -> {
            this.visibility = View.GONE
        }
        else -> {
            val price = if (isCurValid) stockItem.currentPrice else stockItem.eod
            val decimalFormat = DecimalFormat("#,###.##")
            decimalFormat.isDecimalSeparatorAlwaysShown = false
            val c: String = try {
                if (stockItem.currency != null) Currency.getInstance(stockItem.currency).symbol else ""
            } catch (ex: Exception) {
                Log.e("BindingAdapter", "currency (${stockItem.currency}) of ticker: ${stockItem.ticker} couldn't be defined")
                ex.toString()
                stockItem.currency ?: ""
            }
            this.visibility = View.VISIBLE
            text = resources.getString(
                R.string.current_price,
                decimalFormat.format(price),
                c
            )
        }
    }
}

@BindingAdapter("dayDeltaPrice")
fun TextView.dayDeltaPrice(stockItem: StockItem) {
    val isCurValid = isCurrentPriceValid(stockItem)
    val isEodValid = isEodValid(stockItem = stockItem)
    when {
        !isCurValid
                || !isEodValid -> {
            this.visibility = View.GONE
        }
        else -> {
            val decimalFormat = DecimalFormat("#,###.##")
            if (stockItem.eod != null && stockItem.previousEod != null) {
                stockItem.currentPrice?.let {
                    val dayDelta =
                        if (isCurValid) stockItem.currentPrice.minus(stockItem.eod)
                        else stockItem.eod - stockItem.previousEod
                    val percent =
                        if (!isEodValid) dayDelta * 100 / stockItem.eod
                        else dayDelta * 100 / stockItem.previousEod
                    val sign = when (dayDelta.sign) {
                        1.0f -> {
                            setTextColor(resources.getColor(R.color.positive_day_delta))
                            "+"
                        }
                        -1.0f -> {
                            setTextColor(resources.getColor(R.color.negative_day_delta))
                            "-"
                        }
                        else -> ""
                    }
                    this.visibility = View.VISIBLE
                    text = resources.getString(
                        R.string.day_delta_price,
                        sign,
                        decimalFormat.format(dayDelta.absoluteValue),
                        decimalFormat.format(percent.absoluteValue)
                    )
                }
            }
        }
    }
}


