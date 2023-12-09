package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.processor.marketdata.common.AsyncMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.CoroutineMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.runAfterLastPriceHandlers
import io.github.dankosik.starter.invest.processor.marketdata.common.runBeforeLastPriceHandlers
import ru.tinkoff.piapi.contract.v1.LastPrice
import java.util.concurrent.CompletableFuture

interface BaseLastPriceStreamProcessor {
    var beforeEachLastPriceHandlers: Boolean
    var afterEachLastPriceHandlers: Boolean
}

interface BlockingLastPriceStreamProcessorAdapter : BaseLastPriceStreamProcessor {
    fun process(lastPrice: LastPrice)
}

interface AsyncLastPriceStreamProcessorAdapter : BaseLastPriceStreamProcessor {
    fun process(lastPrice: LastPrice): CompletableFuture<Void>
}

interface CoroutineLastPriceStreamProcessorAdapter : BaseLastPriceStreamProcessor {
    suspend fun process(lastPrice: LastPrice)
}

inline fun BlockingLastPriceStreamProcessorAdapter(
    crossinline block: (LastPrice) -> Unit
): BlockingLastPriceStreamProcessorAdapter = object : BlockingLastPriceStreamProcessorAdapter {
    override fun process(lastPrice: LastPrice) = block(lastPrice)
    override var beforeEachLastPriceHandlers: Boolean = false
    override var afterEachLastPriceHandlers: Boolean = false
}


inline fun AsyncLastPriceStreamProcessorAdapter(
    crossinline block: (LastPrice) -> CompletableFuture<Void>
): AsyncLastPriceStreamProcessorAdapter = object : AsyncLastPriceStreamProcessorAdapter {
    override fun process(lastPrice: LastPrice): CompletableFuture<Void> = block(lastPrice)
    override var beforeEachLastPriceHandlers: Boolean = false
    override var afterEachLastPriceHandlers: Boolean = false
}

inline fun CoroutineLastPriceStreamProcessorAdapter(
    crossinline block: suspend (LastPrice) -> Unit
): CoroutineLastPriceStreamProcessorAdapter = object : CoroutineLastPriceStreamProcessorAdapter {
    override suspend fun process(lastPrice: LastPrice): Unit = block(lastPrice)
    override var beforeEachLastPriceHandlers: Boolean = false
    override var afterEachLastPriceHandlers: Boolean = false
}

fun <T : BaseLastPriceStreamProcessor> T.runBeforeEachLastPriceHandlers(): T {
    this.beforeEachLastPriceHandlers = true
    return this
}

fun <T : BaseLastPriceStreamProcessor> T.runAfterEachLastPriceBookHandlers(): T {
    this.afterEachLastPriceHandlers = true
    return this
}

fun BlockingLastPriceStreamProcessorAdapter.toMarketDataProcessor(): BlockingMarketDataStreamProcessorAdapter =
    BlockingMarketDataStreamProcessorAdapter {
        if (it.hasLastPrice()) {
            process(it.lastPrice)
        }
    }.apply {
        if (afterEachLastPriceHandlers) runAfterLastPriceHandlers()
        if (beforeEachLastPriceHandlers) runBeforeLastPriceHandlers()
    }

fun AsyncLastPriceStreamProcessorAdapter.toMarketDataProcessor() =
    AsyncMarketDataStreamProcessorAdapter {
        CompletableFuture.runAsync {
            if (it.hasLastPrice()) {
                process(it.lastPrice)
            }
        }
    }.apply {
        if (afterEachLastPriceHandlers) runAfterLastPriceHandlers()
        if (beforeEachLastPriceHandlers) runBeforeLastPriceHandlers()
    }

fun CoroutineLastPriceStreamProcessorAdapter.toMarketDataProcessor() =
    CoroutineMarketDataStreamProcessorAdapter {
        if (it.hasLastPrice()) {
            process(it.lastPrice)
        }
    }.apply {
        if (afterEachLastPriceHandlers) runAfterLastPriceHandlers()
        if (beforeEachLastPriceHandlers) runBeforeLastPriceHandlers()
    }