package io.github.dankosik.starter.invest.contract.operation.portfolio

interface BasePortfolioHandler

fun MutableCollection<Any>.getPortfolioHandlers() = mapNotNull {
    when (it) {
        is CoroutinePortfolioHandler -> it
        is AsyncPortfolioHandler -> it
        is BlockingPortfolioHandler -> it
        else -> null
    }
}