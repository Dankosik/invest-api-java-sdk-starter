package io.github.dankosik.starter.invest.annotation.marketdata

import io.github.dankosik.starter.invest.contract.marketdata.orderbook.BaseOrderBookHandler
import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleAllOrderBooks(
    val sandboxOnly: Boolean = false,
    val tickers: Array<String> = [],
    val figies: Array<String> = [],
    val instrumentsUids: Array<String> = [],
    val beforeEachOrderBookHandler: Boolean = false,
    val afterEachOrderBookHandler: Boolean = false,
)

fun List<BaseOrderBookHandler>.extractTickersFromAll() =
    map { it.javaClass.getAnnotation(HandleAllOrderBooks::class.java).tickers }
        .toTypedArray()
        .flatten()