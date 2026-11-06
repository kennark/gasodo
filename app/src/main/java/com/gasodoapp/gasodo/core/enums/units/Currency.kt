package com.gasodoapp.gasodo.core.enums.units

/**
 * Currency used to display and enter prices and totals.
 *
 * [symbol] is what gets rendered next to an amount, [displayName] is what the unit selectors show.
 *
 * Several currencies share a symbol in the wild (`$` alone is used by the US, Canadian,
 * Australian and Mexican dollar), so the symbols here are prefixed where needed to stay
 * distinguishable in a list.
 */
enum class Currency(val symbol: String, val displayName: String) {
    USD("$", "US Dollar"),
    EUR("€", "Euro"),
    GBP("£", "British Pound"),
    JPY("¥", "Japanese Yen"),
    CNY("CN¥", "Chinese Yuan"),
    INR("₹", "Indian Rupee"),
    CAD("CA$", "Canadian Dollar"),
    AUD("A$", "Australian Dollar"),
    CHF("CHF", "Swiss Franc"),
    BRL("R$", "Brazilian Real"),
    MXN("MX$", "Mexican Peso"),
    KRW("₩", "South Korean Won"),
    SEK("kr", "Swedish Krona"),
    PLN("zł", "Polish Zloty"),
    ZAR("R", "South African Rand"),
    TRY("₺", "Turkish Lira");

    override fun toString(): String = displayName

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
