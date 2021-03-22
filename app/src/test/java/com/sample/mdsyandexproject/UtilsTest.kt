package com.sample.mdsyandexproject

import com.sample.mdsyandexproject.domain.StockItem
import com.sample.mdsyandexproject.utils.EST
import com.sample.mdsyandexproject.utils.convertDateToNewsFormat
import com.sample.mdsyandexproject.utils.isCurrentPriceValid
import com.sample.mdsyandexproject.utils.isPreviousClosePriceValid
import org.hamcrest.core.Is.`is`
import org.joda.time.DateTime
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UtilsTest {

    @Test
    fun isCurrentPriceValidTest() {
        val now = DateTime.now().withZone(EST).millis
        val tomorrow = DateTime.now().plusDays(1).withZone(EST).millis
        val yesterday = DateTime.now().minusDays(1).withZone(EST).millis

        assertThat(
            isCurrentPriceValid(
                StockItem(
                    ticker = "ticker",
                    companyName = "testCompanyName",
                    currentPriceDate = now
                )
            ), `is`(true)
        );
        assertThat(
            isCurrentPriceValid(
                StockItem(
                    ticker = "ticker",
                    companyName = "testCompanyName",
                    currentPriceDate = tomorrow
                )
            ), `is`(false)
        );
        assertThat(
            isCurrentPriceValid(
                StockItem(
                    ticker = "ticker",
                    companyName = "testCompanyName",
                    currentPriceDate = yesterday
                )
            ), `is`(false)
        );
        assertThat(
            isCurrentPriceValid(
                StockItem(
                    ticker = "ticker",
                    companyName = "testCompanyName",
                    currentPriceDate = null
                )
            ), `is`(false)
        )
    }

    @Test
    fun isPreviousClosePriceValidTest() {
        val monday = DateTime.now().withDayOfWeek(1)
        val tuesday = DateTime.now().withDayOfWeek(2)
        val wednesday = DateTime.now().withDayOfWeek(3)
        val thursday = DateTime.now().withDayOfWeek(4)
        val friday = DateTime.now().withDayOfWeek(5)
        val saturday = DateTime.now().withDayOfWeek(6)
        val sunday = DateTime.now().withDayOfWeek(7)

        val monday_monday = isPreviousClosePriceValid(
            currentDate = monday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                previousClosePriceDate = monday.millis,
                previousClosePrice = 1.1f
            )
        )
        val thuesday_monday = isPreviousClosePriceValid(
            currentDate = tuesday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                previousClosePriceDate = monday.millis,
                previousClosePrice = 1.1f
            )
        )
        val wendsday_monday = isPreviousClosePriceValid(
            currentDate = wednesday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                previousClosePriceDate = monday.millis,
                previousClosePrice = 1.1f
            )
        )
        val friday_monday = isPreviousClosePriceValid(
            currentDate = friday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                previousClosePriceDate = monday.millis,
                previousClosePrice = 1.1f
            )
        )
        val monday_previousWeekFriday = isPreviousClosePriceValid(
            currentDate = monday.toLocalDate(),
            // take a friday from previous week
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                previousClosePriceDate = friday.withWeekOfWeekyear(friday.weekOfWeekyear().get() - 1).millis,
                previousClosePrice = 1.1f
            )

        )
        val monday_previousWeekThursday = isPreviousClosePriceValid(
            currentDate = monday.toLocalDate(),
            // take a thursday from previous week
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                previousClosePriceDate = thursday.withWeekOfWeekyear(thursday.weekOfWeekyear().get() - 1).millis,
                previousClosePrice = 1.1f
            )
        )
        val sunday_friday = isPreviousClosePriceValid(
            currentDate = sunday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                previousClosePriceDate = friday.millis,
                previousClosePrice = 1.1f
            )
        )
        val sunday_thursday = isPreviousClosePriceValid(
            currentDate = sunday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                previousClosePriceDate = thursday.millis,
                previousClosePrice = 1.1f
            )
        )
        val sunday_monday = isPreviousClosePriceValid(
            currentDate = sunday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                previousClosePriceDate = monday.millis,
                previousClosePrice = 1.1f
            )
        )
        val saturday_friday = isPreviousClosePriceValid(
            currentDate = saturday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                previousClosePriceDate = friday.millis,
                previousClosePrice = 1.1f
            )
        )

        val friday_null = isPreviousClosePriceValid(
            currentDate = friday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
            )
        )

        assertThat(monday_monday, `is`(true))
        assertThat(thuesday_monday, `is`(true))
        assertThat(wendsday_monday, `is`(false))
        assertThat(friday_monday, `is`(false))
        assertThat(monday_previousWeekFriday, `is`(true))
        assertThat(monday_previousWeekThursday, `is`(false))
        assertThat(sunday_friday, `is`(true))
        assertThat(sunday_thursday, `is`(false))
        assertThat(sunday_monday, `is`(false))
        assertThat(saturday_friday, `is`(true))
        assertThat(friday_null, `is`(false))
    }

    @Test
    fun convertDateToNewsFormatTest() {
        val newsDateFormat =
            convertDateToNewsFormat(DateTime("2004-04-01").millis)

        assertThat(newsDateFormat, `is`("2004-04-01"))
    }
}