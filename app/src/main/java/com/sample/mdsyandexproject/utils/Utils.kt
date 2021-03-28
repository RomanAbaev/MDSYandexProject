package com.sample.mdsyandexproject.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.sample.mdsyandexproject.App
import com.sample.mdsyandexproject.domain.StockItem
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import retrofit2.HttpException

val EST = DateTimeZone.forID("America/New_York")
const val YYYY_MM_dd = "YYYY-MM-dd"
const val MMM_YY = "MMM-YY"

fun isPreviousClosePriceValid(
    currentDate: LocalDate = DateTime.now().toLocalDate(),
    stockItem: StockItem
): Boolean {
    if (stockItem.previousClosePrice == null || stockItem.previousClosePriceDate == null) return false
    val previousClosePriceDate = DateTime(stockItem.previousClosePriceDate).toLocalDate()
    return when (currentDate.dayOfWeek) {
        1 -> {
            Days.daysBetween(previousClosePriceDate, currentDate).days <= 3
        }
        in 2..6 -> {
            Days.daysBetween(previousClosePriceDate, currentDate).days <= 1
        }
        7 -> {
            Days.daysBetween(previousClosePriceDate, currentDate).days <= 2
        }
        else -> throw IllegalArgumentException("Illegal dayOfWeek parameter it should be from 1 to 7")
    }
}

fun isCurrentPriceValid(stockItem: StockItem): Boolean {
    return (stockItem.currentPriceDate != null || stockItem.currentPrice != null)
            && DateTime(stockItem.currentPriceDate).toLocalDate()
        .isEqual(DateTime.now().toLocalDate())
}

fun isCompanyInfoValid(stockItem: StockItem): Boolean {
    return stockItem.logoUrl != null
}

fun convertDateToNewsFormat(date: Long): String {
    val time = DateTime(date).toLocalDate()
    return time.toString()
}

fun getReadableNetworkMessage(ex: HttpException): String {
    return when (ex.code()) {
        403 -> "Permission denied"
        429 -> "Your limit exceeded"
        else -> "Something goes wrong"
    }
}

fun convertLongToDate(format: String, date: Long): String {
    val formatter: DateTimeFormatter = DateTimeFormat.forPattern(format)
    return DateTime(date).toLocalDate().toString(formatter)
}

fun parseStringDate(format: String, date: String): Long {
    val formatter: DateTimeFormatter = DateTimeFormat.forPattern(format)
    val dateTime = formatter.parseDateTime(date)
    return dateTime.millis
}

fun getFromAndToDateForNews(newsPage: Int): String =
    DateTime.now().minusDays(newsPage).toLocalDate().toString()

fun getStartAndEndOfDayMillis(newsPage: Int): Pair<Long, Long> {
    val dateTime = DateTime.now().minusDays(newsPage)
    val start: Long = dateTime.withTimeAtStartOfDay().millis / 1000L
    val end: Long = dateTime.plusDays(1).withTimeAtStartOfDay().millis / 1000L
    return Pair(start, end)
}

fun isNetworkAvailable(): Boolean {
    val connectivityManager = App.applicationContext()
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val nw = connectivityManager.activeNetwork ?: return false
    val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
    return when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }
}