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
        streamProcessors: List<BaseMarketDataStreamProcessor>
    ): StreamProcessor<MarketDataResponse> = when {
        streamProcessors.isEmpty() -> {
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasTrade()) {
                    with(response.trade) {
                        tradesHandlerRegistry.getHandler(this)?.handleTrade(this)
                    }
                }
            }
        }

        else -> {
            val beforeTradesHandlers = streamProcessors.filter { it.beforeTradesHandlers }.takeIf { it.isNotEmpty() }
            val afterTradesHandlers = streamProcessors.filter { it.afterTradesHandlers }.takeIf { it.isNotEmpty() }
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasTrade()) {
                    beforeTradesHandlers?.runProcessors(response)
                    with(response.trade) {
                        tradesHandlerRegistry.getHandler(this)?.handleTrade(this)
                    }
                    afterTradesHandlers?.runProcessors(response)
                }

            }
        }
    }

    @Bean
    internal fun orderBookStreamProcessor(
        orderBookHandlerRegistry: OrderBookHandlerRegistry,
        streamProcessors: List<BaseMarketDataStreamProcessor>
    ): StreamProcessor<MarketDataResponse> = when {
        streamProcessors.isEmpty() -> {
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasOrderbook()) {
                    with(response.orderbook) {
                        orderBookHandlerRegistry.getHandler(this)?.handleOrderBook(this)
                    }
                }
            }
        }

        else -> {
            val beforeOrderBookHandlers =
                streamProcessors.filter { it.beforeOrderBookHandlers }.takeIf { it.isNotEmpty() }
            val afterOrderBookHandlers =
                streamProcessors.filter { it.afterOrderBookHandlers }.takeIf { it.isNotEmpty() }
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasOrderbook()) {
                    beforeOrderBookHandlers?.runProcessors(response)
                    with(response.orderbook) {
                        orderBookHandlerRegistry.getHandler(this)?.handleOrderBook(this)
                    }
                    afterOrderBookHandlers?.runProcessors(response)
                }
            }
        }
    }

    @Bean
    internal fun lastPriceStreamProcessor(
        lastPriceHandlerRegistry: LastPriceHandlerRegistry,
        streamProcessors: List<BaseMarketDataStreamProcessor>
    ): StreamProcessor<MarketDataResponse> = when {
        streamProcessors.isEmpty() -> {
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasLastPrice()) {
                    with(response.lastPrice) {
                        lastPriceHandlerRegistry.getHandler(this)?.handleLastPrice(this)
                    }
                }
            }
        }

        else -> {
            val beforeLastPriceHandlers =
                streamProcessors.filter { it.beforeLastPriceHandlers }.takeIf { it.isNotEmpty() }
            val afterLastPriceHandlers =
                streamProcessors.filter { it.afterLastPriceHandlers }.takeIf { it.isNotEmpty() }
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasOrderbook()) {
                    beforeLastPriceHandlers?.runProcessors(response)
                    with(response.lastPrice) {
                        lastPriceHandlerRegistry.getHandler(this)?.handleLastPrice(this)
                    }
                    afterLastPriceHandlers?.runProcessors(response)
                }

            }
        }
    }

    @Bean
    internal fun tradingStatusStreamProcessor(
        tradingStatusHandlerRegistry: TradingStatusHandlerRegistry,
        streamProcessors: List<BaseMarketDataStreamProcessor>
    ): StreamProcessor<MarketDataResponse> = when {
        streamProcessors.isEmpty() -> {
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasTradingStatus()) {
                    with(response.tradingStatus) {
                        tradingStatusHandlerRegistry.getHandler(this)?.handleTradingStatus(this)
                    }
                }
            }
        }

        else -> {
            val beforeLastPriceHandlers =
                streamProcessors.filter { it.beforeLastPriceHandlers }.takeIf { it.isNotEmpty() }
            val afterLastPriceHandlers =
                streamProcessors.filter { it.afterLastPriceHandlers }.takeIf { it.isNotEmpty() }
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasOrderbook()) {
                    beforeLastPriceHandlers?.runProcessors(response)
                    with(response.tradingStatus) {
                        tradingStatusHandlerRegistry.getHandler(this)?.handleTradingStatus(this)
                    }
                    afterLastPriceHandlers?.runProcessors(response)
                }
            }
        }
    }

    @Bean
    internal fun candleStreamProcessor(
        lastPriceHandlerRegistry: CandleHandlerRegistry,
        streamProcessors: List<BaseMarketDataStreamProcessor>
    ): StreamProcessor<MarketDataResponse> = when {
        streamProcessors.isEmpty() -> {
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasCandle()) {
                    with(response.candle) {
                        lastPriceHandlerRegistry.getHandler(this)?.handleCandle(this)
                    }
                }
            }
        }

        else -> {
            val beforeLastPriceHandlers =
                streamProcessors.filter { it.beforeCandleHandlers }.takeIf { it.isNotEmpty() }
            val afterLastPriceHandlers =
                streamProcessors.filter { it.afterCandleHandlers }.takeIf { it.isNotEmpty() }
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasOrderbook()) {
                    beforeLastPriceHandlers?.runProcessors(response)
                    with(response.candle) {
                        lastPriceHandlerRegistry.getHandler(this)?.handleCandle(this)
                    }
                    afterLastPriceHandlers?.runProcessors(response)
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
                    with(response.portfolio) {
                        portfolioHandlerRegistry.getHandlerByAccountId(this.accountId)?.handlePortfolio(this)
                    }
                }
            }
        }

        streamProcessors.isEmpty() && portfolioHandlerRegistry.allHandlersByAccount.isNotEmpty() -> {
            StreamProcessor<PortfolioStreamResponse> { response ->
                if (response.hasPortfolio()) {
                    val portfolio = response.portfolio
                    DEFAULT_SCOPE.launch {
                        portfolioHandlerRegistry.getHandlerByAccountId(portfolio.accountId)?.handlePortfolio(portfolio)
                    }
                    DEFAULT_SCOPE.launch {
                        portfolioHandlerRegistry.getHandlersByAccountId(portfolio.accountId)?.forEach {
                            it.handlePortfolio(portfolio)
                        }
                    }
                }
            }
        }

        streamProcessors.isNotEmpty() && portfolioHandlerRegistry.allHandlersByAccount.isNotEmpty() -> {
            val beforeLastPriceHandlers =
                streamProcessors.filter { it.beforePortfolioHandlers }.takeIf { it.isNotEmpty() }
            val afterLastPriceHandlers =
                streamProcessors.filter { it.afterPortfolioHandlers }.takeIf { it.isNotEmpty() }
            StreamProcessor<PortfolioStreamResponse> { response ->
                if (response.hasPortfolio()) {
                    beforeLastPriceHandlers?.runProcessors(response)
                    val portfolio = response.portfolio
                    DEFAULT_SCOPE.launch {
                        portfolioHandlerRegistry.getHandlerByAccountId(portfolio.accountId)?.handlePortfolio(portfolio)
                    }
                    portfolioHandlerRegistry.getHandlersByAccountId(portfolio.accountId)?.forEach {
                        it.handlePortfolio(portfolio)
                    }
                    afterLastPriceHandlers?.runProcessors(response)
                }
            }
        }

        else -> {
            val beforeLastPriceHandlers =
                streamProcessors.filter { it.beforePortfolioHandlers }.takeIf { it.isNotEmpty() }
            val afterLastPriceHandlers =
                streamProcessors.filter { it.afterPortfolioHandlers }.takeIf { it.isNotEmpty() }
            StreamProcessor<PortfolioStreamResponse> { response ->
                if (response.hasPortfolio()) {
                    beforeLastPriceHandlers?.runProcessors(response)
                    with(response.portfolio) {
                        portfolioHandlerRegistry.getHandlerByAccountId(this.accountId)?.handlePortfolio(this)
                    }
                    afterLastPriceHandlers?.runProcessors(response)
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
                    with(response.position) {
                        positionsHandlerRegistry.getHandlerByAccountId(this.accountId)?.handlePositions(this)
                    }
                }
            }
        }

        streamProcessors.isEmpty() && positionsHandlerRegistry.allHandlersByAccount.isNotEmpty() -> {
            StreamProcessor<PositionsStreamResponse> { response ->
                if (response.hasPosition()) {
                    val positionData = response.position
                    DEFAULT_SCOPE.launch {
                        positionsHandlerRegistry.getHandlerByAccountId(positionData.accountId)
                            ?.handlePositions(positionData)
                    }
                    DEFAULT_SCOPE.launch {
                        positionsHandlerRegistry.getHandlersByAccountId(positionData.accountId)?.forEach {
                            it.handlePositions(positionData)
                        }
                    }
                }
            }
        }

        streamProcessors.isNotEmpty() && positionsHandlerRegistry.allHandlersByAccount.isNotEmpty() -> {
            val beforeLastPriceHandlers =
                streamProcessors.filter { it.beforePositionsHandlers }.takeIf { it.isNotEmpty() }
            val afterLastPriceHandlers =
                streamProcessors.filter { it.afterPositionsHandlers }.takeIf { it.isNotEmpty() }
            StreamProcessor<PositionsStreamResponse> { response ->
                if (response.hasPosition()) {
                    beforeLastPriceHandlers?.runProcessors(response)
                    val positionData = response.position
                    DEFAULT_SCOPE.launch {
                        positionsHandlerRegistry.getHandlerByAccountId(positionData.accountId)
                            ?.handlePositions(positionData)
                    }
                    DEFAULT_SCOPE.launch {
                        positionsHandlerRegistry.getHandlersByAccountId(positionData.accountId)?.forEach {
                            it.handlePositions(positionData)
                        }
                    }
                    afterLastPriceHandlers?.runProcessors(response)
                }
            }
        }

        else -> {
            val beforeLastPriceHandlers =
                streamProcessors.filter { it.beforePositionsHandlers }.takeIf { it.isNotEmpty() }
            val afterLastPriceHandlers =
                streamProcessors.filter { it.afterPositionsHandlers }.takeIf { it.isNotEmpty() }
            StreamProcessor<PositionsStreamResponse> { response ->
                if (response.hasPosition()) {
                    beforeLastPriceHandlers?.runProcessors(response)
                    with(response.position) {
                        positionsHandlerRegistry.getHandlerByAccountId(this.accountId)?.handlePositions(this)
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
                    with(response.orderTrades) {
                        ordersHandlerRegistry.getHandler(this)?.handleOrders(this)
                    }
                }
            }
        }

        streamProcessors.isEmpty() && ordersHandlerRegistry.allHandlersByAccount.isNotEmpty() -> {
            StreamProcessor<TradesStreamResponse> { response ->
                if (response.hasOrderTrades()) {
                    val orderTrades = response.orderTrades
                    DEFAULT_SCOPE.launch {
                        ordersHandlerRegistry.getHandler(orderTrades)?.handleOrders(orderTrades)
                    }
                    DEFAULT_SCOPE.launch {
                        ordersHandlerRegistry.getAllHandlersByAccountId(orderTrades)?.forEach {
                            this.launch { it.handleOrders(orderTrades) }
                        }
                    }
                }
            }
        }

        streamProcessors.isNotEmpty() && ordersHandlerRegistry.allHandlersByAccount.isEmpty() -> {
            val beforeLastPriceHandlers =
                streamProcessors.filter { it.beforeOrdersHandlers }.takeIf { it.isNotEmpty() }
            val afterLastPriceHandlers =
                streamProcessors.filter { it.afterOrdersHandlers }.takeIf { it.isNotEmpty() }
            StreamProcessor<TradesStreamResponse> { response ->
                if (response.hasOrderTrades()) {
                    beforeLastPriceHandlers?.runProcessors(response)
                    with(response.orderTrades) {
                        ordersHandlerRegistry.getHandler(this)?.handleOrders(this)
                    }
                    afterLastPriceHandlers?.runProcessors(response)
                }
            }
        }

        else -> {
            val beforeLastPriceHandlers =
                streamProcessors.filter { it.beforeOrdersHandlers }.takeIf { it.isNotEmpty() }
            val afterLastPriceHandlers =
                streamProcessors.filter { it.afterOrdersHandlers }.takeIf { it.isNotEmpty() }
            StreamProcessor<TradesStreamResponse> { response ->
                if (response.hasOrderTrades()) {
                    beforeLastPriceHandlers?.runProcessors(response)
                    val orderTrades = response.orderTrades
                    DEFAULT_SCOPE.launch {
                        ordersHandlerRegistry.getHandler(orderTrades)?.handleOrders(orderTrades)
                    }
                    DEFAULT_SCOPE.launch {
                        ordersHandlerRegistry.getAllHandlersByAccountId(orderTrades)?.forEach {
                            this.launch { it.handleOrders(orderTrades) }
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
            } ?: baseCandleHandler.handleBlocking(candle)

            is CoroutineCandleHandler -> DEFAULT_SCOPE.launch { baseCandleHandler.handle(candle) }
            is AsyncCandleHandler -> baseCandleHandler.handleAsync(candle)
        }
    }

    private fun BaseLastPriceHandler.createFunctionForHandler(): (LastPrice) -> Unit = { lastPrice ->
        when (val baseLastPriceHandler = this) {
            is BlockingLastPriceHandler -> executor?.submit {
                baseLastPriceHandler.handleBlocking(lastPrice)
            } ?: baseLastPriceHandler.handleBlocking(lastPrice)

            is CoroutineLastPriceHandler -> DEFAULT_SCOPE.launch { baseLastPriceHandler.handle(lastPrice) }
            is AsyncLastPriceHandler -> baseLastPriceHandler.handleAsync(lastPrice)
        }
    }

    private fun BaseTradingStatusHandler.createFunctionForHandler(): (TradingStatus) -> Unit = { tradingStatus ->
        when (val baseLastPriceHandler = this) {
            is BlockingTradingStatusHandler -> executor?.submit {
                baseLastPriceHandler.handleBlocking(tradingStatus)
            } ?: baseLastPriceHandler.handleBlocking(tradingStatus)

            is CoroutineTradingStatusHandler -> DEFAULT_SCOPE.launch { baseLastPriceHandler.handle(tradingStatus) }
            is AsyncTradingStatusHandler -> baseLastPriceHandler.handleAsync(tradingStatus)
        }
    }


    private fun BaseTradeHandler.createFunctionForHandler(): (Trade) -> Unit = { trade ->
        when (val baseTradesHandler = this) {
            is BlockingTradeHandler -> executor?.submit {
                baseTradesHandler.handleBlocking(trade)
            } ?: baseTradesHandler.handleBlocking(trade)

            is CoroutineTradeHandler -> DEFAULT_SCOPE.launch { baseTradesHandler.handle(trade) }
            is AsyncTradeHandler -> baseTradesHandler.handleAsync(trade)
        }
    }

    private fun BaseOrderBookHandler.createFunctionForHandler(): (OrderBook) -> Unit = { orderBook ->
        when (val baseOrderBookHandler = this) {
            is BlockingOrderBookHandler -> executor?.submit {
                baseOrderBookHandler.handleBlocking(orderBook)
            } ?: baseOrderBookHandler.handleBlocking(orderBook)

            is CoroutineOrderBookHandler -> DEFAULT_SCOPE.launch { baseOrderBookHandler.handle(orderBook) }
            is AsyncOrderBookHandler -> baseOrderBookHandler.handleAsync(orderBook)
        }
    }

    private fun BasePortfolioHandler.createFunctionForHandler(): (PortfolioResponse) -> Unit = { portfolio ->
        when (val baseOrderBookHandler = this) {
            is BlockingPortfolioHandler -> executor?.submit {
                baseOrderBookHandler.handleBlocking(portfolio)
            } ?: baseOrderBookHandler.handleBlocking(portfolio)

            is CoroutinePortfolioHandler -> DEFAULT_SCOPE.launch { baseOrderBookHandler.handle(portfolio) }
            is AsyncPortfolioHandler -> baseOrderBookHandler.handleAsync(portfolio)
        }
    }

    private fun BasePositionHandler.createFunctionForHandler(): (PositionData) -> Unit = { positions ->
        when (val baseOrderBookHandler = this) {
            is BlockingPositionHandler -> executor?.submit {
                baseOrderBookHandler.handleBlocking(positions)
            } ?: baseOrderBookHandler.handleBlocking(positions)

            is CoroutinePositionHandler -> DEFAULT_SCOPE.launch { baseOrderBookHandler.handle(positions) }
            is AsyncPositionHandler -> baseOrderBookHandler.handleAsync(positions)
        }
    }

    private fun BaseOrderHandler.createFunctionForHandler(): (OrderTrades) -> Unit = { orders ->
        when (val baseOrderBookHandler = this) {
            is BlockingOrderHandler -> executor?.submit {
                baseOrderBookHandler.handleBlocking(orders)
            } ?: baseOrderBookHandler.handleBlocking(orders)

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

                is CoroutineMarketDataStreamProcessorAdapter -> suspend {
                    DEFAULT_SCOPE.async {
                        marketDataStreamProcessor.process(marketDataResponse)
                    }.await()
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

                is CoroutinePortfolioStreamProcessorAdapter -> suspend {
                    DEFAULT_SCOPE.async {
                        portfolioStreamProcessor.process(portfolioResponse)
                    }.await()
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

                is CoroutinePositionsStreamProcessorAdapter -> suspend {
                    DEFAULT_SCOPE.async {
                        customPositionsStreamProcessor.process(positionsStreamResponse)
                    }.await()
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

                is CoroutineOrdersStreamProcessorAdapter -> suspend {
                    DEFAULT_SCOPE.async {
                        customPositionsStreamProcessor.process(tradesStreamResponse)
                    }.await()
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
    }
}
