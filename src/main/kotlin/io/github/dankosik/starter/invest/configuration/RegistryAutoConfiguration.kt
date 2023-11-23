package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.registry.marketdata.CandleHandlerRegistry
import io.github.dankosik.starter.invest.registry.marketdata.LastPriceHandlerRegistry
import io.github.dankosik.starter.invest.registry.marketdata.OrderBookHandlerRegistry
import io.github.dankosik.starter.invest.registry.marketdata.TradesHandlerRegistry
import io.github.dankosik.starter.invest.registry.marketdata.TradingStatusHandlerRegistry
import io.github.dankosik.starter.invest.registry.operation.PortfolioHandlerRegistry
import io.github.dankosik.starter.invest.registry.operation.PositionsHandlerRegistry
import io.github.dankosik.starter.invest.registry.order.OrdersHandlerRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
@AutoConfigureAfter(name = ["tickerToUidMap"])
class RegistryAutoConfiguration(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>
) {

    @Bean
    fun candleHandlerRegistry() = CandleHandlerRegistry(applicationContext, tickerToUidMap)

    @Bean
    fun lastPriceHandlerRegistry() = LastPriceHandlerRegistry(applicationContext, tickerToUidMap)

    @Bean
    fun orderBookHandlerRegistry() = OrderBookHandlerRegistry(applicationContext, tickerToUidMap)

    @Bean
    fun tradesHandlerRegistry() = TradesHandlerRegistry(applicationContext, tickerToUidMap)

    @Bean
    fun tradingStatusHandlerRegistry() = TradingStatusHandlerRegistry(applicationContext, tickerToUidMap)

    @Bean
    fun portfolioHandlerRegistry() = PortfolioHandlerRegistry(applicationContext)

    @Bean
    fun positionsHandlerRegistry() = PositionsHandlerRegistry(applicationContext)

    @Bean
    fun ordersHandlerRegistry() = OrdersHandlerRegistry(applicationContext, tickerToUidMap)
}