package io.github.dankosik.starter.invest.contract.orders

interface BaseOrderHandler

fun MutableCollection<Any>.getOrderHandlers() = mapNotNull {
    when (it) {
        is CoroutineOrderHandler -> it
        is AsyncOrderHandler -> it
        is BlockingOrderHandler -> it
        else -> null
    }
}