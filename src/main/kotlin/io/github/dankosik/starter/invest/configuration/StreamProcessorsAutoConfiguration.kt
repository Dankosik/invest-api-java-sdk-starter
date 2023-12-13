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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
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
import ru.tinkoff.piapi.core.stream.MarketDataStreamService
import ru.tinkoff.piapi.core.stream.MarketDataSubscriptionService
import ru.tinkoff.piapi.core.stream.StreamProcessor

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RegistryAutoConfiguration::class)
class StreamProcessorsAutoConfiguration {

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
    val baseCustomPortfolioStreamProcessorFunctionMap =
        mutableMapOf<BasePortfolioStreamProcessor, (PortfolioStreamResponse) -> Unit>()
    val basePositionsStreamProcessorFunctionMap =
        mutableMapOf<BasePositionsStreamProcessor, (PositionsStreamResponse) -> Unit>()
    val baseOrdersStreamProcessorFunctionMap =
        mutableMapOf<BaseOrdersStreamProcessor, (TradesStreamResponse) -> Unit>()

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
            val beforeTradesHandlers = streamProcessors.filter { it.beforeEachTradeHandler }.takeIf { it.isNotEmpty() }
            val afterTradesHandlers = streamProcessors.filter { it.afterEachTradeHandler }.takeIf { it.isNotEmpty() }
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasTrade()) {
                    beforeTradesHandlers?.runProcessors(response)
                    val trade = response.trade
                    val handlers = tradesHandlerRegistry.getHandlers(trade)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleTrade(trade)
                    } else {
                        suspend {
                            handlers?.map {
                                DEFAULT_SCOPE.async {
                                    it.handleTrade(trade)
                                }
                            }?.awaitAll()
                        }
                    }
                    afterTradesHandlers?.runProcessors(response)
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
            val beforeOrderBookHandlers =
                streamProcessors.filter { it.beforeEachOrderBookHandler }.takeIf { it.isNotEmpty() }
            val afterOrderBookHandlers =
                streamProcessors.filter { it.afterEachOrderBookHandler }.takeIf { it.isNotEmpty() }
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasOrderbook()) {
                    beforeOrderBookHandlers?.runProcessors(response)
                    val orderbook = response.orderbook
                    val handlers = orderBookHandlerRegistry.getHandlers(orderbook)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleOrderBook(orderbook)
                    } else {
                        suspend {
                            handlers?.map {
                                DEFAULT_SCOPE.async {
                                    it.handleOrderBook(orderbook)
                                }
                            }?.awaitAll()
                        }
                    }
                    afterOrderBookHandlers?.runProcessors(response)
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
            val beforeLastPriceHandlers =
                streamProcessors.filter { it.beforeEachLastPriceHandler }.takeIf { it.isNotEmpty() }
            val afterLastPriceHandlers =
                streamProcessors.filter { it.afterEachLastPriceHandler }.takeIf { it.isNotEmpty() }
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasLastPrice()) {
                    beforeLastPriceHandlers?.runProcessors(response)
                    val lastPrice = response.lastPrice
                    val handlers = lastPriceHandlerRegistry.getHandlers(lastPrice)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleLastPrice(lastPrice)
                    } else {
                        suspend {
                            handlers?.map {
                                DEFAULT_SCOPE.async {
                                    it.handleLastPrice(lastPrice)
                                }
                            }?.awaitAll()
                        }
                    }
                    afterLastPriceHandlers?.runProcessors(response)
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
            val beforeTradingStatusHandlers =
                streamProcessors.filter { it.beforeEachTradingStatusHandler }.takeIf { it.isNotEmpty() }
            val afterTradingStatusHandlers =
                streamProcessors.filter { it.afterEachTradingStatusHandler }.takeIf { it.isNotEmpty() }
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasTradingStatus()) {
                    beforeTradingStatusHandlers?.runProcessors(response)
                    val tradingStatus = response.tradingStatus
                    val handlers = tradingStatusHandlerRegistry.getHandlers(tradingStatus)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleTradingStatus(tradingStatus)
                    } else {
                        suspend {
                            handlers?.map {
                                DEFAULT_SCOPE.async {
                                    it.handleTradingStatus(tradingStatus)
                                }
                            }?.awaitAll()
                        }
                    }
                    afterTradingStatusHandlers?.runProcessors(response)
                }
            }
        }
    }

    @Bean
    internal fun candleStreamProcessor(
        candleHandlerRegistry: CandleHandlerRegistry,
        @Qualifier("baseMarketDataStreamProcessor")
        streamProcessors: List<BaseMarketDataStreamProcessor>
    ): StreamProcessor<MarketDataResponse> = when {
        streamProcessors.isEmpty() && candleHandlerRegistry.allHandlersBySubscription.isEmpty() -> {
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasCandle()) {
                    val candle = response.candle
                    val handlers = candleHandlerRegistry.getHandlers(candle)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleCandle(candle)
                    } else {
                        handlers?.forEach {
                            DEFAULT_SCOPE.launch {
                                it.handleCandle(candle)
                            }
                        }
                    }
                }
            }
        }

        streamProcessors.isEmpty() && candleHandlerRegistry.allHandlersBySubscription.isNotEmpty() -> {
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasCandle()) {
                    val candle = response.candle
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
                        candleHandlerRegistry.getCommonHandlers(candle)?.forEach {
                            it.handleCandle(candle)
                        }
                    }
                }
            }
        }

        streamProcessors.isNotEmpty() && candleHandlerRegistry.allHandlersBySubscription.isNotEmpty() -> {
            val beforeCandleHandlers =
                streamProcessors.filter { it.beforeEachCandleHandler }.takeIf { it.isNotEmpty() }
            val afterCandleHandlers =
                streamProcessors.filter { it.afterEachCandleHandler }.takeIf { it.isNotEmpty() }
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasCandle()) {
                    beforeCandleHandlers?.runProcessors(response)
                    val candle = response.candle
                    DEFAULT_SCOPE.launch {
                        val handlers = candleHandlerRegistry.getHandlers(candle)
                        if (handlers != null && handlers.size == 1) {
                            handlers.first().handleCandle(candle)
                        } else {
                            suspend {
                                handlers?.map {
                                    async {
                                        it.handleCandle(candle)
                                    }
                                }?.awaitAll()
                            }
                        }
                    }
                    DEFAULT_SCOPE.launch {
                        candleHandlerRegistry.getCommonHandlers(candle)?.forEach {
                            it.handleCandle(candle)
                        }
                    }
                    afterCandleHandlers?.runProcessors(response)
                }
            }
        }

        else -> {
            val beforeCandleHandlers =
                streamProcessors.filter { it.beforeEachCandleHandler }.takeIf { it.isNotEmpty() }
            val afterCandleHandlers =
                streamProcessors.filter { it.afterEachCandleHandler }.takeIf { it.isNotEmpty() }
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasCandle()) {
                    beforeCandleHandlers?.runProcessors(response)
                    val candle = response.candle
                    val handlers = candleHandlerRegistry.getHandlers(candle)
                    if (handlers != null && handlers.size == 1) {
                        handlers.first().handleCandle(candle)
                    } else {
                        suspend {
                            handlers?.map {
                                DEFAULT_SCOPE.async {
                                    it.handleCandle(candle)
                                }
                            }?.awaitAll()
                        }
                    }
                    afterCandleHandlers?.runProcessors(response)
                }
            }
        }
    }

    @Bean
    internal fun portfolioStreamProcessor(
        portfolioHandlerRegistry: PortfolioHandlerRegistry,
        streamProcessors: List<BasePortfolioStreamProcessor>
    ): StreamProcessor<PortfolioStreamResponse> = when {
        streamProcessors.isEmpty() && portfolioHandlerRegistry.allHandlersByAccount.isEmpty() -> {
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

        streamProcessors.isEmpty() && portfolioHandlerRegistry.allHandlersByAccount.isNotEmpty() -> {
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

        streamProcessors.isNotEmpty() && portfolioHandlerRegistry.allHandlersByAccount.isNotEmpty() -> {
            val beforePortfolioHandlers =
                streamProcessors.filter { it.beforeEachPortfolioHandler }.takeIf { it.isNotEmpty() }
            val afterPortfolioHandlers =
                streamProcessors.filter { it.afterEachPortfolioHandler }.takeIf { it.isNotEmpty() }
            StreamProcessor<PortfolioStreamResponse> { response ->
                if (response.hasPortfolio()) {
                    beforePortfolioHandlers?.runProcessors(response)
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
                    afterPortfolioHandlers?.runProcessors(response)
                }
            }
        }

        else -> {
            val beforePortfolioHandlers =
                streamProcessors.filter { it.beforeEachPortfolioHandler }.takeIf { it.isNotEmpty() }
            val afterPortfolioHandlers =
                streamProcessors.filter { it.afterEachPortfolioHandler }.takeIf { it.isNotEmpty() }
            StreamProcessor<PortfolioStreamResponse> { response ->
                if (response.hasPortfolio()) {
                    beforePortfolioHandlers?.runProcessors(response)
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
                    afterPortfolioHandlers?.runProcessors(response)
                }
            }
        }
    }

    @Bean
    internal fun positionsStreamProcessor(
        positionsHandlerRegistry: PositionsHandlerRegistry,
        streamProcessors: List<BasePositionsStreamProcessor>
    ): StreamProcessor<PositionsStreamResponse> = when {
        streamProcessors.isEmpty() && positionsHandlerRegistry.allHandlersByAccount.isEmpty() -> {
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

        streamProcessors.isEmpty() && positionsHandlerRegistry.allHandlersByAccount.isNotEmpty() -> {
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

        streamProcessors.isNotEmpty() && positionsHandlerRegistry.allHandlersByAccount.isNotEmpty() -> {
            val beforeLastPriceHandlers =
                streamProcessors.filter { it.beforeEachPositionHandler }.takeIf { it.isNotEmpty() }
            val afterLastPriceHandlers =
                streamProcessors.filter { it.afterEachPositionHandler }.takeIf { it.isNotEmpty() }
            StreamProcessor<PositionsStreamResponse> { response ->
                if (response.hasPosition()) {
                    beforeLastPriceHandlers?.runProcessors(response)
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
                    afterLastPriceHandlers?.runProcessors(response)
                }
            }
        }

        else -> {
            val beforeLastPriceHandlers =
                streamProcessors.filter { it.beforeEachPositionHandler }.takeIf { it.isNotEmpty() }
            val afterLastPriceHandlers =
                streamProcessors.filter { it.afterEachPositionHandler }.takeIf { it.isNotEmpty() }
            StreamProcessor<PositionsStreamResponse> { response ->
                if (response.hasPosition()) {
                    beforeLastPriceHandlers?.runProcessors(response)
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
                    afterLastPriceHandlers?.runProcessors(response)
                }
            }
        }
    }

    @Bean
    internal fun ordersStreamProcessor(
        ordersHandlerRegistry: OrdersHandlerRegistry,
        streamProcessors: List<BaseOrdersStreamProcessor>
    ): StreamProcessor<TradesStreamResponse> = when {
        streamProcessors.isEmpty() && ordersHandlerRegistry.allHandlersByAccount.isEmpty() -> {
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

        streamProcessors.isEmpty() && ordersHandlerRegistry.allHandlersByAccount.isNotEmpty() -> {
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

        streamProcessors.isNotEmpty() && ordersHandlerRegistry.allHandlersByAccount.isEmpty() -> {
            val beforeLastPriceHandlers =
                streamProcessors.filter { it.beforeEachOrdersHandler }.takeIf { it.isNotEmpty() }
            val afterLastPriceHandlers =
                streamProcessors.filter { it.afterEachOrdersHandler }.takeIf { it.isNotEmpty() }
            StreamProcessor<TradesStreamResponse> { response ->
                if (response.hasOrderTrades()) {
                    beforeLastPriceHandlers?.runProcessors(response)
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
                    afterLastPriceHandlers?.runProcessors(response)
                }
            }
        }

        else -> {
            val beforeLastPriceHandlers =
                streamProcessors.filter { it.beforeEachOrdersHandler }.takeIf { it.isNotEmpty() }
            val afterLastPriceHandlers =
                streamProcessors.filter { it.afterEachOrdersHandler }.takeIf { it.isNotEmpty() }
            StreamProcessor<TradesStreamResponse> { response ->
                if (response.hasOrderTrades()) {
                    beforeLastPriceHandlers?.runProcessors(response)
                    val orderTrades = response.orderTrades
                    DEFAULT_SCOPE.launch {
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
                    DEFAULT_SCOPE.launch {
                        ordersHandlerRegistry.getCommonHandlersByAccountId(orderTrades)?.forEach {
                            launch { it.handleOrders(orderTrades) }
                        }
                    }
                    afterLastPriceHandlers?.runProcessors(response)
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
        when (val baseLastPriceHandler = this) {
            is BlockingTradingStatusHandler -> executor?.submit {
                baseLastPriceHandler.handleBlocking(tradingStatus)
            } ?: BLOCKING_SCOPE.launch { baseLastPriceHandler.handleBlocking(tradingStatus) }

            is CoroutineTradingStatusHandler -> DEFAULT_SCOPE.launch { baseLastPriceHandler.handle(tradingStatus) }
            is AsyncTradingStatusHandler -> baseLastPriceHandler.handleAsync(tradingStatus)
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
        when (val baseOrderBookHandler = this) {
            is BlockingPortfolioHandler -> executor?.submit {
                baseOrderBookHandler.handleBlocking(portfolio)
            } ?: BLOCKING_SCOPE.launch { baseOrderBookHandler.handleBlocking(portfolio) }

            is CoroutinePortfolioHandler -> DEFAULT_SCOPE.launch { baseOrderBookHandler.handle(portfolio) }
            is AsyncPortfolioHandler -> baseOrderBookHandler.handleAsync(portfolio)
        }
    }

    private fun BasePositionHandler.createFunctionForHandler(): (PositionData) -> Unit = { positions ->
        when (val baseOrderBookHandler = this) {
            is BlockingPositionHandler -> executor?.submit {
                baseOrderBookHandler.handleBlocking(positions)
            } ?: BLOCKING_SCOPE.launch { baseOrderBookHandler.handleBlocking(positions) }

            is CoroutinePositionHandler -> DEFAULT_SCOPE.launch { baseOrderBookHandler.handle(positions) }
            is AsyncPositionHandler -> baseOrderBookHandler.handleAsync(positions)
        }
    }

    private fun BaseOrderHandler.createFunctionForHandler(): (OrderTrades) -> Unit = { orders ->
        when (val baseOrderBookHandler = this) {
            is BlockingOrderHandler -> executor?.submit {
                baseOrderBookHandler.handleBlocking(orders)
            } ?: BLOCKING_SCOPE.launch { baseOrderBookHandler.handleBlocking(orders) }

            is CoroutineOrderHandler -> DEFAULT_SCOPE.launch { baseOrderBookHandler.handle(orders) }
            is AsyncOrderHandler -> baseOrderBookHandler.handleAsync(orders)
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

    private companion object : KLogging() {
        val DEFAULT_SCOPE = CoroutineScope(Dispatchers.Default)
        val BLOCKING_SCOPE = CoroutineScope(Dispatchers.IO)
    }
}
