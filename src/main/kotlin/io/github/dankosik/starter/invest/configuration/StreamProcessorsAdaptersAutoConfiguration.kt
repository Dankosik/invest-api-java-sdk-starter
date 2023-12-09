package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllCandles
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllLastPrices
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllOrderBooks
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTrades
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTradingStatuses
import io.github.dankosik.starter.invest.processor.marketdata.AsyncCandleStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.AsyncLastPriceStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.AsyncOrderBookStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.AsyncTradeStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.AsyncTradingStatusStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BlockingCandleStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.BlockingLastPriceStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.BlockingOrderBookStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.BlockingTradeStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.BlockingTradingStatusStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.CoroutineCandleStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.CoroutineLastPriceStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.CoroutineOrderBookStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.CoroutineTradeStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.CoroutineTradingStatusStreamProcessorAdapter
import io.github.dankosik.starter.invest.contract.marketdata.candle.AsyncCandleHandler
import io.github.dankosik.starter.invest.contract.marketdata.candle.BaseCandleHandler
import io.github.dankosik.starter.invest.contract.marketdata.candle.BlockingCandleHandler
import io.github.dankosik.starter.invest.contract.marketdata.candle.CoroutineCandleHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.AsyncLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.BaseLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.BlockingLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.CoroutineLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.AsyncOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.BaseOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.BlockingOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.CoroutineOrderBookHandler
import io.github.dankosik.starter.invest.processor.marketdata.runAfterEachCandleBookHandlers
import io.github.dankosik.starter.invest.processor.marketdata.runAfterEachLastPriceBookHandlers
import io.github.dankosik.starter.invest.processor.marketdata.runAfterEachOrderBookHandlers
import io.github.dankosik.starter.invest.processor.marketdata.runAfterEachTradingStatusHandlers
import io.github.dankosik.starter.invest.processor.marketdata.runBeforeEachCandleHandlers
import io.github.dankosik.starter.invest.processor.marketdata.runBeforeEachLastPriceHandlers
import io.github.dankosik.starter.invest.processor.marketdata.runBeforeEachOrderBookHandlers
import io.github.dankosik.starter.invest.processor.marketdata.runBeforeEachTradingStatusHandlers
import io.github.dankosik.starter.invest.contract.marketdata.status.AsyncTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.BaseTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.BlockingTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.CoroutineTradingStatusHandler
import io.github.dankosik.starter.invest.processor.marketdata.toMarketDataProcessor
import io.github.dankosik.starter.invest.contract.marketdata.trade.AsyncTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.BaseTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.BlockingTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.CoroutineTradeHandler
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(StreamProcessorsAutoConfiguration::class)
class StreamProcessorsAdaptersAutoConfiguration(
    private val applicationContext: ApplicationContext
) {

    @Bean
    fun baseMarketDataStreamProcessor(): List<BaseMarketDataStreamProcessor> =
        createAllOrderBooksStreamProcessors() +
                createAllCandlesStreamProcessors() +
                createAllTradeStreamProcessors() +
                createAllLastPriceStreamProcessors() +
                createAllTradingStatusStreamProcessors()

    private fun createAllOrderBooksStreamProcessors() =
        applicationContext.getBeansWithAnnotation(HandleAllOrderBooks::class.java).values
            .filterIsInstance<BaseOrderBookHandler>()
            .map { handler ->
                val annotation =
                    handler.javaClass.declaredAnnotations.filterIsInstance<HandleAllOrderBooks>().first()
                when (handler) {
                    is BlockingOrderBookHandler -> {
                        BlockingOrderBookStreamProcessorAdapter { handler.handleBlocking(it) }.apply {
                            if (annotation.afterEachOrderBookHandler) runAfterEachOrderBookHandlers()
                            if (annotation.beforeEachOrderBookHandler) runBeforeEachOrderBookHandlers()
                        }.toMarketDataProcessor()
                    }

                    is AsyncOrderBookHandler -> {
                        AsyncOrderBookStreamProcessorAdapter { handler.handleAsync(it) }.apply {
                            if (annotation.afterEachOrderBookHandler) runAfterEachOrderBookHandlers()
                            if (annotation.beforeEachOrderBookHandler) runBeforeEachOrderBookHandlers()
                        }.toMarketDataProcessor()
                    }

                    is CoroutineOrderBookHandler -> {
                        CoroutineOrderBookStreamProcessorAdapter { handler.handle(it) }.apply {
                            if (annotation.afterEachOrderBookHandler) runAfterEachOrderBookHandlers()
                            if (annotation.beforeEachOrderBookHandler) runBeforeEachOrderBookHandlers()
                        }.toMarketDataProcessor()
                    }

                    else -> {
                        throw RuntimeException()
                    }
                }
            }

    private fun createAllCandlesStreamProcessors() =
        applicationContext.getBeansWithAnnotation(HandleAllCandles::class.java).values
            .filterIsInstance<BaseCandleHandler>()
            .map { handler ->
                val annotation =
                    handler.javaClass.declaredAnnotations.filterIsInstance<HandleAllCandles>().first()
                when (handler) {
                    is BlockingCandleHandler -> {
                        BlockingCandleStreamProcessorAdapter { handler.handleBlocking(it) }.apply {
                            if (annotation.afterEachCandleHandler) runAfterEachCandleBookHandlers()
                            if (annotation.beforeEachCandleHandler) runBeforeEachCandleHandlers()
                        }.toMarketDataProcessor()
                    }

                    is AsyncCandleHandler -> {
                        AsyncCandleStreamProcessorAdapter { handler.handleAsync(it) }.apply {
                            if (annotation.afterEachCandleHandler) runAfterEachCandleBookHandlers()
                            if (annotation.beforeEachCandleHandler) runBeforeEachCandleHandlers()
                        }.toMarketDataProcessor()
                    }

                    is CoroutineCandleHandler -> {
                        CoroutineCandleStreamProcessorAdapter { handler.handle(it) }.apply {
                            if (annotation.afterEachCandleHandler) runAfterEachCandleBookHandlers()
                            if (annotation.beforeEachCandleHandler) runBeforeEachCandleHandlers()
                        }.toMarketDataProcessor()
                    }

                    else -> {
                        throw RuntimeException()
                    }
                }
            }

    private fun createAllTradeStreamProcessors() =
        applicationContext.getBeansWithAnnotation(HandleAllTrades::class.java).values
            .filterIsInstance<BaseTradeHandler>()
            .map { handler ->
                val annotation =
                    handler.javaClass.declaredAnnotations.filterIsInstance<HandleAllTrades>().first()
                when (handler) {
                    is BlockingTradeHandler -> {
                        BlockingTradeStreamProcessorAdapter { handler.handleBlocking(it) }.apply {
                            if (annotation.afterEachTradesHandler) runAfterEachTradingStatusHandlers()
                            if (annotation.beforeEachTradesHandler) runBeforeEachTradingStatusHandlers()
                        }.toMarketDataProcessor()
                    }

                    is AsyncTradeHandler -> {
                        AsyncTradeStreamProcessorAdapter { handler.handleAsync(it) }.apply {
                            if (annotation.afterEachTradesHandler) runAfterEachTradingStatusHandlers()
                            if (annotation.beforeEachTradesHandler) runBeforeEachTradingStatusHandlers()
                        }.toMarketDataProcessor()
                    }

                    is CoroutineTradeHandler -> {
                        CoroutineTradeStreamProcessorAdapter { handler.handle(it) }.apply {
                            if (annotation.afterEachTradesHandler) runAfterEachTradingStatusHandlers()
                            if (annotation.beforeEachTradesHandler) runBeforeEachTradingStatusHandlers()
                        }.toMarketDataProcessor()
                    }

                    else -> {
                        throw RuntimeException()
                    }
                }
            }

    private fun createAllLastPriceStreamProcessors() =
        applicationContext.getBeansWithAnnotation(HandleAllLastPrices::class.java).values
            .filterIsInstance<BaseLastPriceHandler>()
            .map { handler ->
                val annotation =
                    handler.javaClass.declaredAnnotations.filterIsInstance<HandleAllLastPrices>().first()
                when (handler) {
                    is BlockingLastPriceHandler -> {
                        BlockingLastPriceStreamProcessorAdapter { handler.handleBlocking(it) }.apply {
                            if (annotation.afterEachLastPriceHandler) runAfterEachLastPriceBookHandlers()
                            if (annotation.beforeEachLastPriceHandler) runBeforeEachLastPriceHandlers()
                        }.toMarketDataProcessor()
                    }

                    is AsyncLastPriceHandler -> {
                        AsyncLastPriceStreamProcessorAdapter { handler.handleAsync(it) }.apply {
                            if (annotation.afterEachLastPriceHandler) runAfterEachLastPriceBookHandlers()
                            if (annotation.beforeEachLastPriceHandler) runBeforeEachLastPriceHandlers()
                        }.toMarketDataProcessor()
                    }

                    is CoroutineLastPriceHandler -> {
                        CoroutineLastPriceStreamProcessorAdapter { handler.handle(it) }.apply {
                            if (annotation.afterEachLastPriceHandler) runAfterEachLastPriceBookHandlers()
                            if (annotation.beforeEachLastPriceHandler) runBeforeEachLastPriceHandlers()
                        }.toMarketDataProcessor()
                    }

                    else -> {
                        throw RuntimeException()
                    }
                }
            }

    private fun createAllTradingStatusStreamProcessors() =
        applicationContext.getBeansWithAnnotation(HandleAllTradingStatuses::class.java).values
            .filterIsInstance<BaseTradingStatusHandler>()
            .map { handler ->
                val annotation =
                    handler.javaClass.declaredAnnotations.filterIsInstance<HandleAllTradingStatuses>().first()
                when (handler) {
                    is BlockingTradingStatusHandler -> {
                        BlockingTradingStatusStreamProcessorAdapter { handler.handleBlocking(it) }.apply {
                            if (annotation.afterEachTradingStatusHandler) runAfterEachTradingStatusHandlers()
                            if (annotation.beforeEachTradingStatusHandler) runBeforeEachTradingStatusHandlers()
                        }.toMarketDataProcessor()
                    }

                    is AsyncTradingStatusHandler -> {
                        AsyncTradingStatusStreamProcessorAdapter { handler.handleAsync(it) }.apply {
                            if (annotation.afterEachTradingStatusHandler) runAfterEachTradingStatusHandlers()
                            if (annotation.beforeEachTradingStatusHandler) runBeforeEachTradingStatusHandlers()
                        }.toMarketDataProcessor()
                    }

                    is CoroutineTradingStatusHandler -> {
                        CoroutineTradingStatusStreamProcessorAdapter { handler.handle(it) }.apply {
                            if (annotation.afterEachTradingStatusHandler) runAfterEachTradingStatusHandlers()
                            if (annotation.beforeEachTradingStatusHandler) runBeforeEachTradingStatusHandlers()
                        }.toMarketDataProcessor()
                    }

                    else -> {
                        throw RuntimeException()
                    }
                }
            }
}