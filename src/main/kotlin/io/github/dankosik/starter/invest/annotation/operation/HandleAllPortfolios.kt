package io.github.dankosik.starter.invest.annotation.operation

import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleAllPortfolios(
    val sandboxOnly: Boolean = false,
    val accounts: Array<String> = []
)