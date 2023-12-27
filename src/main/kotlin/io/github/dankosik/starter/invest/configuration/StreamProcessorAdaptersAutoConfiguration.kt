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
import io.github.dankosik.starter.invest.processor.marketdata.BaseCandleStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BaseLastPriceStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BaseOrderBookStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BaseTradeStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BaseTradingStatusStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.LastPriceStreamProcessorAdapterFactory
import io.github.dankosik.starter.invest.processor.marketdata.OrderBookStreamProcessorAdapterFactory
import io.github.dankosik.starter.invest.processor.marketdata.TradeStreamProcessorAdapterFactory
import io.github.dankosik.starter.invest.processor.marketdata.TradingStatusStreamProcessorAdapterFactory
import io.github.dankosik.starter.invest.processor.marketdata.common.AsyncMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.CoroutineMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.toMarketDataProcessor
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(StreamProcessorsAutoConfiguration::class)
class StreamProcessorAdaptersAutoConfiguration(
    private val applicationContext: ApplicationContext
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
                        is BlockingOrderBookHandler -> OrderBookStreamProcessorAdapterFactory
                            .runBeforeEachOrderBookHandler(annotation.afterEachOrderBookHandler)
                            .runAfterEachOrderBookHandler(annotation.beforeEachOrderBookHandler)
                            .createBlockingHandler { handler.handleBlocking(it) }
                            .toMarketDataProcessor()

                        is AsyncOrderBookHandler -> OrderBookStreamProcessorAdapterFactory
                            .runBeforeEachOrderBookHandler(annotation.afterEachOrderBookHandler)
                            .runAfterEachOrderBookHandler(annotation.beforeEachOrderBookHandler)
                            .createAsyncHandler { handler.handleAsync(it) }
                            .toMarketDataProcessor()

                        is CoroutineOrderBookHandler -> OrderBookStreamProcessorAdapterFactory
                            .runBeforeEachOrderBookHandler(annotation.afterEachOrderBookHandler)
                            .runAfterEachOrderBookHandler(annotation.beforeEachOrderBookHandler)
                            .createCoroutineHandler { handler.handle(it) }
                            .toMarketDataProcessor()

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
                        is BlockingTradeHandler -> TradeStreamProcessorAdapterFactory
                            .runAfterEachTradeHandler(annotation.afterEachTradesHandler)
                            .runBeforeEachTradeHandler(annotation.beforeEachTradesHandler)
                            .createBlockingHandler { handler.handleBlocking(it) }
                            .toMarketDataProcessor()

                        is AsyncTradeHandler -> TradeStreamProcessorAdapterFactory
                            .runAfterEachTradeHandler(annotation.afterEachTradesHandler)
                            .runBeforeEachTradeHandler(annotation.beforeEachTradesHandler)
                            .createAsyncHandler { handler.handleAsync(it) }
                            .toMarketDataProcessor()

                        is CoroutineTradeHandler -> TradeStreamProcessorAdapterFactory
                            .runAfterEachTradeHandler(annotation.afterEachTradesHandler)
                            .runBeforeEachTradeHandler(annotation.beforeEachTradesHandler)
                            .createCoroutineHandler { handler.handle(it) }
                            .toMarketDataProcessor()

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
                        is BlockingLastPriceHandler -> LastPriceStreamProcessorAdapterFactory
                            .runBeforeEachLastPriceHandler(annotation.afterEachLastPriceHandler)
                            .runAfterEachLastPriceHandler(annotation.beforeEachLastPriceHandler)
                            .createBlockingHandler { handler.handleBlocking(it) }
                            .toMarketDataProcessor()

                        is AsyncLastPriceHandler -> LastPriceStreamProcessorAdapterFactory
                            .runBeforeEachLastPriceHandler(annotation.afterEachLastPriceHandler)
                            .runAfterEachLastPriceHandler(annotation.beforeEachLastPriceHandler)
                            .createAsyncHandler { handler.handleAsync(it) }
                            .toMarketDataProcessor()

                        is CoroutineLastPriceHandler -> LastPriceStreamProcessorAdapterFactory
                            .runBeforeEachLastPriceHandler(annotation.afterEachLastPriceHandler)
                            .runAfterEachLastPriceHandler(annotation.beforeEachLastPriceHandler)
                            .createCoroutineHandler { handler.handle(it) }
                            .toMarketDataProcessor()

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
                        is BlockingTradingStatusHandler -> TradingStatusStreamProcessorAdapterFactory
                            .runBeforeEachTradingStatusHandler(annotation.beforeEachTradingStatusHandler)
                            .runAfterEachTradingStatusHandler(annotation.afterEachTradingStatusHandler)
                            .createBlockingHandler { handler.handleBlocking(it) }
                            .toMarketDataProcessor()

                        is AsyncTradingStatusHandler -> TradingStatusStreamProcessorAdapterFactory
                            .runBeforeEachTradingStatusHandler(annotation.beforeEachTradingStatusHandler)
                            .runAfterEachTradingStatusHandler(annotation.afterEachTradingStatusHandler)
                            .createAsyncHandler { handler.handleAsync(it) }
                            .toMarketDataProcessor()

                        is CoroutineTradingStatusHandler -> TradingStatusStreamProcessorAdapterFactory
                            .runBeforeEachTradingStatusHandler(annotation.beforeEachTradingStatusHandler)
                            .runAfterEachTradingStatusHandler(annotation.afterEachTradingStatusHandler)
                            .createCoroutineHandler { handler.handle(it) }
                            .toMarketDataProcessor()

                        else -> throw CommonException(ErrorCode.HANDLER_NOT_FOUND)
                    }
                } else {
                    null
                }
            }

    private fun createAllTradingStatusStreamProcessorsFromType(): List<BaseMarketDataStreamProcessor> =
        applicationContext.getBeansOfType(BaseTradingStatusStreamProcessor::class.java).values.map { it.toMarketDataProcessor() }
}