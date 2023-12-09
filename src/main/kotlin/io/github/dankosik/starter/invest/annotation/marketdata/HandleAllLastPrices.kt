package io.github.dankosik.starter.invest.annotation.marketdata

import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleAllLastPrices(
    val sandboxOnly: Boolean = false,
    val beforeEachLastPriceHandler: Boolean = false,
    val afterEachLastPriceHandler: Boolean = false,
)
