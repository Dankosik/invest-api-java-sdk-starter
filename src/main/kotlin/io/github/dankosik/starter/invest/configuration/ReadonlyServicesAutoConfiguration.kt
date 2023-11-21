package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.configuration.properties.TinkoffApiProperties
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import ru.tinkoff.piapi.core.InvestApi
import ru.tinkoff.piapi.core.stream.MarketDataStreamService
import ru.tinkoff.piapi.core.stream.MarketDataSubscriptionService
import ru.tinkoff.piapi.core.stream.StreamProcessor

@AutoConfiguration
@ConditionalOnProperty(name = ["tinkoff.starter.apiToken.readonly"])
@EnableConfigurationProperties(TinkoffApiProperties::class)
class ReadonlyServicesAutoConfiguration(
    private val tinkoffApiProperties: TinkoffApiProperties,
) {

    @Bean
    fun investApiReadonly() = InvestApi.createReadonly(tinkoffApiProperties.apiToken.readonly!!)

    @Bean
    fun marketDataServiceReadonly() = investApiReadonly().marketDataService

    @Bean
    fun instrumentsServiceReadonly() = investApiReadonly().instrumentsService

    @Bean
    fun ordersServiceReadonly() = investApiReadonly().ordersService

    @Bean
    fun sandboxServiceReadonly() = investApiReadonly().sandboxService

    @Bean
    fun userServiceReadonly() = investApiReadonly().userService

    @Bean
    fun operationsServiceReadonly() = investApiReadonly().operationsService

    @Bean
    fun stopOrdersServiceReadonly() = investApiReadonly().stopOrdersService

    @Bean
    fun ordersStreamServiceReadonly() = investApiReadonly().ordersStreamService

    @Bean
    fun marketDataStreamServiceReadonly() = investApiReadonly().marketDataStreamService

    @Bean
    fun operationsStreamServiceReadonly() = investApiReadonly().operationsStreamService!!


    @Bean
    @ConditionalOnMissingBean(name = ["commonDataSubscription"])
    fun commonDataSubscription(
        marketDataStreamServiceReadonly: MarketDataStreamService,
        commonMarketDataStreamProcessor: StreamProcessor<MarketDataResponse>
    ): MarketDataSubscriptionService? = marketDataStreamServiceReadonly.newStream(
        "commonDataSubscriptionReadonly",
        commonMarketDataStreamProcessor,
        null
    )
}