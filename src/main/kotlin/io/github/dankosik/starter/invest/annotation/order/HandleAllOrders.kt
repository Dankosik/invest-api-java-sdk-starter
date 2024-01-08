package io.github.dankosik.starter.invest.annotation.order

import io.github.dankosik.starter.invest.contract.orders.BaseOrderHandler
import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleAllOrders(
    val accounts: Array<String> = [],
    val tickers: Array<String> = [],
    val figies: Array<String> = [],
    val instrumentsUids: Array<String> = [],
    val sandboxOnly: Boolean = false,
)

fun List<BaseOrderHandler>.extractTickersFromAll() =
    map { it.javaClass.getAnnotation(HandleAllOrders::class.java).tickers }
        .toTypedArray()
        .flatten()