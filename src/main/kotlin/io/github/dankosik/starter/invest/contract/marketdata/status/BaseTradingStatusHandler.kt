package io.github.dankosik.starter.invest.contract.marketdata.status

interface BaseTradingStatusHandler

fun MutableCollection<Any>.getTradingStatusHandlers() = mapNotNull {
    when (it) {
        is CoroutineTradingStatusHandler -> it
        is AsyncTradingStatusHandler -> it
        is BlockingTradingStatusHandler -> it
        else -> null
    }
}