package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.processor.marketdata.common.AsyncMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.CoroutineMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.runAfterOrderBookHandlers
import io.github.dankosik.starter.invest.processor.marketdata.common.runBeforeOrderBookHandlers
import ru.tinkoff.piapi.contract.v1.OrderBook
import java.util.concurrent.CompletableFuture

interface BaseOrderBookStreamProcessor {
    var beforeEachOrderBookHandlers: Boolean
    var afterEachOrderBookHandlers: Boolean
}

interface BlockingOrderBookStreamProcessorAdapter : BaseOrderBookStreamProcessor {
    fun process(orderBook: OrderBook)
}

interface AsyncOrderBookStreamProcessorAdapter : BaseOrderBookStreamProcessor {
    fun process(orderBook: OrderBook): CompletableFuture<Void>
}

interface CoroutineOrderBookStreamProcessorAdapter : BaseOrderBookStreamProcessor {
    suspend fun process(orderBook: OrderBook)
}

inline fun BlockingOrderBookStreamProcessorAdapter(
    crossinline block: (OrderBook) -> Unit
): BlockingOrderBookStreamProcessorAdapter = object : BlockingOrderBookStreamProcessorAdapter {
    override fun process(orderBook: OrderBook) = block(orderBook)
    override var beforeEachOrderBookHandlers: Boolean = false
    override var afterEachOrderBookHandlers: Boolean = false
}


inline fun AsyncOrderBookStreamProcessorAdapter(
    crossinline block: (OrderBook) -> CompletableFuture<Void>
): AsyncOrderBookStreamProcessorAdapter = object : AsyncOrderBookStreamProcessorAdapter {
    override fun process(orderBook: OrderBook): CompletableFuture<Void> = block(orderBook)
    override var beforeEachOrderBookHandlers: Boolean = false
    override var afterEachOrderBookHandlers: Boolean = false
}

inline fun CoroutineOrderBookStreamProcessorAdapter(
    crossinline block: suspend (OrderBook) -> Unit
): CoroutineOrderBookStreamProcessorAdapter = object : CoroutineOrderBookStreamProcessorAdapter {
    override suspend fun process(orderBook: OrderBook): Unit = block(orderBook)
    override var beforeEachOrderBookHandlers: Boolean = false
    override var afterEachOrderBookHandlers: Boolean = false
}

fun <T : BaseOrderBookStreamProcessor> T.runBeforeEachOrderBookHandlers(): T {
    this.beforeEachOrderBookHandlers = true
    return this
}

fun <T : BaseOrderBookStreamProcessor> T.runAfterEachOrderBookHandlers(): T {
    this.afterEachOrderBookHandlers = true
    return this
}

fun BlockingOrderBookStreamProcessorAdapter.toMarketDataProcessor(): BlockingMarketDataStreamProcessorAdapter =
    BlockingMarketDataStreamProcessorAdapter {
        if (it.hasOrderbook()) {
            process(it.orderbook)
        }
    }.apply {
        if (afterEachOrderBookHandlers) runAfterOrderBookHandlers()
        if (beforeEachOrderBookHandlers) runBeforeOrderBookHandlers()
    }

fun AsyncOrderBookStreamProcessorAdapter.toMarketDataProcessor() =
    AsyncMarketDataStreamProcessorAdapter {
        CompletableFuture.runAsync {
            if (it.hasOrderbook()) {
                process(it.orderbook)
            }
        }
    }.apply {
        if (afterEachOrderBookHandlers) runAfterOrderBookHandlers()
        if (beforeEachOrderBookHandlers) runBeforeOrderBookHandlers()
    }

fun CoroutineOrderBookStreamProcessorAdapter.toMarketDataProcessor() =
    CoroutineMarketDataStreamProcessorAdapter {
        if (it.hasOrderbook()) {
            process(it.orderbook)
        }
    }.apply {
        if (afterEachOrderBookHandlers) runAfterOrderBookHandlers()
        if (beforeEachOrderBookHandlers) runBeforeOrderBookHandlers()
    }