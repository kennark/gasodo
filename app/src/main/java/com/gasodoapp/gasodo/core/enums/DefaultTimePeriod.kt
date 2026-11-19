package com.gasodoapp.gasodo.core.enums

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gasodoapp.gasodo.R
import java.time.LocalDate

/**
 * Period the overview screen selects when it is opened, before the user picks a range of their own.
 *
 * [labelRes] is the user facing name, resolved through [label] so it follows the app language.
 */
enum class DefaultTimePeriod(@param:StringRes val labelRes: Int) {
    SEVEN_DAYS(R.string.default_time_period_seven_days),
    THIS_MONTH(R.string.default_time_period_this_month),
    THIRTY_DAYS(R.string.default_time_period_thirty_days),
    YEAR_TO_DATE(R.string.default_time_period_year_to_date),
    YEAR(R.string.default_time_period_year);

    /**
     * Localised name of this period, for rendering inside a composable.
     */
    @Composable
    fun label(): String = stringResource(labelRes)

    /**
     * Range this period covers when resolved on [today], with both bounds included.
     *
     * Every period ends on [today]. The trailing ones count backwards from it, so [SEVEN_DAYS]
     * covers today plus the six days before it rather than seven days before today, which keeps the
     * window the length its name promises.
     */
    fun dateRange(today: LocalDate = LocalDate.now()): ClosedRange<LocalDate> = when (this) {
        SEVEN_DAYS -> today.minusDays(6)..today
        THIS_MONTH -> today.withDayOfMonth(1)..today
        THIRTY_DAYS -> today.minusDays(29)..today
        YEAR_TO_DATE -> today.withDayOfYear(1)..today
        YEAR -> today.minusYears(1).plusDays(1)..today
    }

    companion object {

        /**
         * Entry used when nothing has been stored yet.
         */
        val DEFAULT: DefaultTimePeriod = YEAR_TO_DATE

        /**
         * Resolves a persisted [name] (an enum constant name), falling back to [DEFAULT] when the
         * value is absent or unknown.
         *
         * An unknown name can only happen after downgrading from a version that knew more periods,
         * or from a hand edited preferences file, so treating it as the default keeps the app
         * usable instead of crashing on `valueOf`.
         */
        fun fromName(name: String?): DefaultTimePeriod =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
