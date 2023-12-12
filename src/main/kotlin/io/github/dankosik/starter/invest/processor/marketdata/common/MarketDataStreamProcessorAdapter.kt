package io.github.dankosik.starter.invest.processor.marketdata.common

import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import java.util.concurrent.CompletableFuture

interface BaseMarketDataStreamProcessor {
    var beforeEachOrderBookHandler: Boolean
    var afterEachOrderBookHandler: Boolean
    var beforeEachTradeHandler: Boolean
    var afterEachTradeHandler: Boolean
    var beforeEachLastPriceHandler: Boolean
    var afterEachLastPriceHandler: Boolean
    var beforeEachCandleHandler: Boolean
    var afterEachCandleHandler: Boolean
    var beforeEachTradingStatusHandler: Boolean
    var afterEachTradingStatusHandler: Boolean
}

interface BlockingMarketDataStreamProcessorAdapter : BaseMarketDataStreamProcessor {
    fun process(marketDataResponse: MarketDataResponse)
}

interface AsyncMarketDataStreamProcessorAdapter : BaseMarketDataStreamProcessor {
    fun process(marketDataResponse: MarketDataResponse): CompletableFuture<Void>
}

interface CoroutineMarketDataStreamProcessorAdapter : BaseMarketDataStreamProcessor {
    suspend fun process(marketDataResponse: MarketDataResponse)
}

inline fun BlockingMarketDataStreamProcessorAdapter(
    crossinline block: (MarketDataResponse) -> Unit
): BlockingMarketDataStreamProcessorAdapter = object : BlockingMarketDataStreamProcessorAdapter {
    override fun process(marketDataResponse: MarketDataResponse) = block(marketDataResponse)
    override var beforeEachOrderBookHandler: Boolean = false
    override var afterEachOrderBookHandler: Boolean = false
    override var beforeEachTradeHandler: Boolean = false
    override var afterEachTradeHandler: Boolean = false
    override var beforeEachLastPriceHandler: Boolean = false
    override var afterEachLastPriceHandler: Boolean = false
    override var beforeEachCandleHandler: Boolean = false
    override var afterEachCandleHandler: Boolean = false
    override var beforeEachTradingStatusHandler: Boolean = false
    override var afterEachTradingStatusHandler: Boolean = false
}


inline fun AsyncMarketDataStreamProcessorAdapter(
    crossinline block: (MarketDataResponse) -> CompletableFuture<Void>
): AsyncMarketDataStreamProcessorAdapter = object : AsyncMarketDataStreamProcessorAdapter {
    override fun process(marketDataResponse: MarketDataResponse): CompletableFuture<Void> = block(marketDataResponse)
    override var beforeEachOrderBookHandler: Boolean = false
    override var afterEachOrderBookHandler: Boolean = false
    override var beforeEachTradeHandler: Boolean = false
    override var afterEachTradeHandler: Boolean = false
    override var beforeEachLastPriceHandler: Boolean = false
    override var afterEachLastPriceHandler: Boolean = false
    override var beforeEachCandleHandler: Boolean = false
    override var afterEachCandleHandler: Boolean = false
    override var beforeEachTradingStatusHandler: Boolean = false
    override var afterEachTradingStatusHandler: Boolean = false
}

inline fun CoroutineMarketDataStreamProcessorAdapter(
    crossinline block: suspend (MarketDataResponse) -> Unit
): CoroutineMarketDataStreamProcessorAdapter = object : CoroutineMarketDataStreamProcessorAdapter {
    override suspend fun process(marketDataResponse: MarketDataResponse): Unit = block(marketDataResponse)
    override var beforeEachOrderBookHandler: Boolean = false
    override var afterEachOrderBookHandler: Boolean = false
    override var beforeEachTradeHandler: Boolean = false
    override var afterEachTradeHandler: Boolean = false
    override var beforeEachLastPriceHandler: Boolean = false
    override var afterEachLastPriceHandler: Boolean = false
    override var beforeEachCandleHandler: Boolean = false
    override var afterEachCandleHandler: Boolean = false
    override var beforeEachTradingStatusHandler: Boolean = false
    override var afterEachTradingStatusHandler: Boolean = false
}

fun <T : BaseMarketDataStreamProcessor> T.runBeforeEachLastPriceHandler(): T {
    this.beforeEachLastPriceHandler = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runAfterEachLastPriceHandler(): T {
    this.afterEachLastPriceHandler = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runBeforeEachOrderBookHandler(): T {
    this.beforeEachOrderBookHandler = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runAfterEachOrderBookHandler(): T {
    this.afterEachOrderBookHandler = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runBeforeEachTradeHandler(): T {
    this.beforeEachTradeHandler = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runAfterEachTradeHandler(): T {
    this.afterEachTradeHandler = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runBeforeEachCandleHandler(): T {
    this.beforeEachCandleHandler = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runAfterEachCandleHandler(): T {
    this.afterEachCandleHandler = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runAfterEachTradingStatusHandler(): T {
    this.afterEachTradingStatusHandler = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runBeforeEachTradingStatusHandler(): T {
    this.beforeEachTradingStatusHandler = true
    return this
}