package io.github.dankosik.starter.invest.contract

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

fun BlockingMarketDataStreamProcessorAdapter(
    block: (MarketDataResponse) -> Unit
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
}


fun AsyncMarketDataStreamProcessorAdapter(
    block: (MarketDataResponse) -> CompletableFuture<Void>
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
}

fun CoroutineMarketDataStreamProcessorAdapter(
    block: suspend (MarketDataResponse) -> Unit
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
}

fun <T : BaseMarketDataStreamProcessor> T.beforeLastPriceHandlers(): T {
    this.beforeLastPriceHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.afterLastPriceHandlers(): T {
    this.afterLastPriceHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.beforeOrderBookHandlers(): T {
    this.beforeOrderBookHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.afterOrderBookHandlers(): T {
    this.afterOrderBookHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.beforeTradesHandlers(): T {
    this.beforeTradesHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.afterTradesHandlers(): T {
    this.afterTradesHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.beforeCandleHandlers(): T {
    this.beforeCandleHandlers = true
    return this
}

fun <T : BaseMarketDataStreamProcessor> T.afterCandleHandlers(): T {
    this.afterCandleHandlers = true
    return this
}