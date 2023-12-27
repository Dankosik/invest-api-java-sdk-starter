package io.github.dankosik.starter.invest.annotation.marketdata

import io.github.dankosik.starter.invest.contract.marketdata.status.BaseTradingStatusHandler
import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleAllTradingStatuses(
    val sandboxOnly: Boolean = false,
    val tickers: Array<String> = [],
    val figies: Array<String> = [],
    val instrumentsUids: Array<String> = [],
    val beforeEachTradingStatusHandler: Boolean = false,
    val afterEachTradingStatusHandler: Boolean = false,
)

fun List<BaseTradingStatusHandler>.extractTickersFromAll() =
    map { it.javaClass.getAnnotation(HandleAllTradingStatuses::class.java).tickers }
        .toTypedArray()
        .flatten()