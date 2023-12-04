package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.configuration.properties.TinkoffApiProperties
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
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
    fun marketDataServiceReadonly(investApiReadonly: InvestApi) = investApiReadonly.marketDataService

    @Bean
    fun instrumentsServiceReadonly(investApiReadonly: InvestApi) = investApiReadonly.instrumentsService

    @Bean
    fun ordersServiceReadonly(investApiReadonly: InvestApi) = investApiReadonly.ordersService

    @Bean
    fun sandboxServiceReadonly(investApiReadonly: InvestApi) = investApiReadonly.sandboxService

    @Bean
    fun userServiceReadonly(investApiReadonly: InvestApi) = investApiReadonly.userService

    @Bean
    fun operationsServiceReadonly(investApiReadonly: InvestApi) = investApiReadonly.operationsService

    @Bean
    fun stopOrdersServiceReadonly(investApiReadonly: InvestApi) = investApiReadonly.stopOrdersService

    @Bean
    fun ordersStreamServiceReadonly(investApiReadonly: InvestApi) = investApiReadonly.ordersStreamService

    @Bean
    fun marketDataStreamServiceReadonly(investApiReadonly: InvestApi) = investApiReadonly.marketDataStreamService

    @Bean
    fun operationsStreamServiceReadonly(investApiReadonly: InvestApi) = investApiReadonly.operationsStreamService!!
}