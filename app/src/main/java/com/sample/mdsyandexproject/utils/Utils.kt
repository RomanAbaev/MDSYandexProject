package com.sample.mdsyandexproject.utils

import com.sample.mdsyandexproject.domain.StockItem
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat

val EST = DateTimeZone.forID("America/New_York")

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

fun parseISO8601Date(date: String): Long {
    val parser = ISODateTimeFormat.dateTimeParser().withZone(EST)
    return parser.parseDateTime(date).millis
}