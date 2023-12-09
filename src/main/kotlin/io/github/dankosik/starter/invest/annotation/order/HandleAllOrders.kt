package io.github.dankosik.starter.invest.annotation.order

import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleAllOrders(
    val accounts: Array<String> = [],
    val sandboxOnly: Boolean = false,
)