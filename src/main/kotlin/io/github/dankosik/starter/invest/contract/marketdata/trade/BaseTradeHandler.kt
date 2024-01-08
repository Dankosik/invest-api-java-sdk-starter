package io.github.dankosik.starter.invest.contract.marketdata.trade

interface BaseTradeHandler

fun MutableCollection<Any>.getTradesHandlers() = mapNotNull {
    when (it) {
        is CoroutineTradeHandler -> it
        is AsyncTradeHandler -> it
        is BlockingTradeHandler -> it
        else -> null
    }
}