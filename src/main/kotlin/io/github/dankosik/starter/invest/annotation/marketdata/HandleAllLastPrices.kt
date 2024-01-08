package io.github.dankosik.starter.invest.annotation.marketdata

import io.github.dankosik.starter.invest.contract.marketdata.lastprice.BaseLastPriceHandler
import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleAllLastPrices(
    val sandboxOnly: Boolean = false,
    val tickers: Array<String> = [],
    val figies: Array<String> = [],
    val instrumentsUids: Array<String> = [],
    val beforeEachLastPriceHandler: Boolean = false,
    val afterEachLastPriceHandler: Boolean = false,
)

fun List<BaseLastPriceHandler>.extractTickersFromAll() =
    map { it.javaClass.getAnnotation(HandleAllLastPrices::class.java).tickers }
        .toTypedArray()
        .flatten()
