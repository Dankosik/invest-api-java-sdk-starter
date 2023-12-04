package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.processor.marketadata.CandleBeanPostProcessor
import io.github.dankosik.starter.invest.processor.marketadata.LastPriceBeanPostProcessor
import io.github.dankosik.starter.invest.processor.marketadata.OrderBookHandlerBeanPostProcessor
import io.github.dankosik.starter.invest.processor.marketadata.TradesHandlerBeanPostProcessor
import io.github.dankosik.starter.invest.processor.marketadata.TradingStatusBeanPostProcessor
import io.github.dankosik.starter.invest.processor.operation.PortfolioBeanPostProcessor
import io.github.dankosik.starter.invest.processor.operation.PositionsBeanPostProcessor
import io.github.dankosik.starter.invest.processor.orders.OrdersBeanPostProcessor
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