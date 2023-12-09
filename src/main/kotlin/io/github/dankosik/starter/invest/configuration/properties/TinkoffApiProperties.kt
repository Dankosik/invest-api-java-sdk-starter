package io.github.dankosik.starter.invest.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("tinkoff.starter")
data class TinkoffApiProperties(
    val apiToken: ApiToken,
    val subscription: SubscriptionProperties? = null,
)

data class ApiToken(
    val readonly: String? = null,
    val fullAccess: String? = null,
    val sandbox: String? = null,
)

data class SubscriptionProperties(
    val orderBook: OrderBookProperties? = null,
)

data class OrderBookProperties(
    val depth: Int? = null,
)