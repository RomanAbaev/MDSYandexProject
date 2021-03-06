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
import com.sample.mdsyandexproject.utils.isPreviousClosePriceValid
import org.joda.time.DateTime
import java.text.DecimalFormat
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.sign

@BindingAdapter("isFavourite")
fun ImageView.isFavourite(item: StockItem?) {
    item?.let {
        when {
            item.error != null || item.errorMessage != null -> this.visibility = View.GONE
            item.isFavourite -> {
                this.visibility = View.VISIBLE
                setBackgroundResource(R.drawable.ic_favour)
            }
            else -> {
                this.visibility = View.VISIBLE
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
                    .transform(RoundedCorners(24))
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
                || !isPreviousClosePriceValid(stockItem = stockItem) -> {
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
    val isPrevValid = isPreviousClosePriceValid(stockItem = stockItem)
    when {
        stockItem.error != null || stockItem.errorMessage != null -> {
            this.visibility = View.GONE
        }
        !isCurValid
                || !isPrevValid -> {
            this.visibility = View.GONE
        }
        else -> {
            val curPrice = stockItem.currentPrice
            if (curPrice != null) {
                val decimalFormat = DecimalFormat("#,###.##")
                decimalFormat.isDecimalSeparatorAlwaysShown = false
                val c: String = try {
                    if (stockItem.currency != null) Currency.getInstance(stockItem.currency).symbol else ""
                } catch (ex: Exception) {
                    Log.e(
                        "BindingAdapter",
                        "currency (${stockItem.currency}) of ticker: ${stockItem.ticker} couldn't be defined"
                    )
                    ex.toString()
                    stockItem.currency ?: ""
                }
                this.visibility = View.VISIBLE
                text = resources.getString(
                    R.string.current_price,
                    decimalFormat.format(curPrice),
                    c
                )
            }
        }
    }
}

@BindingAdapter("dayDeltaPrice")
fun TextView.dayDeltaPrice(stockItem: StockItem) {
    val isCurValid = isCurrentPriceValid(stockItem)
    val isPrevValid = isPreviousClosePriceValid(stockItem = stockItem)
    when {
        !isCurValid
                || !isPrevValid -> {
            this.visibility = View.GONE
        }
        else -> {
            val decimalFormat = DecimalFormat("#,###.##")
            val prevPrice = stockItem.previousClosePrice
            val curPrice = stockItem.currentPrice
            if (prevPrice != null && curPrice != null) {
                val dayDelta = curPrice - prevPrice
                val percent = dayDelta * 100 / prevPrice
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

@BindingAdapter("unixDate")
fun TextView.unixDate(datetime: Long) {
    val dateTime = DateTime(datetime.times(1000L))
    this.text = dateTime.toString("dd.MM.YYYY HH:mm")
}
