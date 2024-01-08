package io.github.dankosik.starter.invest.annotation.marketdata

import io.github.dankosik.starter.invest.contract.marketdata.candle.BaseCandleHandler
import org.springframework.stereotype.Service
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleAllCandles(
    val sandboxOnly: Boolean = false,
    val tickers: Array<String> = [],
    val figies: Array<String> = [],
    val instrumentsUids: Array<String> = [],
    val beforeEachCandleHandler: Boolean = false,
    val afterEachCandleHandler: Boolean = false,
    val waitClose: Boolean = false,
    val subscriptionInterval: SubscriptionInterval = SubscriptionInterval.SUBSCRIPTION_INTERVAL_UNSPECIFIED,
)

fun List<BaseCandleHandler>.extractTickersFromAll() =
    map { it.javaClass.getAnnotation(HandleAllCandles::class.java).tickers }
        .toTypedArray()
        .flatten()
