package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllLastPrices
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllOrderBooks
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTrades
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTradingStatuses
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.AsyncLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.BaseLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.BlockingLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.CoroutineLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.AsyncOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.BaseOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.BlockingOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.CoroutineOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.AsyncTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.BaseTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.BlockingTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.CoroutineTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.AsyncTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.BaseTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.BlockingTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.CoroutineTradeHandler
import io.github.dankosik.starter.invest.exception.CommonException
import io.github.dankosik.starter.invest.exception.ErrorCode
import io.github.dankosik.starter.invest.processor.marketdata.AsyncLastPriceStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.AsyncOrderBookStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.AsyncTradingStatusStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.BaseCandleStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BaseLastPriceStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BaseOrderBookStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BaseTradeStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BaseTradingStatusStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BlockingLastPriceStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.BlockingOrderBookStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.BlockingTradingStatusStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.CoroutineLastPriceStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.CoroutineOrderBookStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.CoroutineTradingStatusStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.TradeStreamProcessorAdapterFactory
import io.github.dankosik.starter.invest.processor.marketdata.common.AsyncMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.CoroutineMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.runAfterEachLastPriceBookHandler
import io.github.dankosik.starter.invest.processor.marketdata.runAfterEachOrderBookHandler
import io.github.dankosik.starter.invest.processor.marketdata.runAfterEachTradingStatusHandler
import io.github.dankosik.starter.invest.processor.marketdata.runBeforeEachLastPriceHandler
import io.github.dankosik.starter.invest.processor.marketdata.runBeforeEachOrderBookHandler
import io.github.dankosik.starter.invest.processor.marketdata.runBeforeEachTradingStatusHandler
import io.github.dankosik.starter.invest.processor.marketdata.toMarketDataProcessor
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(StreamProcessorsAutoConfiguration::class)
class StreamProcessorAdaptersAutoConfiguration(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>
) {

    @Bean
    fun baseMarketDataStreamProcessor(): List<BaseMarketDataStreamProcessor> =
        createAllOrderBooksStreamProcessors() +
                createAllOrderBooksStreamProcessorsFromType() +
                createAllTradeStreamProcessors() +
                createAllTradeStreamProcessorsFromType() +
                createAllLastPriceStreamProcessors() +
                createAllLastPriceStreamProcessorsFromType() +
                createAllTradingStatusStreamProcessors() +
                createAllTradingStatusStreamProcessorsFromType() +
                createAllCoroutineMarketDataStreamProcessorAdapters() +
                createAllAsyncMarketDataStreamProcessorAdapters() +
                createAllBlockingMarketDataStreamProcessorAdapters() +
                createAllCandleStreamProcessorsFromType()

    private fun createAllCoroutineMarketDataStreamProcessorAdapters(): List<BaseMarketDataStreamProcessor> =
        applicationContext.getBeansOfType(CoroutineMarketDataStreamProcessorAdapter::class.java).values.toList()

    private fun createAllAsyncMarketDataStreamProcessorAdapters(): List<BaseMarketDataStreamProcessor> =
        applicationContext.getBeansOfType(AsyncMarketDataStreamProcessorAdapter::class.java).values.toList()

    private fun createAllBlockingMarketDataStreamProcessorAdapters(): List<BaseMarketDataStreamProcessor> =
        applicationContext.getBeansOfType(BlockingMarketDataStreamProcessorAdapter::class.java).values.toList()

    private fun createAllOrderBooksStreamProcessors(): List<BaseMarketDataStreamProcessor> =
        applicationContext.getBeansWithAnnotation(HandleAllOrderBooks::class.java).values
            .filterIsInstance<BaseOrderBookHandler>()
            .mapNotNull { handler ->
                val annotation =
                    handler.javaClass.declaredAnnotations.filterIsInstance<HandleAllOrderBooks>().first()
                if (annotation.instrumentsUids.isEmpty() && annotation.tickers.isEmpty() && annotation.figies.isEmpty()) {
                    when (handler) {
                        is BlockingOrderBookHandler -> {
                            BlockingOrderBookStreamProcessorAdapter { handler.handleBlocking(it) }.apply {
                                if (annotation.afterEachOrderBookHandler) runAfterEachOrderBookHandler()
                                if (annotation.beforeEachOrderBookHandler) runBeforeEachOrderBookHandler()
                            }.toMarketDataProcessor()
                        }

                        is AsyncOrderBookHandler -> {
                            AsyncOrderBookStreamProcessorAdapter { handler.handleAsync(it) }.apply {
                                if (annotation.afterEachOrderBookHandler) runAfterEachOrderBookHandler()
                                if (annotation.beforeEachOrderBookHandler) runBeforeEachOrderBookHandler()
                            }.toMarketDataProcessor()
                        }

                        is CoroutineOrderBookHandler -> {
                            CoroutineOrderBookStreamProcessorAdapter { handler.handle(it) }.apply {
                                if (annotation.afterEachOrderBookHandler) runAfterEachOrderBookHandler()
                                if (annotation.beforeEachOrderBookHandler) runBeforeEachOrderBookHandler()
                            }.toMarketDataProcessor()
                        }

                        else -> throw CommonException(ErrorCode.HANDLER_NOT_FOUND)
                    }
                } else {
                    null
                }
            }

    private fun createAllOrderBooksStreamProcessorsFromType(): List<BaseMarketDataStreamProcessor> =
        applicationContext.getBeansOfType(BaseOrderBookStreamProcessor::class.java).values.map { it.toMarketDataProcessor() }

    private fun createAllTradeStreamProcessors() =
        applicationContext.getBeansWithAnnotation(HandleAllTrades::class.java).values
            .filterIsInstance<BaseTradeHandler>()
            .mapNotNull { handler ->
                val annotation =
                    handler.javaClass.declaredAnnotations.filterIsInstance<HandleAllTrades>().first()
                if (annotation.instrumentsUids.isEmpty() && annotation.tickers.isEmpty() && annotation.figies.isEmpty()) {
                    when (handler) {
                        is BlockingTradeHandler -> {
                            TradeStreamProcessorAdapterFactory
                                .runAfterEachCandleHandler(annotation.afterEachTradesHandler)
                                .runBeforeEachCandleHandler(annotation.beforeEachTradesHandler)
                                .createBlockingHandler { handler.handleBlocking(it) }
                                .toMarketDataProcessor()
                        }

                        is AsyncTradeHandler -> {
                            TradeStreamProcessorAdapterFactory
                                .runAfterEachCandleHandler(annotation.afterEachTradesHandler)
                                .runBeforeEachCandleHandler(annotation.beforeEachTradesHandler)
                                .createAsyncHandler { handler.handleAsync(it) }
                                .toMarketDataProcessor()
                        }

                        is CoroutineTradeHandler -> {
                            TradeStreamProcessorAdapterFactory
                                .runAfterEachCandleHandler(annotation.afterEachTradesHandler)
                                .runBeforeEachCandleHandler(annotation.beforeEachTradesHandler)
                                .createCoroutineHandler { handler.handle(it) }
                                .toMarketDataProcessor()
                        }

                        else -> throw CommonException(ErrorCode.HANDLER_NOT_FOUND)
                    }
                } else {
                    null
                }
            }

    private fun createAllTradeStreamProcessorsFromType(): List<BaseMarketDataStreamProcessor> =
        applicationContext.getBeansOfType(BaseTradeStreamProcessor::class.java).values.map { it.toMarketDataProcessor() }

    private fun createAllLastPriceStreamProcessors() =
        applicationContext.getBeansWithAnnotation(HandleAllLastPrices::class.java).values
            .filterIsInstance<BaseLastPriceHandler>()
            .mapNotNull { handler ->
                val annotation =
                    handler.javaClass.declaredAnnotations.filterIsInstance<HandleAllLastPrices>().first()
                if (annotation.instrumentsUids.isEmpty() && annotation.tickers.isEmpty() && annotation.figies.isEmpty()) {
                    when (handler) {
                        is BlockingLastPriceHandler -> {
                            BlockingLastPriceStreamProcessorAdapter {
                                handler.handleBlocking(it)
                            }.apply {
                                if (annotation.afterEachLastPriceHandler) runAfterEachLastPriceBookHandler()
                                if (annotation.beforeEachLastPriceHandler) runBeforeEachLastPriceHandler()
                            }.toMarketDataProcessor()
                        }

                        is AsyncLastPriceHandler -> {
                            AsyncLastPriceStreamProcessorAdapter { handler.handleAsync(it) }.apply {
                                if (annotation.afterEachLastPriceHandler) runAfterEachLastPriceBookHandler()
                                if (annotation.beforeEachLastPriceHandler) runBeforeEachLastPriceHandler()
                            }.toMarketDataProcessor()
                        }

                        is CoroutineLastPriceHandler -> {
                            CoroutineLastPriceStreamProcessorAdapter { handler.handle(it) }.apply {
                                if (annotation.afterEachLastPriceHandler) runAfterEachLastPriceBookHandler()
                                if (annotation.beforeEachLastPriceHandler) runBeforeEachLastPriceHandler()
                            }.toMarketDataProcessor()
                        }

                        else -> throw CommonException(ErrorCode.HANDLER_NOT_FOUND)
                    }
                } else {
                    null
                }
            }

    private fun createAllLastPriceStreamProcessorsFromType(): List<BaseMarketDataStreamProcessor> =
        applicationContext.getBeansOfType(BaseLastPriceStreamProcessor::class.java).values.map { it.toMarketDataProcessor() }

    private fun createAllCandleStreamProcessorsFromType(): List<BaseMarketDataStreamProcessor> =
        applicationContext.getBeansOfType(BaseCandleStreamProcessor::class.java).values.map { it.toMarketDataProcessor() }

    private fun createAllTradingStatusStreamProcessors() =
        applicationContext.getBeansWithAnnotation(HandleAllTradingStatuses::class.java).values
            .filterIsInstance<BaseTradingStatusHandler>()
            .mapNotNull { handler ->
                val annotation =
                    handler.javaClass.declaredAnnotations.filterIsInstance<HandleAllTradingStatuses>().first()
                if (annotation.instrumentsUids.isEmpty() && annotation.tickers.isEmpty() && annotation.figies.isEmpty()) {
                    when (handler) {
                        is BlockingTradingStatusHandler -> {
                            BlockingTradingStatusStreamProcessorAdapter { handler.handleBlocking(it) }.apply {
                                if (annotation.afterEachTradingStatusHandler) runAfterEachTradingStatusHandler()
                                if (annotation.beforeEachTradingStatusHandler) runBeforeEachTradingStatusHandler()
                            }.toMarketDataProcessor()
                        }

                        is AsyncTradingStatusHandler -> {
                            AsyncTradingStatusStreamProcessorAdapter { handler.handleAsync(it) }.apply {
                                if (annotation.afterEachTradingStatusHandler) runAfterEachTradingStatusHandler()
                                if (annotation.beforeEachTradingStatusHandler) runBeforeEachTradingStatusHandler()
                            }.toMarketDataProcessor()
                        }

                        is CoroutineTradingStatusHandler -> {
                            CoroutineTradingStatusStreamProcessorAdapter { handler.handle(it) }.apply {
                                if (annotation.afterEachTradingStatusHandler) runAfterEachTradingStatusHandler()
                                if (annotation.beforeEachTradingStatusHandler) runBeforeEachTradingStatusHandler()
                            }.toMarketDataProcessor()
                        }

                        else -> throw CommonException(ErrorCode.HANDLER_NOT_FOUND)
                    }
                } else {
                    null
                }
            }

    private fun createAllTradingStatusStreamProcessorsFromType(): List<BaseMarketDataStreamProcessor> =
        applicationContext.getBeansOfType(BaseTradingStatusStreamProcessor::class.java).values.map { it.toMarketDataProcessor() }
}