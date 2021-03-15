package com.sample.mdsyandexproject.utils

import com.sample.mdsyandexproject.domain.StockItem
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import java.lang.IllegalArgumentException

val EST = DateTimeZone.forID("America/New_York")

fun isEodValid(
    currentDate: LocalDate = DateTime.now().withZone(EST).toLocalDate(),
    stockItem: StockItem
): Boolean {
    if (stockItem.eodDate == null || stockItem.eod == null) return false
    val eodLocalDate = DateTime(stockItem.eodDate).withZone(EST).toLocalDate()
    return when (currentDate.dayOfWeek) {
        1 -> {
            Days.daysBetween(eodLocalDate, currentDate).days <= 3
        }
        in 2..6 -> {
            Days.daysBetween(eodLocalDate, currentDate).days <= 1
        }
        7 -> {
            Days.daysBetween(eodLocalDate, currentDate).days <= 2
        }
        else -> throw IllegalArgumentException("Illegal dayOfWeek parameter it should be from 1 to 7")
    }
}

fun isCurrentPriceValid(stockItem: StockItem): Boolean {
    return (stockItem.currentPriceDate != null || stockItem.currentPrice != null)
            && DateTime(stockItem.currentPriceDate).withZone(EST).toLocalDate()
        .isEqual(DateTime.now().withZone(EST).toLocalDate())
}

fun isCompanyInfoValid(stockItem: StockItem): Boolean {
    return stockItem.logoUrl != null && stockItem.currency != null
}

fun parseISO8601Date(date: String): Long {
    val parser = ISODateTimeFormat.dateTimeParser().withZone(EST)
    return parser.parseDateTime(date).millis
}