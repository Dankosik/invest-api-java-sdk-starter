package io.github.dankosik.starter.invest.annotation.marketdata

import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleAllTradingStatuses(
    val sandboxOnly: Boolean = false,
    val beforeEachTradingStatusHandler: Boolean = false,
    val afterEachTradingStatusHandler: Boolean = false,
)
