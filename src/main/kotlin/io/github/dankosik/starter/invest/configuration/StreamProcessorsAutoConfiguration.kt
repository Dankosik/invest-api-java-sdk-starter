package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.contract.AsyncMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.contract.AsyncOrdersStreamProcessorAdapter
import io.github.dankosik.starter.invest.contract.AsyncPortfolioStreamProcessorAdapter
import io.github.dankosik.starter.invest.contract.AsyncPositionsStreamProcessorAdapter
import io.github.dankosik.starter.invest.contract.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.contract.BaseOrdersStreamProcessor
import io.github.dankosik.starter.invest.contract.BasePortfolioStreamProcessor
import io.github.dankosik.starter.invest.contract.BasePositionsStreamProcessor
import io.github.dankosik.starter.invest.contract.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.contract.BlockingOrdersStreamProcessorAdapter
import io.github.dankosik.starter.invest.contract.BlockingPortfolioStreamProcessorAdapter
import io.github.dankosik.starter.invest.contract.BlockingPositionsStreamProcessorAdapter
import io.github.dankosik.starter.invest.contract.CoroutineMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.contract.CoroutineOrdersStreamProcessorAdapter
import io.github.dankosik.starter.invest.contract.CoroutinePortfolioStreamProcessorAdapter
import io.github.dankosik.starter.invest.contract.CoroutinePositionsStreamProcessorAdapter
import io.github.dankosik.starter.invest.contract.candle.AsyncCandleHandler
import io.github.dankosik.starter.invest.contract.candle.BaseCandleHandler
import io.github.dankosik.starter.invest.contract.candle.BlockingCandleHandler
import io.github.dankosik.starter.invest.contract.candle.CoroutineCandleHandler
import io.github.dankosik.starter.invest.contract.lastprice.AsyncLastPriceHandler
import io.github.dankosik.starter.invest.contract.lastprice.BaseLastPriceHandler
import io.github.dankosik.starter.invest.contract.lastprice.BlockingLastPriceHandler
import io.github.dankosik.starter.invest.contract.lastprice.CoroutineLastPriceHandler
import io.github.dankosik.starter.invest.contract.orderbook.AsyncOrderBookHandler
import io.github.dankosik.starter.invest.contract.orderbook.BaseOrderBookHandler
import io.github.dankosik.starter.invest.contract.orderbook.BlockingOrderBookHandler
import io.github.dankosik.starter.invest.contract.orderbook.CoroutineOrderBookHandler
import io.github.dankosik.starter.invest.contract.orders.AsyncOrdersHandler
import io.github.dankosik.starter.invest.contract.orders.BaseOrdersHandler
import io.github.dankosik.starter.invest.contract.orders.BlockingOrdersHandler
import io.github.dankosik.starter.invest.contract.orders.CoroutineOrdersHandler
import io.github.dankosik.starter.invest.contract.portfolio.AsyncPortfolioHandler
import io.github.dankosik.starter.invest.contract.portfolio.BasePortfolioHandler
import io.github.dankosik.starter.invest.contract.portfolio.BlockingPortfolioHandler
import io.github.dankosik.starter.invest.contract.portfolio.CoroutinePortfolioHandler
import io.github.dankosik.starter.invest.contract.positions.AsyncPositionsHandler
import io.github.dankosik.starter.invest.contract.positions.BasePositionsHandler
import io.github.dankosik.starter.invest.contract.positions.BlockingPositionsHandler
import io.github.dankosik.starter.invest.contract.positions.CoroutinePositionsHandler
import io.github.dankosik.starter.invest.contract.status.AsyncTradingStatusHandler
import io.github.dankosik.starter.invest.contract.status.BaseTradingStatusHandler
import io.github.dankosik.starter.invest.contract.status.BlockingTradingStatusHandler
import io.github.dankosik.starter.invest.contract.status.CoroutineTradingStatusHandler
import io.github.dankosik.starter.invest.contract.trade.AsyncTradesHandler
import io.github.dankosik.starter.invest.contract.trade.BaseTradesHandler
import io.github.dankosik.starter.invest.contract.trade.BlockingTradesHandler
import io.github.dankosik.starter.invest.contract.trade.CoroutineTradesHandler
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
    val tradesHandlerFunctionMap = mutableMapOf<BaseTradesHandler, (Trade) -> Unit>()
    val lastPriceHandlerFunctionMap = mutableMapOf<BaseLastPriceHandler, (LastPrice) -> Unit>()
    val tradingStatusHandlerFunctionMap = mutableMapOf<BaseTradingStatusHandler, (TradingStatus) -> Unit>()
    val candleHandlerFunctionMap = mutableMapOf<BaseCandleHandler, (Candle) -> Unit>()
    val portfolioHandlerFunctionMap = mutableMapOf<BasePortfolioHandler, (PortfolioResponse) -> Unit>()
    val positionsHandlerFunctionMap = mutableMapOf<BasePositionsHandler, (PositionData) -> Unit>()
    val ordersHandlerFunctionMap = mutableMapOf<BaseOrdersHandler, (OrderTrades) -> Unit>()
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
    fun commonDataSubscriptionServiceSandbox(
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
                    val trade = response.trade
                    tradesHandlerRegistry.getHandlerByUid(trade.instrumentUid)?.handleTrade(trade)
                        ?: tradesHandlerRegistry.getHandlerByFigi(trade.figi)?.handleTrade(trade)
                }
            }
        }

        else -> {
            val beforeTradesHandlers = streamProcessors.filter { it.beforeTradesHandlers }.takeIf { it.isNotEmpty() }
            val afterTradesHandlers = streamProcessors.filter { it.afterTradesHandlers }.takeIf { it.isNotEmpty() }
            StreamProcessor<MarketDataResponse> { response ->
                if (response.hasTrade()) {
                    beforeTradesHandlers?.runProcessors(response)
                    val trade = response.trade
                    tradesHandlerRegistry.getHandlerByUid(trade.instrumentUid)?.handleTrade(trade)
                        ?: tradesHandlerRegistry.getHandlerByFigi(trade.figi)?.handleTrade(trade)
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
                    val orderBook = response.orderbook
                    orderBookHandlerRegistry.getHandlerByUid(orderBook.instrumentUid)?.handleOrderBook(orderBook)
                        ?: orderBookHandlerRegistry.getHandlerByFigi(orderBook.figi)?.handleOrderBook(orderBook)
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
                    val orderBook = response.orderbook
                    orderBookHandlerRegistry.getHandlerByUid(orderBook.instrumentUid)?.handleOrderBook(orderBook)
                        ?: orderBookHandlerRegistry.getHandlerByFigi(orderBook.figi)?.handleOrderBook(orderBook)
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
                    val lastPrice = response.lastPrice
                    lastPriceHandlerRegistry.getHandlerByUid(lastPrice.instrumentUid)?.handleLastPrice(lastPrice)
                        ?: lastPriceHandlerRegistry.getHandlerByFigi(lastPrice.figi)?.handleLastPrice(lastPrice)
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
                    val lastPrice = response.lastPrice
                    lastPriceHandlerRegistry.getHandlerByUid(lastPrice.instrumentUid)?.handleLastPrice(lastPrice)
                        ?: lastPriceHandlerRegistry.getHandlerByFigi(lastPrice.figi)?.handleLastPrice(lastPrice)
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
                    val status = response.tradingStatus
                    tradingStatusHandlerRegistry.getHandlerByUid(status.instrumentUid)?.handleTradingStatus(status)
                        ?: tradingStatusHandlerRegistry.getHandlerByFigi(status.figi)?.handleTradingStatus(status)
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
                    val status = response.tradingStatus
                    tradingStatusHandlerRegistry.getHandlerByUid(status.instrumentUid)?.handleTradingStatus(status)
                        ?: tradingStatusHandlerRegistry.getHandlerByFigi(status.figi)?.handleTradingStatus(status)
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
                    val candle = response.candle
                    val subscriptionInterval = candle.interval
                    lastPriceHandlerRegistry.getHandlerByUidAndInterval(candle.instrumentUid, subscriptionInterval)
                        ?.handleCandle(candle)
                        ?: lastPriceHandlerRegistry.getHandlerByFigiAndInterval(candle.figi, subscriptionInterval)
                            ?.handleCandle(candle)
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
                    val candle = response.candle
                    val subscriptionInterval = candle.interval
                    lastPriceHandlerRegistry.getHandlerByUidAndInterval(
                        candle.instrumentUid,
                        subscriptionInterval
                    )?.handleCandle(candle)
                        ?: lastPriceHandlerRegistry.getHandlerByFigiAndInterval(
                            candle.figi,
                            subscriptionInterval
                        )?.handleCandle(candle)
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
        streamProcessors.isEmpty() -> {
            StreamProcessor<PortfolioStreamResponse> { response ->
                if (response.hasPortfolio()) {
                    val portfolioResponse = response.portfolio
                    portfolioHandlerRegistry.getHandlerByAccountId(portfolioResponse.accountId)
                        ?.handlePortfolio(portfolioResponse)
                        ?: portfolioHandlerRegistry.getHandlerByAccountId(portfolioResponse.accountId)
                            ?.handlePortfolio(portfolioResponse)
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
                    val portfolioResponse = response.portfolio
                    portfolioHandlerRegistry.getHandlerByAccountId(portfolioResponse.accountId)
                        ?.handlePortfolio(portfolioResponse)
                        ?: portfolioHandlerRegistry.getHandlerByAccountId(portfolioResponse.accountId)
                            ?.handlePortfolio(portfolioResponse)
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
        streamProcessors.isEmpty() -> {
            StreamProcessor<PositionsStreamResponse> { response ->
                if (response.hasPosition()) {
                    val portfolioResponse = response.position
                    positionsHandlerRegistry.getHandlerByAccountId(portfolioResponse.accountId)
                        ?.handlePositions(portfolioResponse)
                        ?: positionsHandlerRegistry.getHandlerByAccountId(portfolioResponse.accountId)
                            ?.handlePositions(portfolioResponse)
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
                    val portfolioResponse = response.position
                    positionsHandlerRegistry.getHandlerByAccountId(portfolioResponse.accountId)
                        ?.handlePositions(portfolioResponse)
                        ?: positionsHandlerRegistry.getHandlerByAccountId(portfolioResponse.accountId)
                            ?.handlePositions(portfolioResponse)
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
        streamProcessors.isEmpty() -> {
            StreamProcessor<TradesStreamResponse> { response ->
                if (response.hasOrderTrades()) {
                    val orderTradesResponse = response.orderTrades
                    ordersHandlerRegistry.getHandlerByUidAndAccountId(
                        orderTradesResponse.instrumentUid, orderTradesResponse.accountId
                    )?.handleOrders(orderTradesResponse)
                        ?: ordersHandlerRegistry.getHandlerByFigiAndAccountId(
                            orderTradesResponse.figi, orderTradesResponse.accountId
                        )?.handleOrders(orderTradesResponse)
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
                    val orderTradesResponse = response.orderTrades
                    ordersHandlerRegistry.getHandlerByUidAndAccountId(
                        orderTradesResponse.instrumentUid, orderTradesResponse.accountId
                    )?.handleOrders(orderTradesResponse)
                        ?: ordersHandlerRegistry.getHandlerByFigiAndAccountId(
                            orderTradesResponse.figi, orderTradesResponse.accountId
                        )?.handleOrders(orderTradesResponse)
                    afterLastPriceHandlers?.runProcessors(response)
                }
            }
        }
    }


    private fun BaseOrderBookHandler.handleOrderBook(
        orderBook: OrderBook
    ) = also { handler ->
        orderBookHandlerFunctionMap[handler]?.invoke(orderBook) ?: run {
            orderBookHandlerFunctionMap[handler] = createFunctionForHandler().also { it.invoke(orderBook) }
        }
    }

    private fun BaseTradesHandler.handleTrade(trade: Trade) = also { handler ->
        tradesHandlerFunctionMap[handler]?.invoke(trade) ?: run {
            tradesHandlerFunctionMap[handler] = createFunctionForHandler().also { it.invoke(trade) }
        }
    }

    private fun BaseLastPriceHandler.handleLastPrice(lastPrice: LastPrice) = also { handler ->
        lastPriceHandlerFunctionMap[handler]?.invoke(lastPrice) ?: run {
            lastPriceHandlerFunctionMap[handler] = createFunctionForHandler().also { it.invoke(lastPrice) }
        }
    }

    private fun BaseTradingStatusHandler.handleTradingStatus(tradingStatus: TradingStatus) = also { handler ->
        tradingStatusHandlerFunctionMap[handler]?.invoke(tradingStatus) ?: run {
            tradingStatusHandlerFunctionMap[handler] = createFunctionForHandler().also { it.invoke(tradingStatus) }
        }
    }

    private fun BaseCandleHandler.handleCandle(candle: Candle) = also { handler ->
        candleHandlerFunctionMap[handler]?.invoke(candle) ?: run {
            candleHandlerFunctionMap[handler] = createFunctionForHandler().also { it.invoke(candle) }
        }
    }

    private fun BasePortfolioHandler.handlePortfolio(portfolio: PortfolioResponse) = also { handler ->
        portfolioHandlerFunctionMap[handler]?.invoke(portfolio) ?: run {
            portfolioHandlerFunctionMap[handler] = createFunctionForHandler().also { it.invoke(portfolio) }
        }
    }

    private fun BasePositionsHandler.handlePositions(positionData: PositionData) = also { handler ->
        positionsHandlerFunctionMap[handler]?.invoke(positionData) ?: run {
            positionsHandlerFunctionMap[handler] = createFunctionForHandler().also { it.invoke(positionData) }
        }
    }

    private fun BaseOrdersHandler.handleOrders(orderTrades: OrderTrades) = also { handler ->
        ordersHandlerFunctionMap[handler]?.invoke(orderTrades) ?: run {
            ordersHandlerFunctionMap[handler] = createFunctionForHandler().also { it.invoke(orderTrades) }
        }
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


    private fun BaseTradesHandler.createFunctionForHandler(): (Trade) -> Unit = { trade ->
        when (val baseTradesHandler = this) {
            is BlockingTradesHandler -> executor?.submit {
                baseTradesHandler.handleBlocking(trade)
            } ?: baseTradesHandler.handleBlocking(trade)

            is CoroutineTradesHandler -> DEFAULT_SCOPE.launch { baseTradesHandler.handle(trade) }
            is AsyncTradesHandler -> baseTradesHandler.handleAsync(trade)
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

    private fun BasePositionsHandler.createFunctionForHandler(): (PositionData) -> Unit = { positions ->
        when (val baseOrderBookHandler = this) {
            is BlockingPositionsHandler -> executor?.submit {
                baseOrderBookHandler.handleBlocking(positions)
            } ?: baseOrderBookHandler.handleBlocking(positions)

            is CoroutinePositionsHandler -> DEFAULT_SCOPE.launch { baseOrderBookHandler.handle(positions) }
            is AsyncPositionsHandler -> baseOrderBookHandler.handleAsync(positions)
        }
    }

    private fun BaseOrdersHandler.createFunctionForHandler(): (OrderTrades) -> Unit = { orders ->
        when (val baseOrderBookHandler = this) {
            is BlockingOrdersHandler -> executor?.submit {
                baseOrderBookHandler.handleBlocking(orders)
            } ?: baseOrderBookHandler.handleBlocking(orders)

            is CoroutineOrdersHandler -> DEFAULT_SCOPE.launch { baseOrderBookHandler.handle(orders) }
            is AsyncOrdersHandler -> baseOrderBookHandler.handleAsync(orders)
        }
    }

    private fun BaseMarketDataStreamProcessor.createFunctionForProcessor(): (MarketDataResponse) -> Unit =
        { marketDataResponse ->
            when (val marketDataStreamProcessor = this) {
                is BlockingMarketDataStreamProcessorAdapter -> executor?.submit {
                    marketDataStreamProcessor.process(marketDataResponse)
                } ?: marketDataStreamProcessor.process(marketDataResponse)

                is CoroutineMarketDataStreamProcessorAdapter -> DEFAULT_SCOPE.launch {
                    marketDataStreamProcessor.process(marketDataResponse)
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

                is CoroutinePortfolioStreamProcessorAdapter -> DEFAULT_SCOPE.launch {
                    portfolioStreamProcessor.process(portfolioResponse)
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

                is CoroutinePositionsStreamProcessorAdapter -> DEFAULT_SCOPE.launch {
                    customPositionsStreamProcessor.process(positionsStreamResponse)
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

                is CoroutineOrdersStreamProcessorAdapter -> DEFAULT_SCOPE.launch {
                    customPositionsStreamProcessor.process(tradesStreamResponse)
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
