package io.github.dankosik.starter.invest.configuration

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
import io.github.dankosik.starter.invest.contract.marketdata.status.AsyncTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.BaseTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.BlockingTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.CoroutineTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.AsyncTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.BaseTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.BlockingTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.CoroutineTradeHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.AsyncPortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.BasePortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.BlockingPortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.CoroutinePortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.positions.AsyncPositionHandler
import io.github.dankosik.starter.invest.contract.operation.positions.BasePositionHandler
import io.github.dankosik.starter.invest.contract.operation.positions.BlockingPositionHandler
import io.github.dankosik.starter.invest.contract.operation.positions.CoroutinePositionHandler
import io.github.dankosik.starter.invest.contract.orders.AsyncOrderHandler
import io.github.dankosik.starter.invest.contract.orders.BaseOrderHandler
import io.github.dankosik.starter.invest.contract.orders.BlockingOrderHandler
import io.github.dankosik.starter.invest.contract.orders.CoroutineOrderHandler
import io.github.dankosik.starter.invest.exception.CommonException
import io.github.dankosik.starter.invest.exception.ErrorCode
import io.github.dankosik.starter.invest.extension.awaitSingle
import io.github.dankosik.starter.invest.extension.extractAfterCandleHandlersByFigi
import io.github.dankosik.starter.invest.extension.extractAfterCandleHandlersByTicker
import io.github.dankosik.starter.invest.extension.extractAfterCandleHandlersByUid
import io.github.dankosik.starter.invest.extension.extractAfterLastPriceHandlersByFigi
import io.github.dankosik.starter.invest.extension.extractAfterLastPriceHandlersByTicker
import io.github.dankosik.starter.invest.extension.extractAfterLastPriceHandlersByUid
import io.github.dankosik.starter.invest.extension.extractAfterOrderBookHandlersByFigi
import io.github.dankosik.starter.invest.extension.extractAfterOrderBookHandlersByTicker
import io.github.dankosik.starter.invest.extension.extractAfterOrderBookHandlersByUid
import io.github.dankosik.starter.invest.extension.extractAfterTradesHandlersByFigi
import io.github.dankosik.starter.invest.extension.extractAfterTradesHandlersByTicker
import io.github.dankosik.starter.invest.extension.extractAfterTradesHandlersByUid
import io.github.dankosik.starter.invest.extension.extractAfterTradingStatusHandlersByFigi
import io.github.dankosik.starter.invest.extension.extractAfterTradingStatusHandlersByTicker
import io.github.dankosik.starter.invest.extension.extractAfterTradingStatusHandlersByUid
import io.github.dankosik.starter.invest.extension.extractBeforeCandleByTicker
import io.github.dankosik.starter.invest.extension.extractBeforeCandleHandlersByFigi
import io.github.dankosik.starter.invest.extension.extractBeforeCandleHandlersByUid
import io.github.dankosik.starter.invest.extension.extractBeforeLastPriceByTicker
import io.github.dankosik.starter.invest.extension.extractBeforeLastPriceHandlersByFigi
import io.github.dankosik.starter.invest.extension.extractBeforeLastPriceHandlersByUid
import io.github.dankosik.starter.invest.extension.extractBeforeOrderBookByTicker
import io.github.dankosik.starter.invest.extension.extractBeforeOrderBookHandlersByFigi
import io.github.dankosik.starter.invest.extension.extractBeforeOrderBookHandlersByUid
import io.github.dankosik.starter.invest.extension.extractBeforeTradesHandlersByFigi
import io.github.dankosik.starter.invest.extension.extractBeforeTradesHandlersByTicker
import io.github.dankosik.starter.invest.extension.extractBeforeTradesHandlersByUid
import io.github.dankosik.starter.invest.extension.extractBeforeTradingStatusByTicker
import io.github.dankosik.starter.invest.extension.extractBeforeTradingStatusHandlersByFigi
import io.github.dankosik.starter.invest.extension.extractBeforeTradingStatusHandlersByUid
import io.github.dankosik.starter.invest.extension.extractCommonAfterCandleHandlers
import io.github.dankosik.starter.invest.extension.extractCommonAfterLastPriceHandlers
import io.github.dankosik.starter.invest.extension.extractCommonAfterOrderBookHandlers
import io.github.dankosik.starter.invest.extension.extractCommonAfterTradesHandlers
import io.github.dankosik.starter.invest.extension.extractCommonAfterTradingStatusHandlers
import io.github.dankosik.starter.invest.extension.extractCommonBeforeCandleHandlers
import io.github.dankosik.starter.invest.extension.extractCommonBeforeLastPriceHandlers
import io.github.dankosik.starter.invest.extension.extractCommonBeforeOrderBookHandlers
import io.github.dankosik.starter.invest.extension.extractCommonBeforeTradesHandlers
import io.github.dankosik.starter.invest.extension.extractCommonBeforeTradingStatusHandlers
import io.github.dankosik.starter.invest.extension.extractCommonHandlers
import io.github.dankosik.starter.invest.extension.extractCommonHandlersByFigi
import io.github.dankosik.starter.invest.extension.extractCommonHandlersByTicker
import io.github.dankosik.starter.invest.extension.extractCommonHandlersByUid
import io.github.dankosik.starter.invest.processor.marketdata.AsyncCandleStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.BaseCandleStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BlockingCandleStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.CoroutineCandleStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.AsyncMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.CoroutineMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.operation.AsyncPortfolioStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.operation.AsyncPositionsStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.operation.BasePortfolioStreamProcessor
import io.github.dankosik.starter.invest.processor.operation.BasePositionsStreamProcessor
import io.github.dankosik.starter.invest.processor.operation.BlockingPortfolioStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.operation.BlockingPositionsStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.operation.CoroutinePortfolioStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.operation.CoroutinePositionsStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.operation.toHandlersByAccount
import io.github.dankosik.starter.invest.processor.order.AsyncOrdersStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.order.BaseOrdersStreamProcessor
import io.github.dankosik.starter.invest.processor.order.BlockingOrdersStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.order.CoroutineOrdersStreamProcessorAdapter
import io.github.dankosik.starter.invest.registry.marketdata.CandleHandlerRegistry
import io.github.dankosik.starter.invest.registry.marketdata.LastPriceHandlerRegistry
import io.github.dankosik.starter.invest.registry.marketdata.OrderBookHandlerRegistry
import io.github.dankosik.starter.invest.registry.marketdata.TradesHandlerRegistry
import io.github.dankosik.starter.invest.registry.marketdata.TradingStatusHandlerRegistry
import io.github.dankosik.starter.invest.registry.operation.PortfolioHandlerRegistry
import io.github.dankosik.starter.invest.registry.operation.PositionsHandlerRegistry
import io.github.dankosik.starter.invest.registry.order.OrdersHandlerRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.core.task.SimpleAsyncTaskExecutor
import ru.tinkoff.piapi.contract.v1.Candle
import ru.tinkoff.piapi.contract.v1.InstrumentStatus
import ru.tinkoff.piapi.contract.v1.LastPrice
import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import ru.tinkoff.piapi.contract.v1.OrderBook
import ru.tinkoff.piapi.contract.v1.OrderTrades
import ru.tinkoff.piapi.contract.v1.PortfolioResponse
import ru.tinkoff.piapi.contract.v1.PortfolioStreamResponse
import ru.tinkoff.piapi.contract.v1.PositionData
import ru.tinkoff.piapi.contract.v1.PositionsStreamResponse
import ru.tinkoff.piapi.contract.v1.Trade
import ru.tinkoff.piapi.contract.v1.TradesStreamResponse
import ru.tinkoff.piapi.contract.v1.TradingStatus
import ru.tinkoff.piapi.core.InstrumentsService
import ru.tinkoff.piapi.core.stream.MarketDataStreamService
import ru.tinkoff.piapi.core.stream.MarketDataSubscriptionService
import ru.tinkoff.piapi.core.stream.StreamProcessor

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RegistryAutoConfiguration::class)
class StreamProcessorsAutoConfiguration(
    private val tickerToUidMap: Map<String, String>,
    @Qualifier("baseMarketDataStreamProcessor")
    streamProcessors: List<BaseMarketDataStreamProcessor>,
    baseOrdersStreamProcessors: List<BaseOrdersStreamProcessor>,
    baseCandleStreamProcessors: List<BaseCandleStreamProcessor>,
    private val instrumentsServices: List<InstrumentsService>,
) {
    private val instrumentsService = instrumentsServices.first()

    val orderBookHandlerFunctionMap = mutableMapOf<BaseOrderBookHandler, (OrderBook) -> Unit>()
    val tradesHandlerFunctionMap = mutableMapOf<BaseTradeHandler, (Trade) -> Unit>()
    val lastPriceHandlerFunctionMap = mutableMapOf<BaseLastPriceHandler, (LastPrice) -> Unit>()
    val tradingStatusHandlerFunctionMap = mutableMapOf<BaseTradingStatusHandler, (TradingStatus) -> Unit>()
    val candleHandlerFunctionMap = mutableMapOf<BaseCandleHandler, (Candle) -> Unit>()
    val portfolioHandlerFunctionMap = mutableMapOf<BasePortfolioHandler, (PortfolioResponse) -> Unit>()
    val positionsHandlerFunctionMap = mutableMapOf<BasePositionHandler, (PositionData) -> Unit>()
    val ordersHandlerFunctionMap = mutableMapOf<BaseOrderHandler, (OrderTrades) -> Unit>()
    val baseMarketDataStreamProcessorFunctionMap =
        mutableMapOf<BaseMarketDataStreamProcessor, (MarketDataResponse) -> Unit>()
    val baseCandleStreamProcessorFunctionMap =
        mutableMapOf<BaseCandleStreamProcessor, (Candle) -> Unit>()
    val baseCustomPortfolioStreamProcessorFunctionMap =
        mutableMapOf<BasePortfolioStreamProcessor, (PortfolioStreamResponse) -> Unit>()
    val basePositionsStreamProcessorFunctionMap =
        mutableMapOf<BasePositionsStreamProcessor, (PositionsStreamResponse) -> Unit>()
    val baseOrdersStreamProcessorFunctionMap =
        mutableMapOf<BaseOrdersStreamProcessor, (TradesStreamResponse) -> Unit>()

    val newTickerToUidMap = tickerToUidMap.toMutableMap()

    init {
        runBlocking {
            streamProcessors.filter { it.tickers.isNotEmpty() }
                .map { it.tickers }
                .map { it.toTypedArray() }
                .toTypedArray()
                .flatten()
                .forEach { ticker ->
                    launch {
                        if (newTickerToUidMap[ticker] == null) {
                            val uId = getUidByTicker(ticker)
                            newTickerToUidMap[ticker] = uId
                        }
                    }
                }

            baseOrdersStreamProcessors.filter { it.tickers.isNotEmpty() }
                .map { it.tickers }
                .map { it.toTypedArray() }
                .toTypedArray()
                .flatten()
                .forEach { ticker ->
                    launch {
                        if (newTickerToUidMap[ticker] == null) {
                            val uId = getUidByTicker(ticker)
                            newTickerToUidMap[ticker] = uId
                        }
                    }
                }
            baseCandleStreamProcessors.filter { it.tickers.isNotEmpty() }
                .map { it.tickers }
                .map { it.toTypedArray() }
                .toTypedArray()
                .flatten()
                .forEach { ticker ->
                    launch {
                        if (newTickerToUidMap[ticker] == null) {
                            val uId = getUidByTicker(ticker)
                            newTickerToUidMap[ticker] = uId
                        }
                    }
                }
        }
    }

    @Autowired(required = false)
    @Qualifier("executor")
    private val executor: SimpleAsyncTaskExecutor? = null

    @Bean
    fun commonMarketDataStreamProcessor(
        streamProcessors: List<StreamProcessor<MarketDataResponse>>
    ): StreamProcessor<MarketDataResponse> = StreamProcessor<MarketDataResponse> { response ->
        streamProcessors.forEach { it.process(response) }
    }

    @Bean
    @ConditionalOnBean(name = ["marketDataStreamService"])
    @DependsOn(value = ["commonMarketDataStreamProcessor"])
    fun commonMarketDataSubscription(
        @Qualifier("marketDataStreamService")
        marketDataStreamService: MarketDataStreamService,
        @Qualifier("commonMarketDataStreamProcessor")
        commonMarketDataStreamProcessor: StreamProcessor<MarketDataResponse>
    ): MarketDataSubscriptionService? = marketDataStreamService.newStream(
        "commonMarketDataSubscription",
        commonMarketDataStreamProcessor,
        null
    )

    @Bean("commonMarketDataSubscription")
    @ConditionalOnMissingBean(name = ["commonMarketDataSubscription"])
    @ConditionalOnBean(name = ["marketDataStreamServiceReadonly"])
    @DependsOn(value = ["commonMarketDataStreamProcessor"])
    fun commonMarketDataSubscriptionReadonly(
        @Qualifier("marketDataStreamServiceReadonly")
        marketDataStreamServiceReadonly: MarketDataStreamService,
        @Qualifier("commonMarketDataStreamProcessor")
        commonMarketDataStreamProcessor: StreamProcessor<MarketDataResponse>
    ): MarketDataSubscriptionService? = marketDataStreamServiceReadonly.newStream(
        "commonDataSubscriptionReadonly",
        commonMarketDataStreamProcessor,
        null
    )

    @Bean
    @ConditionalOnBean(name = ["marketDataStreamServiceSandbox"])
    @DependsOn(value = ["commonMarketDataStreamProcessor"])
    fun commonMarketDataSubscriptionSandbox(
        @Qualifier("marketDataStreamServiceSandbox")
        marketDataStreamServiceSandbox: MarketDataStreamService,
        @Qualifier("commonMarketDataStreamProcessor")
        commonMarketDataStreamProcessor: StreamProcessor<MarketDataResponse>
    ): MarketDataSubscriptionService? = marketDataStreamServiceSandbox.newStream(
        "commonDataSubscriptionServiceSandbox",
        commonMarketDataStreamProcessor,
        null
    )

    @Bean
    internal fun tradesStreamProcessor(
        tradesHandlerRegistry: TradesHandlerRegistry,
        @Qualifier("baseMarketDataStreamProcessor")
        streamProcessors: List<BaseMarketDataStreamProcessor>
    ): StreamProcessor<MarketDataResponse> = when {
        streamProcessors.isEmpty() -> {
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasTrade()) {
                    val trade = response.trade
                    val handlers = tradesHandlerRegistry.getHandlers(trade)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleTrade(trade)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handleTrade(trade)
                            }
                        }
                    }
                }
            }
        }

        else -> {
            val commonTradesHandlers = streamProcessors.extractCommonHandlers()
            val commonTradesHandlersByTicker = streamProcessors.extractCommonHandlersByTicker(newTickerToUidMap)
            val commonTradesHandlersByFigi = streamProcessors.extractCommonHandlersByFigi()
            val commonTradesHandlersByUid = streamProcessors.extractCommonHandlersByUid()
            val commonBeforeTradesHandlers = streamProcessors.extractCommonBeforeTradesHandlers()
            val commonAfterTradesHandlers = streamProcessors.extractCommonAfterTradesHandlers()

            val beforeHandlersByTicker = streamProcessors.extractBeforeTradesHandlersByTicker(newTickerToUidMap)
            val beforeHandlersByUid = streamProcessors.extractBeforeTradesHandlersByUid()
            val beforeHandlersByFigi = streamProcessors.extractBeforeTradesHandlersByFigi()

            val afterHandlersByTicker = streamProcessors.extractAfterTradesHandlersByTicker(newTickerToUidMap)
            val afterHandlersByFigi = streamProcessors.extractAfterTradesHandlersByFigi()
            val afterHandlersByUid = streamProcessors.extractAfterTradesHandlersByUid()

            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasTrade()) {
                    commonBeforeTradesHandlers?.runProcessors(response)
                    val trade = response.trade
                    beforeHandlersByTicker?.get(trade.instrumentUid)?.runProcessors(response)
                    beforeHandlersByUid?.get(trade.instrumentUid)?.runProcessors(response)
                    beforeHandlersByFigi?.get(trade.figi)?.runProcessors(response)
                    val handlers = tradesHandlerRegistry.getHandlers(trade)
                    if (!commonTradesHandlers.isNullOrEmpty()) {
                        DEFAULT_SCOPE.launch {
                            commonTradesHandlers.runProcessors(response)
                        }
                    }
                    commonTradesHandlersByTicker?.get(trade.instrumentUid)?.runProcessors(response)
                    commonTradesHandlersByUid?.get(trade.instrumentUid)?.runProcessors(response)
                    commonTradesHandlersByFigi?.get(trade.figi)?.runProcessors(response)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleTrade(trade)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handleTrade(trade)
                            }
                        }
                    }
                    commonAfterTradesHandlers?.runProcessors(response)
                    afterHandlersByTicker?.get(trade.instrumentUid)?.runProcessors(response)
                    afterHandlersByFigi?.get(trade.figi)?.runProcessors(response)
                    afterHandlersByUid?.get(trade.instrumentUid)?.runProcessors(response)
                }
            }
        }
    }

    @Bean
    internal fun orderBookStreamProcessor(
        orderBookHandlerRegistry: OrderBookHandlerRegistry,
        @Qualifier("baseMarketDataStreamProcessor")
        streamProcessors: List<BaseMarketDataStreamProcessor>
    ): StreamProcessor<MarketDataResponse> = when {
        streamProcessors.isEmpty() -> {
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasOrderbook()) {
                    val orderbook = response.orderbook
                    val handlers = orderBookHandlerRegistry.getHandlers(orderbook)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleOrderBook(orderbook)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handleOrderBook(orderbook)
                            }
                        }
                    }
                }
            }
        }

        else -> {
            val commonHandlers = streamProcessors.extractCommonHandlers()
            val commonTradesHandlersByTicker = streamProcessors.extractCommonHandlersByTicker(newTickerToUidMap)
            val commonTradesHandlersByFigi = streamProcessors.extractCommonHandlersByFigi()
            val commonTradesHandlersByUid = streamProcessors.extractCommonHandlersByUid()
            val commonBeforeOrderBookHandlers = streamProcessors.extractCommonBeforeOrderBookHandlers()
            val commonAfterOrderBookHandlers = streamProcessors.extractCommonAfterOrderBookHandlers()

            val beforeHandlersByTicker = streamProcessors.extractBeforeOrderBookByTicker(newTickerToUidMap)
            val afterHandlersByTicker = streamProcessors.extractAfterOrderBookHandlersByTicker(newTickerToUidMap)
            val beforeHandlersByFigi = streamProcessors.extractBeforeOrderBookHandlersByFigi()
            val afterHandlersByFigi = streamProcessors.extractAfterOrderBookHandlersByFigi()
            val beforeHandlersByUid = streamProcessors.extractBeforeOrderBookHandlersByUid()
            val afterHandlersByUid = streamProcessors.extractAfterOrderBookHandlersByUid()
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasOrderbook()) {
                    commonBeforeOrderBookHandlers?.runProcessors(response)
                    val orderbook = response.orderbook
                    beforeHandlersByTicker?.get(orderbook.instrumentUid)?.runProcessors(response)
                    beforeHandlersByUid?.get(orderbook.instrumentUid)?.runProcessors(response)
                    beforeHandlersByFigi?.get(orderbook.figi)?.runProcessors(response)
                    val handlers = orderBookHandlerRegistry.getHandlers(orderbook)
                    commonTradesHandlersByTicker?.get(orderbook.instrumentUid)?.runProcessors(response)
                    commonTradesHandlersByUid?.get(orderbook.instrumentUid)?.runProcessors(response)
                    commonTradesHandlersByFigi?.get(orderbook.figi)?.runProcessors(response)
                    if (!commonHandlers.isNullOrEmpty()) {
                        DEFAULT_SCOPE.launch {
                            commonHandlers.runProcessors(response)
                        }
                    }
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleOrderBook(orderbook)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handleOrderBook(orderbook)
                            }
                        }
                    }
                    commonAfterOrderBookHandlers?.runProcessors(response)
                    afterHandlersByTicker?.get(orderbook.instrumentUid)?.runProcessors(response)
                    afterHandlersByFigi?.get(orderbook.figi)?.runProcessors(response)
                    afterHandlersByUid?.get(orderbook.instrumentUid)?.runProcessors(response)
                }
            }
        }
    }

    @Bean
    internal fun lastPriceStreamProcessor(
        lastPriceHandlerRegistry: LastPriceHandlerRegistry,
        @Qualifier("baseMarketDataStreamProcessor")
        streamProcessors: List<BaseMarketDataStreamProcessor>
    ): StreamProcessor<MarketDataResponse> = when {
        streamProcessors.isEmpty() -> {
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasLastPrice()) {
                    val lastPrice = response.lastPrice
                    val handlers = lastPriceHandlerRegistry.getHandlers(lastPrice)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleLastPrice(lastPrice)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handleLastPrice(lastPrice)
                            }
                        }
                    }
                }
            }
        }

        else -> {
            val commonHandlers = streamProcessors.extractCommonHandlers()
            val commonBeforeLastPriceHandlers = streamProcessors.extractCommonBeforeLastPriceHandlers()
            val commonAfterLastPriceHandlers = streamProcessors.extractCommonAfterLastPriceHandlers()
            val commonTradesHandlersByTicker = streamProcessors.extractCommonHandlersByTicker(newTickerToUidMap)
            val commonTradesHandlersByFigi = streamProcessors.extractCommonHandlersByFigi()
            val commonTradesHandlersByUid = streamProcessors.extractCommonHandlersByUid()

            val beforeHandlersByTicker = streamProcessors.extractBeforeLastPriceByTicker(newTickerToUidMap)
            val afterHandlersByTicker = streamProcessors.extractAfterLastPriceHandlersByTicker(newTickerToUidMap)
            val beforeHandlersByFigi = streamProcessors.extractBeforeLastPriceHandlersByFigi()
            val afterHandlersByFigi = streamProcessors.extractAfterLastPriceHandlersByFigi()
            val beforeHandlersByUid = streamProcessors.extractBeforeLastPriceHandlersByUid()
            val afterHandlersByUid = streamProcessors.extractAfterLastPriceHandlersByUid()
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasLastPrice()) {
                    commonBeforeLastPriceHandlers?.runProcessors(response)
                    val lastPrice = response.lastPrice
                    beforeHandlersByTicker?.get(lastPrice.instrumentUid)?.runProcessors(response)
                    beforeHandlersByUid?.get(lastPrice.instrumentUid)?.runProcessors(response)
                    beforeHandlersByFigi?.get(lastPrice.figi)?.runProcessors(response)
                    val handlers = lastPriceHandlerRegistry.getHandlers(lastPrice)
                    commonTradesHandlersByTicker?.get(lastPrice.instrumentUid)?.runProcessors(response)
                    commonTradesHandlersByUid?.get(lastPrice.instrumentUid)?.runProcessors(response)
                    commonTradesHandlersByFigi?.get(lastPrice.figi)?.runProcessors(response)
                    if (!commonHandlers.isNullOrEmpty()) {
                        DEFAULT_SCOPE.launch {
                            commonHandlers.runProcessors(response)
                        }
                    }
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleLastPrice(lastPrice)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handleLastPrice(lastPrice)
                            }
                        }
                    }
                    commonAfterLastPriceHandlers?.runProcessors(response)
                    afterHandlersByTicker?.get(lastPrice.instrumentUid)?.runProcessors(response)
                    afterHandlersByFigi?.get(lastPrice.figi)?.runProcessors(response)
                    afterHandlersByUid?.get(lastPrice.instrumentUid)?.runProcessors(response)
                }

            }
        }
    }

    @Bean
    internal fun tradingStatusStreamProcessor(
        tradingStatusHandlerRegistry: TradingStatusHandlerRegistry,
        @Qualifier("baseMarketDataStreamProcessor")
        streamProcessors: List<BaseMarketDataStreamProcessor>
    ): StreamProcessor<MarketDataResponse> = when {
        streamProcessors.isEmpty() -> {
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasTradingStatus()) {
                    val tradingStatus = response.tradingStatus
                    val handlers = tradingStatusHandlerRegistry.getHandlers(tradingStatus)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleTradingStatus(tradingStatus)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handleTradingStatus(tradingStatus)
                            }
                        }
                    }
                }
            }
        }

        else -> {
            val commonHandlers = streamProcessors.extractCommonHandlers()
            val commonBeforeTradingStatusHandlers = streamProcessors.extractCommonBeforeTradingStatusHandlers()
            val commonAfterTradingStatusHandlers = streamProcessors.extractCommonAfterTradingStatusHandlers()
            val commonTradesHandlersByTicker = streamProcessors.extractCommonHandlersByTicker(newTickerToUidMap)
            val commonTradesHandlersByFigi = streamProcessors.extractCommonHandlersByFigi()
            val commonTradesHandlersByUid = streamProcessors.extractCommonHandlersByUid()

            val beforeHandlersByTicker = streamProcessors.extractBeforeTradingStatusByTicker(newTickerToUidMap)
            val afterHandlersByTicker = streamProcessors.extractAfterTradingStatusHandlersByTicker(newTickerToUidMap)
            val beforeHandlersByFigi = streamProcessors.extractBeforeTradingStatusHandlersByFigi()
            val afterHandlersByFigi = streamProcessors.extractAfterTradingStatusHandlersByFigi()
            val beforeHandlersByUid = streamProcessors.extractBeforeTradingStatusHandlersByUid()
            val afterHandlersByUid = streamProcessors.extractAfterTradingStatusHandlersByUid()
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasTradingStatus()) {
                    commonBeforeTradingStatusHandlers?.runProcessors(response)
                    val tradingStatus = response.tradingStatus
                    beforeHandlersByTicker?.get(tradingStatus.instrumentUid)?.runProcessors(response)
                    beforeHandlersByUid?.get(tradingStatus.instrumentUid)?.runProcessors(response)
                    beforeHandlersByFigi?.get(tradingStatus.figi)?.runProcessors(response)
                    val handlers = tradingStatusHandlerRegistry.getHandlers(tradingStatus)
                    commonTradesHandlersByTicker?.get(tradingStatus.instrumentUid)?.runProcessors(response)
                    commonTradesHandlersByUid?.get(tradingStatus.instrumentUid)?.runProcessors(response)
                    commonTradesHandlersByFigi?.get(tradingStatus.figi)?.runProcessors(response)
                    if (!commonHandlers.isNullOrEmpty()) {
                        DEFAULT_SCOPE.launch {
                            commonHandlers.runProcessors(response)
                        }
                    }
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleTradingStatus(tradingStatus)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handleTradingStatus(tradingStatus)
                            }
                        }
                    }
                    commonAfterTradingStatusHandlers?.runProcessors(response)
                    afterHandlersByTicker?.get(tradingStatus.instrumentUid)?.runProcessors(response)
                    afterHandlersByFigi?.get(tradingStatus.figi)?.runProcessors(response)
                    afterHandlersByUid?.get(tradingStatus.instrumentUid)?.runProcessors(response)
                }
            }
        }
    }

    @Bean
    internal fun candleStreamProcessor(
        candleHandlerRegistry: CandleHandlerRegistry,
        @Qualifier("baseMarketDataStreamProcessor")
        streamProcessors: List<BaseMarketDataStreamProcessor>,
        baseCandleStreamProcessors: List<BaseCandleStreamProcessor>,
    ): StreamProcessor<MarketDataResponse> = when {
        streamProcessors.isEmpty() && candleHandlerRegistry.commonHandlersBySubscriptionAfter.isEmpty() -> {
            candleHandlerRegistry.addIntervalToHandlerMap(baseCandleStreamProcessors, newTickerToUidMap)
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasCandle()) {
                    val candle = response.candle
                    val handlers = candleHandlerRegistry.getHandlers(candle)
                    val handlersFromFactory = candleHandlerRegistry.getHandlersFromFactory(candle)
                    handlersFromFactory?.filter { it.beforeEachCandleHandler }?.runProcessors(candle)
                    handlersFromFactory?.filter { !it.beforeEachCandleHandler && !it.afterEachCandleHandler }
                        ?.runProcessors(candle)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleCandle(candle)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handleCandle(candle)
                            }
                        }
                    }
                    handlersFromFactory?.filter { it.afterEachCandleHandler }?.runProcessors(candle)
                }
            }
        }

        streamProcessors.isEmpty() && candleHandlerRegistry.commonHandlersBySubscriptionAfter.isNotEmpty() -> {
            candleHandlerRegistry.addIntervalToHandlerMap(baseCandleStreamProcessors, newTickerToUidMap)
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasCandle()) {
                    val candle = response.candle
                    val handlersFromFactory = candleHandlerRegistry.getHandlersFromFactory(candle)
                    handlersFromFactory?.filter { it.beforeEachCandleHandler }?.runProcessors(candle)
                    handlersFromFactory?.filter { !it.beforeEachCandleHandler && !it.afterEachCandleHandler }
                        ?.runProcessors(candle)
                    DEFAULT_SCOPE.launch {
                        candleHandlerRegistry.getCommonHandlersBefore(candle)?.forEach {
                            it.handleCandle(candle)
                        }
                    }
                    DEFAULT_SCOPE.launch {
                        val handlers = candleHandlerRegistry.getHandlers(candle)
                        candleHandlerRegistry.getCommonHandlers(candle)?.forEach {
                            launch {
                                it.handleCandle(candle)
                            }
                        }
                        if (handlers != null && handlers.size == 1) {
                            handlers.first().handleCandle(candle)
                        } else {
                            handlers?.forEach {
                                launch {
                                    it.handleCandle(candle)
                                }
                            }
                        }
                    }
                    DEFAULT_SCOPE.launch {
                        candleHandlerRegistry.getCommonHandlersAfter(candle)?.forEach {
                            it.handleCandle(candle)
                        }
                    }
                    handlersFromFactory?.filter { it.afterEachCandleHandler }?.runProcessors(candle)
                }
            }
        }

        streamProcessors.isNotEmpty() && candleHandlerRegistry.commonHandlersBySubscriptionAfter.isNotEmpty() -> {
            candleHandlerRegistry.addIntervalToHandlerMap(baseCandleStreamProcessors, newTickerToUidMap)
            val commonHandlers = streamProcessors.extractCommonHandlers()
            val commonBeforeTradingStatusHandlers = streamProcessors.extractCommonBeforeCandleHandlers()
            val commonAfterTradingStatusHandlers = streamProcessors.extractCommonAfterCandleHandlers()
            val commonTradesHandlersByTicker = streamProcessors.extractCommonHandlersByTicker(newTickerToUidMap)
            val commonTradesHandlersByFigi = streamProcessors.extractCommonHandlersByFigi()
            val commonTradesHandlersByUid = streamProcessors.extractCommonHandlersByUid()

            val beforeHandlersByTicker = streamProcessors.extractBeforeCandleByTicker(newTickerToUidMap)
            val afterHandlersByTicker = streamProcessors.extractAfterCandleHandlersByTicker(newTickerToUidMap)
            val beforeHandlersByFigi = streamProcessors.extractBeforeCandleHandlersByFigi()
            val afterHandlersByFigi = streamProcessors.extractAfterCandleHandlersByFigi()
            val beforeHandlersByUid = streamProcessors.extractBeforeCandleHandlersByUid()
            val afterHandlersByUid = streamProcessors.extractAfterCandleHandlersByUid()
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasCandle()) {
                    commonBeforeTradingStatusHandlers?.runProcessors(response)
                    val candle = response.candle
                    val handlersFromFactory = candleHandlerRegistry.getHandlersFromFactory(candle)
                    handlersFromFactory?.filter { it.beforeEachCandleHandler }?.runProcessors(candle)
                    beforeHandlersByTicker?.get(candle.instrumentUid)?.runProcessors(response)
                    beforeHandlersByUid?.get(candle.instrumentUid)?.runProcessors(response)
                    beforeHandlersByFigi?.get(candle.figi)?.runProcessors(response)
                    handlersFromFactory?.filter { !it.beforeEachCandleHandler && !it.afterEachCandleHandler }
                        ?.runProcessors(candle)
                    if (!commonHandlers.isNullOrEmpty()) {
                        DEFAULT_SCOPE.launch {
                            commonHandlers.runProcessors(response)
                        }
                    }
                    commonTradesHandlersByTicker?.get(candle.instrumentUid)?.runProcessors(response)
                    commonTradesHandlersByUid?.get(candle.instrumentUid)?.runProcessors(response)
                    commonTradesHandlersByFigi?.get(candle.figi)?.runProcessors(response)
                    DEFAULT_SCOPE.launch {
                        val handlers = candleHandlerRegistry.getHandlers(candle)
                        if (handlers != null && handlers.size == 1) {
                            handlers.first().handleCandle(candle)
                        } else {
                            handlers?.forEach {
                                launch {
                                    it.handleCandle(candle)
                                }
                            }
                        }
                    }
                    DEFAULT_SCOPE.launch {
                        candleHandlerRegistry.getCommonHandlersAfter(candle)?.forEach {
                            it.handleCandle(candle)
                        }
                    }
                    handlersFromFactory?.filter { it.afterEachCandleHandler }?.runProcessors(candle)
                    commonAfterTradingStatusHandlers?.runProcessors(response)
                    afterHandlersByTicker?.get(candle.instrumentUid)?.runProcessors(response)
                    afterHandlersByFigi?.get(candle.figi)?.runProcessors(response)
                    afterHandlersByUid?.get(candle.instrumentUid)?.runProcessors(response)
                }
            }
        }

        else -> {
            candleHandlerRegistry.addIntervalToHandlerMap(baseCandleStreamProcessors, newTickerToUidMap)
            val commonHandlers = streamProcessors.extractCommonHandlers()
            val commonBeforeTradingStatusHandlers = streamProcessors.extractCommonBeforeCandleHandlers()
            val commonAfterTradingStatusHandlers = streamProcessors.extractCommonAfterCandleHandlers()
            val commonTradesHandlersByTicker = streamProcessors.extractCommonHandlersByTicker(newTickerToUidMap)
            val commonTradesHandlersByFigi = streamProcessors.extractCommonHandlersByFigi()
            val commonTradesHandlersByUid = streamProcessors.extractCommonHandlersByUid()

            val beforeHandlersByTicker = streamProcessors.extractBeforeCandleByTicker(newTickerToUidMap)
            val afterHandlersByTicker = streamProcessors.extractAfterCandleHandlersByTicker(newTickerToUidMap)
            val beforeHandlersByFigi = streamProcessors.extractBeforeCandleHandlersByFigi()
            val afterHandlersByFigi = streamProcessors.extractAfterCandleHandlersByFigi()
            val beforeHandlersByUid = streamProcessors.extractBeforeCandleHandlersByUid()
            val afterHandlersByUid = streamProcessors.extractAfterCandleHandlersByUid()
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasCandle()) {
                    commonBeforeTradingStatusHandlers?.runProcessors(response)
                    val candle = response.candle
                    val handlersFromFactory = candleHandlerRegistry.getHandlersFromFactory(candle)
                    handlersFromFactory?.filter { it.beforeEachCandleHandler }?.runProcessors(candle)
                    beforeHandlersByTicker?.get(candle.instrumentUid)?.runProcessors(response)
                    beforeHandlersByUid?.get(candle.instrumentUid)?.runProcessors(response)
                    beforeHandlersByFigi?.get(candle.figi)?.runProcessors(response)
                    handlersFromFactory?.filter { !it.beforeEachCandleHandler && !it.afterEachCandleHandler }
                        ?.runProcessors(candle)
                    val handlers = candleHandlerRegistry.getHandlers(candle)
                    commonTradesHandlersByTicker?.get(candle.instrumentUid)?.runProcessors(response)
                    commonTradesHandlersByUid?.get(candle.instrumentUid)?.runProcessors(response)
                    commonTradesHandlersByFigi?.get(candle.figi)?.runProcessors(response)
                    if (!commonHandlers.isNullOrEmpty()) {
                        DEFAULT_SCOPE.launch {
                            commonHandlers.runProcessors(response)
                        }
                    }
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleCandle(candle)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handleCandle(candle)
                            }
                        }
                    }
                    commonAfterTradingStatusHandlers?.runProcessors(response)
                    handlersFromFactory?.filter { it.afterEachCandleHandler }?.runProcessors(candle)
                    afterHandlersByTicker?.get(candle.instrumentUid)?.runProcessors(response)
                    afterHandlersByFigi?.get(candle.figi)?.runProcessors(response)
                    afterHandlersByUid?.get(candle.instrumentUid)?.runProcessors(response)
                }
            }
        }
    }

    @Bean
    internal fun portfolioStreamProcessor(
        portfolioHandlerRegistry: PortfolioHandlerRegistry,
        streamProcessors: List<BasePortfolioStreamProcessor>
    ): StreamProcessor<PortfolioStreamResponse> = when {
        streamProcessors.isEmpty() && portfolioHandlerRegistry.commonHandlersByAccount.isEmpty() -> {
            StreamProcessor<PortfolioStreamResponse> { response ->
                if (response.hasPortfolio()) {
                    val portfolio = response.portfolio
                    val handlers = portfolioHandlerRegistry.getHandlersByAccountId(portfolio.accountId)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handlePortfolio(portfolio)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handlePortfolio(portfolio)
                            }
                        }
                    }
                }
            }
        }

        streamProcessors.isEmpty() && portfolioHandlerRegistry.commonHandlersByAccount.isNotEmpty() -> {
            StreamProcessor<PortfolioStreamResponse> { response ->
                if (response.hasPortfolio()) {
                    val portfolio = response.portfolio
                    DEFAULT_SCOPE.launch {
                        val handlers = portfolioHandlerRegistry.getHandlersByAccountId(portfolio.accountId)
                        if (handlers != null && handlers.size == 1) {
                            handlers.first().handlePortfolio(portfolio)
                        } else {
                            handlers?.forEach {
                                launch {
                                    it.handlePortfolio(portfolio)
                                }
                            }
                        }
                    }
                    DEFAULT_SCOPE.launch {
                        portfolioHandlerRegistry.getCommonHandlersByAccountId(portfolio.accountId)?.forEach {
                            launch {
                                it.handlePortfolio(portfolio)
                            }
                        }
                    }
                }
            }
        }

        streamProcessors.isNotEmpty() && portfolioHandlerRegistry.commonHandlersByAccount.isNotEmpty() -> {
            val beforePortfolioHandlers =
                streamProcessors.filter { it.beforeEachPortfolioHandler }.takeIf { it.isNotEmpty() }
                    ?.toHandlersByAccount()
            val afterPortfolioHandlers =
                streamProcessors.filter { it.afterEachPortfolioHandler }.takeIf { it.isNotEmpty() }
                    ?.toHandlersByAccount()

            val commonPortfolioHandlers =
                streamProcessors.filter { !it.beforeEachPortfolioHandler && !it.afterEachPortfolioHandler }
                    .takeIf { it.isNotEmpty() }?.toHandlersByAccount()
            StreamProcessor<PortfolioStreamResponse> { response ->
                if (response.hasPortfolio()) {
                    val portfolio = response.portfolio
                    beforePortfolioHandlers?.get(portfolio.accountId)?.runProcessors(response)
                    DEFAULT_SCOPE.launch {
                        val handlers = portfolioHandlerRegistry.getHandlersByAccountId(portfolio.accountId)
                        commonPortfolioHandlers?.get(portfolio.accountId)?.runProcessors(response)
                        if (handlers != null && handlers.size == 1) {
                            handlers.first().handlePortfolio(portfolio)
                        } else {
                            handlers?.forEach {
                                launch {
                                    it.handlePortfolio(portfolio)
                                }
                            }
                        }
                    }
                    DEFAULT_SCOPE.launch {
                        portfolioHandlerRegistry.getCommonHandlersByAccountId(portfolio.accountId)?.forEach {
                            launch {
                                it.handlePortfolio(portfolio)
                            }
                        }
                    }
                    afterPortfolioHandlers?.get(portfolio.accountId)?.runProcessors(response)
                }
            }
        }

        else -> {
            val beforePortfolioHandlers =
                streamProcessors.filter { it.beforeEachPortfolioHandler }.takeIf { it.isNotEmpty() }
                    ?.toHandlersByAccount()
            val afterPortfolioHandlers =
                streamProcessors.filter { it.afterEachPortfolioHandler }.takeIf { it.isNotEmpty() }
                    ?.toHandlersByAccount()

            val commonPortfolioHandlers =
                streamProcessors.filter { !it.beforeEachPortfolioHandler && !it.afterEachPortfolioHandler }
                    .takeIf { it.isNotEmpty() }?.toHandlersByAccount()
            StreamProcessor<PortfolioStreamResponse> { response ->
                if (response.hasPortfolio()) {
                    val portfolio = response.portfolio
                    beforePortfolioHandlers?.get(portfolio.accountId)?.runProcessors(response)
                    val handlers = portfolioHandlerRegistry.getHandlersByAccountId(portfolio.accountId)
                    commonPortfolioHandlers?.get(portfolio.accountId)?.runProcessors(response)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handlePortfolio(portfolio)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handlePortfolio(portfolio)
                            }
                        }
                    }
                    afterPortfolioHandlers?.get(portfolio.accountId)?.runProcessors(response)
                }
            }
        }
    }

    @Bean
    internal fun positionsStreamProcessor(
        positionsHandlerRegistry: PositionsHandlerRegistry,
        streamProcessors: List<BasePositionsStreamProcessor>
    ): StreamProcessor<PositionsStreamResponse> = when {
        streamProcessors.isEmpty() && positionsHandlerRegistry.commonHandlersByAccount.isEmpty() -> {
            StreamProcessor<PositionsStreamResponse> { response ->
                if (response.hasPosition()) {
                    val positionData = response.position
                    val handlers = positionsHandlerRegistry.getHandlersByAccountId(positionData.accountId)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handlePositions(positionData)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handlePositions(positionData)
                            }
                        }
                    }
                }
            }
        }

        streamProcessors.isEmpty() && positionsHandlerRegistry.commonHandlersByAccount.isNotEmpty() -> {
            StreamProcessor<PositionsStreamResponse> { response ->
                if (response.hasPosition()) {
                    val positionData = response.position
                    DEFAULT_SCOPE.launch {
                        val handlers = positionsHandlerRegistry.getHandlersByAccountId(positionData.accountId)
                        if (handlers != null && handlers.size == 1) {
                            handlers.first().handlePositions(positionData)
                        } else {
                            handlers?.forEach {
                                launch {
                                    it.handlePositions(positionData)
                                }
                            }
                        }
                    }
                    DEFAULT_SCOPE.launch {
                        positionsHandlerRegistry.getCommonHandlersByAccountId(positionData.accountId)?.forEach {
                            launch {
                                it.handlePositions(positionData)
                            }
                        }
                    }
                }
            }
        }

        streamProcessors.isNotEmpty() && positionsHandlerRegistry.commonHandlersByAccount.isNotEmpty() -> {
            val beforePositionsHandlers =
                streamProcessors.filter { it.beforeEachPositionHandler }.takeIf { it.isNotEmpty() }
                    ?.toHandlersByAccount()
            val afterPositionsHandlers =
                streamProcessors.filter { it.afterEachPositionHandler }.takeIf { it.isNotEmpty() }
                    ?.toHandlersByAccount()

            val commonPositionsHandlers =
                streamProcessors.filter { !it.beforeEachPositionHandler && !it.afterEachPositionHandler }
                    .takeIf { it.isNotEmpty() }?.toHandlersByAccount()
            StreamProcessor<PositionsStreamResponse> { response ->
                if (response.hasPosition()) {
                    val positionData = response.position
                    beforePositionsHandlers?.get(positionData.accountId)?.runProcessors(response)
                    DEFAULT_SCOPE.launch {
                        val handlers = positionsHandlerRegistry.getHandlersByAccountId(positionData.accountId)
                        commonPositionsHandlers?.get(positionData.accountId)?.runProcessors(response)
                        if (handlers != null && handlers.size == 1) {
                            handlers.first().handlePositions(positionData)
                        } else {
                            handlers?.forEach {
                                launch {
                                    it.handlePositions(positionData)
                                }
                            }
                        }
                    }
                    DEFAULT_SCOPE.launch {
                        positionsHandlerRegistry.getCommonHandlersByAccountId(positionData.accountId)?.forEach {
                            launch {
                                it.handlePositions(positionData)
                            }
                        }
                    }
                    afterPositionsHandlers?.get(positionData.accountId)?.runProcessors(response)
                }
            }
        }

        else -> {
            val beforePositionsHandlers =
                streamProcessors.filter { it.beforeEachPositionHandler }.takeIf { it.isNotEmpty() }
                    ?.toHandlersByAccount()
            val afterPositionsHandlers =
                streamProcessors.filter { it.afterEachPositionHandler }.takeIf { it.isNotEmpty() }
                    ?.toHandlersByAccount()

            val commonPositionsHandlers =
                streamProcessors.filter { !it.beforeEachPositionHandler && !it.afterEachPositionHandler }
                    .takeIf { it.isNotEmpty() }?.toHandlersByAccount()
            StreamProcessor<PositionsStreamResponse> { response ->
                if (response.hasPosition()) {
                    val positionData = response.position
                    beforePositionsHandlers?.get(positionData.accountId)?.runProcessors(response)
                    val handlers = positionsHandlerRegistry.getHandlersByAccountId(positionData.accountId)
                    commonPositionsHandlers?.get(positionData.accountId)?.runProcessors(response)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handlePositions(positionData)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handlePositions(positionData)
                            }
                        }
                    }
                    afterPositionsHandlers?.get(positionData.accountId)?.runProcessors(response)
                }
            }
        }
    }

    @Bean
    internal fun ordersStreamProcessor(
        ordersHandlerRegistry: OrdersHandlerRegistry,
        streamProcessors: List<BaseOrdersStreamProcessor>
    ): StreamProcessor<TradesStreamResponse> = when {
        streamProcessors.isEmpty() && ordersHandlerRegistry.commonHandlersByAccount.isEmpty() -> {
            StreamProcessor<TradesStreamResponse> { response ->
                if (response.hasOrderTrades()) {
                    val orderTrades = response.orderTrades
                    val handlers = ordersHandlerRegistry.getHandlers(orderTrades)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleOrders(orderTrades)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handleOrders(orderTrades)
                            }
                        }
                    }
                }
            }
        }

        streamProcessors.isEmpty() && ordersHandlerRegistry.commonHandlersByAccount.isNotEmpty() -> {
            StreamProcessor<TradesStreamResponse> { response ->
                if (response.hasOrderTrades()) {
                    val orderTrades = response.orderTrades
                    DEFAULT_SCOPE.launch {
                        val handlers = ordersHandlerRegistry.getHandlers(orderTrades)
                        if (handlers != null && handlers.size == 1) {
                            handlers.first().handleOrders(orderTrades)
                        } else {
                            handlers?.forEach {
                                launch {
                                    it.handleOrders(orderTrades)
                                }
                            }
                        }
                    }
                    DEFAULT_SCOPE.launch {
                        ordersHandlerRegistry.getCommonHandlersByAccountId(orderTrades)?.forEach {
                            launch {
                                it.handleOrders(orderTrades)
                            }
                        }
                    }
                }
            }
        }

        streamProcessors.isNotEmpty() && ordersHandlerRegistry.commonHandlersByAccount.isEmpty() -> {
            ordersHandlerRegistry.addIntervalToHandlerMap(streamProcessors, newTickerToUidMap)
            StreamProcessor<TradesStreamResponse> { response ->
                if (response.hasOrderTrades()) {
                    val orderTrades = response.orderTrades
                    val handlersFromFactory = ordersHandlerRegistry.getHandlersFromFactory(orderTrades)
                    handlersFromFactory?.filter { it.beforeEachOrdersHandler }?.runProcessors(response)
                    DEFAULT_SCOPE.launch {
                        val handlers = ordersHandlerRegistry.getHandlers(orderTrades)
                        handlersFromFactory?.filter { !it.beforeEachOrdersHandler && !it.afterEachOrdersHandler }
                            ?.runProcessors(response)
                        if (handlers != null && handlers.size == 1) {
                            handlers.first().handleOrders(orderTrades)
                        } else {
                            handlers?.forEach {
                                launch {
                                    it.handleOrders(orderTrades)
                                }
                            }
                        }
                    }
                    DEFAULT_SCOPE.launch {
                        ordersHandlerRegistry.getCommonHandlersByAccountId(orderTrades)?.forEach {
                            launch {
                                it.handleOrders(orderTrades)
                            }
                        }
                    }
                    handlersFromFactory?.filter { it.afterEachOrdersHandler }?.runProcessors(response)
                }
            }
        }

        else -> {
            ordersHandlerRegistry.addIntervalToHandlerMap(streamProcessors, newTickerToUidMap)
            StreamProcessor<TradesStreamResponse> { response ->
                if (response.hasOrderTrades()) {
                    val orderTrades = response.orderTrades
                    val handlersFromFactory = ordersHandlerRegistry.getHandlersFromFactory(orderTrades)
                    handlersFromFactory?.filter { it.beforeEachOrdersHandler }?.runProcessors(response)
                    DEFAULT_SCOPE.launch {
                        val handlers = ordersHandlerRegistry.getHandlers(orderTrades)
                        handlersFromFactory?.filter { !it.beforeEachOrdersHandler && !it.afterEachOrdersHandler }
                            ?.runProcessors(response)
                        if (handlers != null && handlers.size == 1) {
                            handlers.first().handleOrders(orderTrades)
                        } else {
                            handlers?.forEach {
                                DEFAULT_SCOPE.launch {
                                    it.handleOrders(orderTrades)
                                }
                            }
                        }
                    }
                    DEFAULT_SCOPE.launch {
                        ordersHandlerRegistry.getCommonHandlersByAccountId(orderTrades)?.forEach {
                            launch { it.handleOrders(orderTrades) }
                        }
                    }
                    handlersFromFactory?.filter { it.afterEachOrdersHandler }?.runProcessors(response)
                }
            }
        }
    }

    private fun BaseOrderBookHandler.handleOrderBook(orderBook: OrderBook) =
        orderBookHandlerFunctionMap[this]?.invoke(orderBook) ?: run {
            orderBookHandlerFunctionMap[this] = createFunctionForHandler().also { it.invoke(orderBook) }
        }

    private fun BaseTradeHandler.handleTrade(trade: Trade) =
        tradesHandlerFunctionMap[this]?.invoke(trade) ?: run {
            tradesHandlerFunctionMap[this] = createFunctionForHandler().also { it.invoke(trade) }
        }

    private fun BaseLastPriceHandler.handleLastPrice(lastPrice: LastPrice) =
        lastPriceHandlerFunctionMap[this]?.invoke(lastPrice) ?: run {
            lastPriceHandlerFunctionMap[this] = createFunctionForHandler().also { it.invoke(lastPrice) }
        }

    private fun BaseTradingStatusHandler.handleTradingStatus(tradingStatus: TradingStatus) =
        tradingStatusHandlerFunctionMap[this]?.invoke(tradingStatus) ?: run {
            tradingStatusHandlerFunctionMap[this] = createFunctionForHandler().also { it.invoke(tradingStatus) }
        }

    private fun BaseCandleHandler.handleCandle(candle: Candle) =
        candleHandlerFunctionMap[this]?.invoke(candle) ?: run {
            candleHandlerFunctionMap[this] = createFunctionForHandler().also { it.invoke(candle) }
        }

    private fun BasePortfolioHandler.handlePortfolio(portfolio: PortfolioResponse) =
        portfolioHandlerFunctionMap[this]?.invoke(portfolio) ?: run {
            portfolioHandlerFunctionMap[this] = createFunctionForHandler().also { it.invoke(portfolio) }
        }

    private fun BasePositionHandler.handlePositions(positionData: PositionData) =
        positionsHandlerFunctionMap[this]?.invoke(positionData) ?: run {
            positionsHandlerFunctionMap[this] = createFunctionForHandler().also { it.invoke(positionData) }
        }

    private fun BaseOrderHandler.handleOrders(orderTrades: OrderTrades) =
        ordersHandlerFunctionMap[this]?.invoke(orderTrades) ?: run {
            ordersHandlerFunctionMap[this] = createFunctionForHandler().also { it.invoke(orderTrades) }
        }

    private fun BaseCandleHandler.createFunctionForHandler(): (Candle) -> Unit = { candle ->
        when (val baseCandleHandler = this) {
            is BlockingCandleHandler -> executor?.submit {
                baseCandleHandler.handleBlocking(candle)
            } ?: BLOCKING_SCOPE.launch { baseCandleHandler.handleBlocking(candle) }

            is CoroutineCandleHandler -> DEFAULT_SCOPE.launch { baseCandleHandler.handle(candle) }
            is AsyncCandleHandler -> baseCandleHandler.handleAsync(candle)
        }
    }

    private fun BaseLastPriceHandler.createFunctionForHandler(): (LastPrice) -> Unit = { lastPrice ->
        when (val baseLastPriceHandler = this) {
            is BlockingLastPriceHandler -> executor?.submit {
                baseLastPriceHandler.handleBlocking(lastPrice)
            } ?: BLOCKING_SCOPE.launch { baseLastPriceHandler.handleBlocking(lastPrice) }

            is CoroutineLastPriceHandler -> DEFAULT_SCOPE.launch { baseLastPriceHandler.handle(lastPrice) }
            is AsyncLastPriceHandler -> baseLastPriceHandler.handleAsync(lastPrice)
        }
    }

    private fun BaseTradingStatusHandler.createFunctionForHandler(): (TradingStatus) -> Unit = { tradingStatus ->
        when (val baseTradingStatusHandler = this) {
            is BlockingTradingStatusHandler -> executor?.submit {
                baseTradingStatusHandler.handleBlocking(tradingStatus)
            } ?: BLOCKING_SCOPE.launch { baseTradingStatusHandler.handleBlocking(tradingStatus) }

            is CoroutineTradingStatusHandler -> DEFAULT_SCOPE.launch { baseTradingStatusHandler.handle(tradingStatus) }
            is AsyncTradingStatusHandler -> baseTradingStatusHandler.handleAsync(tradingStatus)
        }
    }


    private fun BaseTradeHandler.createFunctionForHandler(): (Trade) -> Unit = { trade ->
        when (val baseTradesHandler = this) {
            is BlockingTradeHandler -> executor?.submit {
                baseTradesHandler.handleBlocking(trade)
            } ?: BLOCKING_SCOPE.launch { baseTradesHandler.handleBlocking(trade) }

            is CoroutineTradeHandler -> DEFAULT_SCOPE.launch { baseTradesHandler.handle(trade) }
            is AsyncTradeHandler -> baseTradesHandler.handleAsync(trade)
        }
    }

    private fun BaseOrderBookHandler.createFunctionForHandler(): (OrderBook) -> Unit = { orderBook ->
        when (val baseOrderBookHandler = this) {
            is BlockingOrderBookHandler -> executor?.submit {
                baseOrderBookHandler.handleBlocking(orderBook)
            } ?: BLOCKING_SCOPE.launch { baseOrderBookHandler.handleBlocking(orderBook) }

            is CoroutineOrderBookHandler -> DEFAULT_SCOPE.launch { baseOrderBookHandler.handle(orderBook) }
            is AsyncOrderBookHandler -> baseOrderBookHandler.handleAsync(orderBook)
        }
    }

    private fun BasePortfolioHandler.createFunctionForHandler(): (PortfolioResponse) -> Unit = { portfolio ->
        when (val basePortfolioHandler = this) {
            is BlockingPortfolioHandler -> executor?.submit {
                basePortfolioHandler.handleBlocking(portfolio)
            } ?: BLOCKING_SCOPE.launch { basePortfolioHandler.handleBlocking(portfolio) }

            is CoroutinePortfolioHandler -> DEFAULT_SCOPE.launch { basePortfolioHandler.handle(portfolio) }
            is AsyncPortfolioHandler -> basePortfolioHandler.handleAsync(portfolio)
        }
    }

    private fun BasePositionHandler.createFunctionForHandler(): (PositionData) -> Unit = { positions ->
        when (val basePositionHandler = this) {
            is BlockingPositionHandler -> executor?.submit {
                basePositionHandler.handleBlocking(positions)
            } ?: BLOCKING_SCOPE.launch { basePositionHandler.handleBlocking(positions) }

            is CoroutinePositionHandler -> DEFAULT_SCOPE.launch { basePositionHandler.handle(positions) }
            is AsyncPositionHandler -> basePositionHandler.handleAsync(positions)
        }
    }

    private fun BaseOrderHandler.createFunctionForHandler(): (OrderTrades) -> Unit = { orders ->
        when (val baseOrderHandler = this) {
            is BlockingOrderHandler -> executor?.submit {
                baseOrderHandler.handleBlocking(orders)
            } ?: BLOCKING_SCOPE.launch { baseOrderHandler.handleBlocking(orders) }

            is CoroutineOrderHandler -> DEFAULT_SCOPE.launch { baseOrderHandler.handle(orders) }
            is AsyncOrderHandler -> baseOrderHandler.handleAsync(orders)
        }
    }

    private fun BaseCandleStreamProcessor.createFunctionForProcessor(): (Candle) -> Unit =
        { candle ->
            when (val candleStreamProcessor = this) {
                is BlockingCandleStreamProcessorAdapter -> executor?.submit {
                    candleStreamProcessor.process(candle)
                } ?: candleStreamProcessor.process(candle)

                is CoroutineCandleStreamProcessorAdapter -> {
                    DEFAULT_SCOPE.launch {
                        candleStreamProcessor.process(candle)
                    }
                }

                is AsyncCandleStreamProcessorAdapter -> candleStreamProcessor.process(candle)
            }
        }

    private fun BaseMarketDataStreamProcessor.createFunctionForProcessor(): (MarketDataResponse) -> Unit =
        { marketDataResponse ->
            when (val marketDataStreamProcessor = this) {
                is BlockingMarketDataStreamProcessorAdapter -> executor?.submit {
                    marketDataStreamProcessor.process(marketDataResponse)
                } ?: marketDataStreamProcessor.process(marketDataResponse)

                is CoroutineMarketDataStreamProcessorAdapter -> {
                    DEFAULT_SCOPE.launch {
                        marketDataStreamProcessor.process(marketDataResponse)
                    }
                }

                is AsyncMarketDataStreamProcessorAdapter -> marketDataStreamProcessor.process(marketDataResponse)
            }
        }

    private fun BasePortfolioStreamProcessor.createFunctionForProcessor(): (PortfolioStreamResponse) -> Unit =
        { portfolioResponse ->
            when (val portfolioStreamProcessor = this) {
                is BlockingPortfolioStreamProcessorAdapter -> executor?.submit {
                    portfolioStreamProcessor.process(portfolioResponse)
                } ?: portfolioStreamProcessor.process(portfolioResponse)

                is CoroutinePortfolioStreamProcessorAdapter -> {
                    DEFAULT_SCOPE.launch {
                        portfolioStreamProcessor.process(portfolioResponse)
                    }
                }

                is AsyncPortfolioStreamProcessorAdapter -> portfolioStreamProcessor.process(portfolioResponse)
            }
        }

    private fun BasePositionsStreamProcessor.createFunctionForProcessor(): (PositionsStreamResponse) -> Unit =
        { positionsStreamResponse ->
            when (val customPositionsStreamProcessor = this) {
                is BlockingPositionsStreamProcessorAdapter -> executor?.submit {
                    customPositionsStreamProcessor.process(positionsStreamResponse)
                } ?: customPositionsStreamProcessor.process(positionsStreamResponse)

                is CoroutinePositionsStreamProcessorAdapter -> {
                    DEFAULT_SCOPE.launch {
                        customPositionsStreamProcessor.process(positionsStreamResponse)
                    }
                }

                is AsyncPositionsStreamProcessorAdapter -> customPositionsStreamProcessor.process(
                    positionsStreamResponse
                )
            }
        }

    private fun BaseOrdersStreamProcessor.createFunctionForProcessor(): (TradesStreamResponse) -> Unit =
        { tradesStreamResponse ->
            when (val customPositionsStreamProcessor = this) {
                is BlockingOrdersStreamProcessorAdapter -> executor?.submit {
                    customPositionsStreamProcessor.process(tradesStreamResponse)
                } ?: customPositionsStreamProcessor.process(tradesStreamResponse)

                is CoroutineOrdersStreamProcessorAdapter -> {
                    DEFAULT_SCOPE.launch {
                        customPositionsStreamProcessor.process(tradesStreamResponse)
                    }
                }

                is AsyncOrdersStreamProcessorAdapter -> customPositionsStreamProcessor.process(
                    tradesStreamResponse
                )
            }
        }

    private fun List<BaseMarketDataStreamProcessor>.runProcessors(
        response: MarketDataResponse,
    ) = forEach { streamProcessor ->
        baseMarketDataStreamProcessorFunctionMap[streamProcessor]?.invoke(response) ?: run {
            baseMarketDataStreamProcessorFunctionMap[streamProcessor] =
                streamProcessor.createFunctionForProcessor().also { it.invoke(response) }
        }
    }

    private fun List<BaseCandleStreamProcessor>.runProcessors(
        candle: Candle,
    ) = forEach { streamProcessor ->
        baseCandleStreamProcessorFunctionMap[streamProcessor]?.invoke(candle) ?: run {
            baseCandleStreamProcessorFunctionMap[streamProcessor] =
                streamProcessor.createFunctionForProcessor().also { it.invoke(candle) }
        }
    }


    private fun List<BasePortfolioStreamProcessor>.runProcessors(
        response: PortfolioStreamResponse,
    ) = forEach { streamProcessor ->
        baseCustomPortfolioStreamProcessorFunctionMap[streamProcessor]?.invoke(response) ?: run {
            baseCustomPortfolioStreamProcessorFunctionMap[streamProcessor] =
                streamProcessor.createFunctionForProcessor().also { it.invoke(response) }
        }
    }

    private fun List<BasePositionsStreamProcessor>.runProcessors(
        response: PositionsStreamResponse,
    ) = forEach { streamProcessor ->
        basePositionsStreamProcessorFunctionMap[streamProcessor]?.invoke(response) ?: run {
            basePositionsStreamProcessorFunctionMap[streamProcessor] =
                streamProcessor.createFunctionForProcessor().also { it.invoke(response) }
        }
    }

    private fun List<BaseOrdersStreamProcessor>.runProcessors(
        response: TradesStreamResponse,
    ) = forEach { streamProcessor ->
        baseOrdersStreamProcessorFunctionMap[streamProcessor]?.invoke(response) ?: run {
            baseOrdersStreamProcessorFunctionMap[streamProcessor] =
                streamProcessor.createFunctionForProcessor().also { it.invoke(response) }
        }
    }


    private suspend fun getUidByTicker(ticker: String): String =
        instrumentsService.getFutures(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
            .find { it.ticker == ticker }?.uid
            ?: instrumentsService.getShares(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                .find { it.ticker == ticker }?.uid
            ?: instrumentsService.getCurrencies(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                .find { it.ticker == ticker }?.uid
            ?: instrumentsService.getBonds(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                .find { it.ticker == ticker }?.uid
            ?: instrumentsService.getEtfs(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                .find { it.ticker == ticker }?.uid
            ?: instrumentsService.getOptions(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                .find { it.ticker == ticker }?.uid
            ?: throw CommonException(ErrorCode.INSTRUMENT_NOT_FOUND).also { logger.error { "Instrument by ticker: $ticker is not found" } }

    private companion object : KLogging() {
        val DEFAULT_SCOPE = CoroutineScope(Dispatchers.Default)
        val BLOCKING_SCOPE = CoroutineScope(Dispatchers.IO)
    }
}