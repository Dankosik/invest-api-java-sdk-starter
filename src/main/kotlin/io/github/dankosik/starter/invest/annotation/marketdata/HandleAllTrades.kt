package io.github.dankosik.starter.invest.annotation.marketdata

import io.github.dankosik.starter.invest.contract.marketdata.trade.BaseTradeHandler
import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleAllTrades(
    val sandboxOnly: Boolean = false,
    val tickers: Array<String> = [],
    val figies: Array<String> = [],
    val instrumentsUids: Array<String> = [],
    val beforeEachTradesHandler: Boolean = false,
    val afterEachTradesHandler: Boolean = false,
)

fun List<BaseTradeHandler>.extractTickersFromAll() =
    map { it.javaClass.getAnnotation(HandleAllTrades::class.java).tickers }
        .toTypedArray()
        .flatten()