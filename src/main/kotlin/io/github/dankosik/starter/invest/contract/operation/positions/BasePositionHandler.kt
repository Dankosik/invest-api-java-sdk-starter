package io.github.dankosik.starter.invest.contract.operation.positions

interface BasePositionHandler

fun MutableCollection<Any>.getPositionHandlers() = mapNotNull {
    when (it) {
        is CoroutinePositionHandler -> it
        is AsyncPositionHandler -> it
        is BlockingPositionHandler -> it
        else -> null
    }
}