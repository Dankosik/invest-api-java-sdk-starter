package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.registry.marketdata.CandleHandlerRegistry
import io.github.dankosik.starter.invest.registry.marketdata.LastPriceHandlerRegistry
import io.github.dankosik.starter.invest.registry.marketdata.OrderBookHandlerRegistry
import io.github.dankosik.starter.invest.registry.marketdata.TradesHandlerRegistry
import io.github.dankosik.starter.invest.registry.marketdata.TradingStatusHandlerRegistry
import io.github.dankosik.starter.invest.registry.operation.PortfolioHandlerRegistry
import io.github.dankosik.starter.invest.registry.operation.PositionsHandlerRegistry
import io.github.dankosik.starter.invest.registry.order.OrdersHandlerRegistry
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(name = ["tickerToUidMap"])
class RegistryAutoConfiguration(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>
) {

    @Bean
    internal fun candleHandlerRegistry() = CandleHandlerRegistry(applicationContext, tickerToUidMap)

    @Bean
    internal fun lastPriceHandlerRegistry() = LastPriceHandlerRegistry(applicationContext, tickerToUidMap)

    @Bean
    internal fun orderBookHandlerRegistry() = OrderBookHandlerRegistry(applicationContext, tickerToUidMap)

    @Bean
    internal fun tradesHandlerRegistry() = TradesHandlerRegistry(applicationContext, tickerToUidMap)

    @Bean
    internal fun tradingStatusHandlerRegistry() = TradingStatusHandlerRegistry(applicationContext, tickerToUidMap)

    @Bean
    internal fun portfolioHandlerRegistry() = PortfolioHandlerRegistry(applicationContext)

    @Bean
    internal fun positionsHandlerRegistry() = PositionsHandlerRegistry(applicationContext)

    @Bean
    internal fun ordersHandlerRegistry() = OrdersHandlerRegistry(applicationContext, tickerToUidMap)
}