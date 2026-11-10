package com.gasodoapp.gasodo.core.enums.units

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gasodoapp.gasodo.R

/**
 * Currency used to display and enter prices and totals.
 *
 * [symbol] is what gets rendered next to an amount, [nameRes] is what the unit selectors show.
 *
 * Several currencies share a symbol in the wild (`$` alone is used by the US, Canadian,
 * Australian and Mexican dollar), so the symbols here are prefixed where needed to stay
 * distinguishable in a list.
 *
 * [symbol] is deliberately a plain `String` rather than a resource: currency symbols are not
 * translated, which also keeps [com.gasodoapp.gasodo.core.settings.Units] usable from plain unit
 * tests.
 */
enum class Currency(val symbol: String, @param:StringRes val nameRes: Int) {
    USD("$", R.string.currency_usd),
    EUR("€", R.string.currency_eur),
    GBP("£", R.string.currency_gbp),
    JPY("¥", R.string.currency_jpy),
    CNY("CN¥", R.string.currency_cny),
    INR("₹", R.string.currency_inr),
    CAD("CA$", R.string.currency_cad),
    AUD("A$", R.string.currency_aud),
    CHF("CHF", R.string.currency_chf),
    BRL("R$", R.string.currency_brl),
    MXN("MX$", R.string.currency_mxn),
    KRW("₩", R.string.currency_krw),
    SEK("kr", R.string.currency_sek),
    PLN("zł", R.string.currency_pln),
    ZAR("R", R.string.currency_zar),
    TRY("₺", R.string.currency_try);

    /**
     * Localised name of this currency, for the unit selectors.
     */
    @Composable
    fun name(): String = stringResource(nameRes)

    companion object {

        /**
         * Entry used when nothing has been stored yet.
         */
        val DEFAULT: Currency = EUR

        /**
         * Resolves a persisted [name] (an enum constant name), falling back to [DEFAULT] when the
         * value is absent or unknown.
         *
         * An unknown name can only happen after downgrading from a version that knew more entries,
         * or from a hand edited preferences file, so treating it as the default keeps the app
         * usable instead of crashing on `valueOf`.
         */
        fun fromName(name: String?): Currency =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
