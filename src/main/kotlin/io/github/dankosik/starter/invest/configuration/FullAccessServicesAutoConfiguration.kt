package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.configuration.properties.TinkoffApiProperties
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
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

    @Bean
    fun investApi() = InvestApi.create(tinkoffApiProperties.apiToken.fullAccess!!)

    @Bean
    fun marketDataService(investApi: InvestApi) = investApi.marketDataService

    @Bean
    fun instrumentsService(investApi: InvestApi) = investApi.instrumentsService

    @Bean
    fun ordersService(investApi: InvestApi) = investApi.ordersService

    @Bean
    fun sandboxService(investApi: InvestApi) = investApi.sandboxService

    @Bean
    fun userService(investApi: InvestApi) = investApi.userService

    @Bean
    fun operationsService(investApi: InvestApi) = investApi.operationsService

    @Bean
    fun stopOrdersService(investApi: InvestApi) = investApi.stopOrdersService

    @Bean
    fun ordersStreamService(investApi: InvestApi) = investApi.ordersStreamService

    @Bean
    fun operationsStreamService(investApi: InvestApi) = investApi.operationsStreamService!!

    @Bean
    fun marketDataStreamService(investApi: InvestApi) = investApi.marketDataStreamService
}
