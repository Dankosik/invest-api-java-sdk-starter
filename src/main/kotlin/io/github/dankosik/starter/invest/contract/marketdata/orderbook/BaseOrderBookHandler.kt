package io.github.dankosik.starter.invest.contract.marketdata.orderbook

interface BaseOrderBookHandler

fun MutableCollection<Any>.getOrderBookHandlers() = mapNotNull {
    when (it) {
        is CoroutineOrderBookHandler -> it
        is AsyncOrderBookHandler -> it
        is BlockingOrderBookHandler -> it
        else -> null
    }
}