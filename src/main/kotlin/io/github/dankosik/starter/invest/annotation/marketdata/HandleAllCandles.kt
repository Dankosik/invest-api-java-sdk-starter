package io.github.dankosik.starter.invest.annotation.marketdata

import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleAllCandles(
    val sandboxOnly: Boolean = false,
    val beforeEachCandleHandler: Boolean = false,
    val afterEachCandleHandler: Boolean = false,
)
