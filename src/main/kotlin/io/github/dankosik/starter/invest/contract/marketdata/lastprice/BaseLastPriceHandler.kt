package io.github.dankosik.starter.invest.contract.marketdata.lastprice

interface BaseLastPriceHandler

fun MutableCollection<Any>.getLastPriceHandlers() = mapNotNull {
    when (it) {
        is CoroutineLastPriceHandler -> it
        is AsyncLastPriceHandler -> it
        is BlockingLastPriceHandler -> it
        else -> null
    }
}