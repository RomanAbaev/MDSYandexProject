package com.sample.mdsyandexproject

import com.sample.mdsyandexproject.domain.StockItem
import com.sample.mdsyandexproject.utils.EST
import com.sample.mdsyandexproject.utils.isCurrentPriceValid
import com.sample.mdsyandexproject.utils.isEodValid
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
    fun isEodValidTest() {
        val monday = DateTime.now().withDayOfWeek(1).withZone(EST)
        val tuesday = DateTime.now().withDayOfWeek(2).withZone(EST)
        val wednesday = DateTime.now().withDayOfWeek(3).withZone(EST)
        val thursday = DateTime.now().withDayOfWeek(4).withZone(EST)
        val friday = DateTime.now().withDayOfWeek(5).withZone(EST)
        val saturday = DateTime.now().withDayOfWeek(6).withZone(EST)
        val sunday = DateTime.now().withDayOfWeek(7).withZone(EST)

        val monday_monday = isEodValid(
            currentDate = monday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                eodDate = monday.millis,
                eod = 1.1f
            )
        )
        val thuesday_monday = isEodValid(
            currentDate = tuesday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                eodDate = monday.millis,
                eod = 1.1f
            )
        )
        val wendsday_monday = isEodValid(
            currentDate = wednesday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                eodDate = monday.millis,
                eod = 1.1f
            )
        )
        val friday_monday = isEodValid(
            currentDate = friday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                eodDate = monday.millis,
                eod = 1.1f
            )
        )
        val monday_previousWeekFriday = isEodValid(
            currentDate = monday.toLocalDate(),
            // take a friday from previous week
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                eodDate = friday.withWeekOfWeekyear(friday.weekOfWeekyear().get() - 1).millis,
                eod = 1.1f
            )

        )
        val monday_previousWeekThursday = isEodValid(
            currentDate = monday.toLocalDate(),
            // take a thursday from previous week
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                eodDate = thursday.withWeekOfWeekyear(thursday.weekOfWeekyear().get() - 1).millis,
                eod = 1.1f
            )
        )
        val sunday_friday = isEodValid(
            currentDate = sunday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                eodDate = friday.millis,
                eod = 1.1f
            )
        )
        val sunday_thursday = isEodValid(
            currentDate = sunday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                eodDate = thursday.millis,
                eod = 1.1f
            )
        )
        val sunday_monday = isEodValid(
            currentDate = sunday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                eodDate = monday.millis,
                eod = 1.1f
            )
        )
        val saturday_friday = isEodValid(
            currentDate = saturday.toLocalDate(),
            StockItem(
                ticker = "ticker",
                companyName = "testCompanyName",
                eodDate = friday.millis,
                eod = 1.1f
            )
        )

        val friday_null = isEodValid(
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
}