package io.github.dankosik.starter.invest.annotation.marketdata

import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleAllOrderBooks(
    val sandboxOnly: Boolean = false,
    val beforeEachOrderBookHandler: Boolean = false,
    val afterEachOrderBookHandler: Boolean = false,
)
