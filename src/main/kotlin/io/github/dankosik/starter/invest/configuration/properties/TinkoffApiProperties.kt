package io.github.dankosik.starter.invest.configuration.properties

import jakarta.validation.constraints.PositiveOrZero
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("tinkoff.starter")
data class TinkoffApiProperties(
    val apiToken: ApiToken,
    val orderBookHandler: OrderBookHandlerProperties? = null,
    val marketDataStreamProcessor: MarketDataStreamProcessor? = null
)

data class ApiToken(
    val readonly: String? = null,
    val fullAccess: String? = null,
    val sandbox: String? = null,
)

data class OrderBookHandlerProperties(
    @field:PositiveOrZero
    val orderBookDepth: Int? = null
)

data class MarketDataStreamProcessor(
    val useCustomOnly: Boolean? = false,
)

