package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.configuration.properties.TinkoffApiProperties
import mu.KLogging
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
@ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
@EnableConfigurationProperties(TinkoffApiProperties::class)
class SandboxServicesAutoConfiguration(
    private val tinkoffApiProperties: TinkoffApiProperties,
) {

    @Bean
    fun investApiSandbox() = InvestApi.createSandbox(tinkoffApiProperties.apiToken.sandbox!!)

    @Bean
    fun marketDataServiceSandbox(investApiSandbox: InvestApi) = investApiSandbox.marketDataService

    @Bean
    fun instrumentsServiceSandbox(investApiSandbox: InvestApi) = investApiSandbox.instrumentsService

    @Bean
    fun ordersServiceSandbox(investApiSandbox: InvestApi) = investApiSandbox.ordersService

    @Bean
    fun sandboxServiceSandbox(investApiSandbox: InvestApi) = investApiSandbox.sandboxService

    @Bean
    fun userServiceSandbox(investApiSandbox: InvestApi) = investApiSandbox.userService

    @Bean
    fun operationsServiceSandbox(investApiSandbox: InvestApi) = investApiSandbox.operationsService

    @Bean
    fun stopOrdersServiceSandbox(investApiSandbox: InvestApi) = investApiSandbox.stopOrdersService

    @Bean
    fun ordersStreamServiceSandbox(investApiSandbox: InvestApi) = investApiSandbox.ordersStreamService

    @Bean
    fun marketDataStreamServiceSandbox(investApiSandbox: InvestApi) = investApiSandbox.marketDataStreamService

    @Bean
    fun operationsStreamServiceSandbox(investApiSandbox: InvestApi) = investApiSandbox.operationsStreamService!!
}