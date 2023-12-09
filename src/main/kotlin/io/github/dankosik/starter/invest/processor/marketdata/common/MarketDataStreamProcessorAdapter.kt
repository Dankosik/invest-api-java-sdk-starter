package io.github.dankosik.starter.invest.processor.marketdata.common

import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import java.util.concurrent.CompletableFuture

interface BaseMarketDataStreamProcessor {
    var beforeOrderBookHandlers: Boolean
    var afterOrderBookHandlers: Boolean
    var beforeTradesHandlers: Boolean
    var afterTradesHandlers: Boolean
    var beforeLastPriceHandlers: Boolean
    var afterLastPriceHandlers: Boolean
    var beforeCandleHandlers: Boolean
    var afterCandleHandlers: Boolean
    var beforeTradingStatusHandlers: Boolean
    var afterTradingStatusHandlers: Boolean
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
    override var beforeOrderBookHandlers: Boolean = false
    override var afterOrderBookHandlers: Boolean = false
    override var beforeTradesHandlers: Boolean = false
    override var afterTradesHandlers: Boolean = false
    override var beforeLastPriceHandlers: Boolean = false
    override var afterLastPriceHandlers: Boolean = false
    override var beforeCandleHandlers: Boolean = false
    override var afterCandleHandlers: Boolean = false
    override var beforeTradingStatusHandlers: Boolean = false
    override var afterTradingStatusHandlers: Boolean = false
}


inline fun AsyncMarketDataStreamProcessorAdapter(
    crossinline block: (MarketDataResponse) -> CompletableFuture<Void>
): AsyncMarketDataStreamProcessorAdapter = object : AsyncMarketDataStreamProcessorAdapter {
    override fun process(marketDataResponse: MarketDataResponse): CompletableFuture<Void> = block(marketDataResponse)
    override var beforeOrderBookHandlers: Boolean = false
    override var afterOrderBookHandlers: Boolean = false
    override var beforeTradesHandlers: Boolean = false
    override var afterTradesHandlers: Boolean = false
    override var beforeLastPriceHandlers: Boolean = false
    override var afterLastPriceHandlers: Boolean = false
    override var beforeCandleHandlers: Boolean = false
    override var afterCandleHandlers: Boolean = false
    override var beforeTradingStatusHandlers: Boolean = false
    override var afterTradingStatusHandlers: Boolean = false
}

inline fun CoroutineMarketDataStreamProcessorAdapter(
    crossinline block: suspend (MarketDataResponse) -> Unit
): CoroutineMarketDataStreamProcessorAdapter = object : CoroutineMarketDataStreamProcessorAdapter {
    override suspend fun process(marketDataResponse: MarketDataResponse): Unit = block(marketDataResponse)
    override var beforeOrderBookHandlers: Boolean = false
    override var afterOrderBookHandlers: Boolean = false
    override var beforeTradesHandlers: Boolean = false
    override var afterTradesHandlers: Boolean = false
    override var beforeLastPriceHandlers: Boolean = false
    override var afterLastPriceHandlers: Boolean = false
    override var beforeCandleHandlers: Boolean = false
    override var afterCandleHandlers: Boolean = false
    override var beforeTradingStatusHandlers: Boolean = false
    override var afterTradingStatusHandlers: Boolean = false
}

fun <T : BaseMarketDataStreamProcessor> T.runBeforeLastPriceHandlers(): T {
    this.beforeLastPriceHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runAfterLastPriceHandlers(): T {
    this.afterLastPriceHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runBeforeOrderBookHandlers(): T {
    this.beforeOrderBookHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runAfterOrderBookHandlers(): T {
    this.afterOrderBookHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runBeforeTradesHandlers(): T {
    this.beforeTradesHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runAfterTradesHandlers(): T {
    this.afterTradesHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runBeforeCandleHandlers(): T {
    this.beforeCandleHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runAfterCandleHandlers(): T {
    this.afterCandleHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runAfterTradingStatusHandlers(): T {
    this.afterTradingStatusHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.runBeforeTradingStatusHandlers(): T {
    this.beforeTradingStatusHandlers = true
    return this
}