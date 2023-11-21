package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.configuration.properties.TinkoffApiProperties
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import ru.tinkoff.piapi.core.InvestApi
import ru.tinkoff.piapi.core.stream.MarketDataStreamService
import ru.tinkoff.piapi.core.stream.MarketDataSubscriptionService
import ru.tinkoff.piapi.core.stream.StreamProcessor

@AutoConfiguration
@ConditionalOnProperty(name = ["tinkoff.starter.apiToken.fullAccess"])
@EnableConfigurationProperties(TinkoffApiProperties::class)
class FullAccessServicesAutoConfiguration(
    private val tinkoffApiProperties: TinkoffApiProperties,
) {

    @Volatile
    private var id: Int = 0

    @Bean
    fun investApi() = InvestApi.create(tinkoffApiProperties.apiToken.fullAccess!!)

    @Bean
    fun marketDataService() = investApi().marketDataService

    @Bean
    fun instrumentsService() = investApi().instrumentsService

    @Bean
    fun ordersService() = investApi().ordersService

    @Bean
    fun sandboxService() = investApi().sandboxService

    @Bean
    fun userService() = investApi().userService

    @Bean
    fun operationsService() = investApi().operationsService

    @Bean
    fun stopOrdersService() = investApi().stopOrdersService

    @Bean
    fun ordersStreamService() = investApi().ordersStreamService

    @Bean
    fun operationsStreamService() = investApi().operationsStreamService!!

    @Bean
    fun marketDataStreamService() = investApi().marketDataStreamService

    @Bean
    fun commonMarketDataSubscription(
        marketDataStreamService: MarketDataStreamService,
        commonMarketDataStreamProcessor: StreamProcessor<MarketDataResponse>
    ): MarketDataSubscriptionService? = marketDataStreamService.newStream(
        "commonMarketDataSubscription",
        commonMarketDataStreamProcessor,
        null
    )
}
