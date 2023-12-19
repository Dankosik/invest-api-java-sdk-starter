package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.exception.CommonException
import io.github.dankosik.starter.invest.exception.ErrorCode
import io.github.dankosik.starter.invest.processor.marketdata.common.AsyncMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.CoroutineMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.runAfterEachOrderBookHandler
import io.github.dankosik.starter.invest.processor.marketdata.common.runBeforeEachOrderBookHandler
import ru.tinkoff.piapi.contract.v1.OrderBook
import java.util.concurrent.CompletableFuture

interface BaseOrderBookStreamProcessor {
    var beforeEachOrderBookHandler: Boolean
    var afterEachOrderBookHandler: Boolean
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
    override var beforeEachOrderBookHandler: Boolean = false
    override var afterEachOrderBookHandler: Boolean = false
}

inline fun AsyncOrderBookStreamProcessorAdapter(
    crossinline block: (OrderBook) -> CompletableFuture<Void>
): AsyncOrderBookStreamProcessorAdapter = object : AsyncOrderBookStreamProcessorAdapter {
    override fun process(orderBook: OrderBook): CompletableFuture<Void> = block(orderBook)
    override var beforeEachOrderBookHandler: Boolean = false
    override var afterEachOrderBookHandler: Boolean = false
}

inline fun CoroutineOrderBookStreamProcessorAdapter(
    crossinline block: suspend (OrderBook) -> Unit
): CoroutineOrderBookStreamProcessorAdapter = object : CoroutineOrderBookStreamProcessorAdapter {
    override suspend fun process(orderBook: OrderBook): Unit = block(orderBook)
    override var beforeEachOrderBookHandler: Boolean = false
    override var afterEachOrderBookHandler: Boolean = false
}

fun <T : BaseOrderBookStreamProcessor> T.runBeforeEachOrderBookHandler(): T {
    this.beforeEachOrderBookHandler = true
    return this
}

fun <T : BaseOrderBookStreamProcessor> T.runAfterEachOrderBookHandler(): T {
    this.afterEachOrderBookHandler = true
    return this
}

fun BaseOrderBookStreamProcessor.toMarketDataProcessor(): BaseMarketDataStreamProcessor = when (this) {
    is BlockingOrderBookStreamProcessorAdapter -> this.toMarketDataProcessor()

    is AsyncOrderBookStreamProcessorAdapter -> this.toMarketDataProcessor()

    is CoroutineOrderBookStreamProcessorAdapter -> this.toMarketDataProcessor()

    else -> throw CommonException(ErrorCode.STREAM_PROCESSOR_ADAPTER_NOT_FOUND)
}

fun BlockingOrderBookStreamProcessorAdapter.toMarketDataProcessor(): BlockingMarketDataStreamProcessorAdapter =
    BlockingMarketDataStreamProcessorAdapter {
        if (it.hasOrderbook()) {
            process(it.orderbook)
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachOrderBookHandler) runAfterEachOrderBookHandler()
        if (this@toMarketDataProcessor.beforeEachOrderBookHandler) runBeforeEachOrderBookHandler()
    }

fun AsyncOrderBookStreamProcessorAdapter.toMarketDataProcessor() =
    AsyncMarketDataStreamProcessorAdapter {
        CompletableFuture.runAsync {
            if (it.hasOrderbook()) {
                process(it.orderbook)
            }
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachOrderBookHandler) runAfterEachOrderBookHandler()
        if (this@toMarketDataProcessor.beforeEachOrderBookHandler) runBeforeEachOrderBookHandler()
    }

fun CoroutineOrderBookStreamProcessorAdapter.toMarketDataProcessor() =
    CoroutineMarketDataStreamProcessorAdapter {
        if (it.hasOrderbook()) {
            process(it.orderbook)
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachOrderBookHandler) runAfterEachOrderBookHandler()
        if (this@toMarketDataProcessor.beforeEachOrderBookHandler) runBeforeEachOrderBookHandler()
    }