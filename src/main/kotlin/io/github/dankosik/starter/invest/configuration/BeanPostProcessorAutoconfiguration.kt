package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.validation.marketadata.CandleBeanPostProcessor
import io.github.dankosik.starter.invest.validation.marketadata.LastPriceBeanPostProcessor
import io.github.dankosik.starter.invest.validation.marketadata.OrderBookHandlerBeanPostProcessor
import io.github.dankosik.starter.invest.validation.marketadata.TradesHandlerBeanPostProcessor
import io.github.dankosik.starter.invest.validation.marketadata.TradingStatusBeanPostProcessor
import io.github.dankosik.starter.invest.validation.operation.PortfolioBeanPostProcessor
import io.github.dankosik.starter.invest.validation.operation.PositionsBeanPostProcessor
import io.github.dankosik.starter.invest.validation.orders.OrdersBeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class BeanPostProcessorAutoconfiguration {

    @Bean
    internal fun candleBeanPostProcessor() = CandleBeanPostProcessor()

    @Bean
    internal fun lastPriceBeanPostProcessor() = LastPriceBeanPostProcessor()

    @Bean
    internal fun orderBookHandlerBeanPostProcessor() = OrderBookHandlerBeanPostProcessor()

    @Bean
    internal fun tradesHandlerBeanPostProcessor() = TradesHandlerBeanPostProcessor()

    @Bean
    internal fun tradingStatusBeanPostProcessor() = TradingStatusBeanPostProcessor()

    @Bean
    internal fun portfolioBeanPostProcessor() = PortfolioBeanPostProcessor()

    @Bean
    internal fun positionsBeanPostProcessor() = PositionsBeanPostProcessor()

    @Bean
    internal fun ordersBeanPostProcessor() = OrdersBeanPostProcessor()
}