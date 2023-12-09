package io.github.dankosik.starter.invest.annotation.marketdata

import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleAllTrades(
    val sandboxOnly: Boolean = false,
    val beforeEachTradesHandler: Boolean = false,
    val afterEachTradesHandler: Boolean = false,
)