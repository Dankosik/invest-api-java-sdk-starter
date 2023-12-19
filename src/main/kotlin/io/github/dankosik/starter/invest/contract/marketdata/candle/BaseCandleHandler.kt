package io.github.dankosik.starter.invest.contract.marketdata.candle

interface BaseCandleHandler

fun MutableCollection<Any>.getCandleHandlers() = mapNotNull {
    when (it) {
        is CoroutineCandleHandler -> it
        is AsyncCandleHandler -> it
        is BlockingCandleHandler -> it
        else -> null
    }
}