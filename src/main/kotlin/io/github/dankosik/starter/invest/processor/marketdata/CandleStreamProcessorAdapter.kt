package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.processor.marketdata.common.AsyncMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.CoroutineMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.runAfterCandleHandlers
import io.github.dankosik.starter.invest.processor.marketdata.common.runBeforeCandleHandlers
import ru.tinkoff.piapi.contract.v1.Candle
import java.util.concurrent.CompletableFuture

interface BaseCandleStreamProcessor {
    var beforeEachCandleHandlers: Boolean
    var afterEachCandleHandlers: Boolean
}

interface BlockingCandleStreamProcessorAdapter : BaseCandleStreamProcessor {
    fun process(candle: Candle)
}

interface AsyncCandleStreamProcessorAdapter : BaseCandleStreamProcessor {
    fun process(candle: Candle): CompletableFuture<Void>
}

interface CoroutineCandleStreamProcessorAdapter : BaseCandleStreamProcessor {
    suspend fun process(candle: Candle)
}

inline fun BlockingCandleStreamProcessorAdapter(
    crossinline block: (Candle) -> Unit
): BlockingCandleStreamProcessorAdapter = object : BlockingCandleStreamProcessorAdapter {
    override fun process(candle: Candle) = block(candle)
    override var beforeEachCandleHandlers: Boolean = false
    override var afterEachCandleHandlers: Boolean = false
}


inline fun AsyncCandleStreamProcessorAdapter(
    crossinline block: (Candle) -> CompletableFuture<Void>
): AsyncCandleStreamProcessorAdapter = object : AsyncCandleStreamProcessorAdapter {
    override fun process(candle: Candle): CompletableFuture<Void> = block(candle)
    override var beforeEachCandleHandlers: Boolean = false
    override var afterEachCandleHandlers: Boolean = false
}

inline fun CoroutineCandleStreamProcessorAdapter(
    crossinline block: suspend (Candle) -> Unit
): CoroutineCandleStreamProcessorAdapter = object : CoroutineCandleStreamProcessorAdapter {
    override suspend fun process(candle: Candle): Unit = block(candle)
    override var beforeEachCandleHandlers: Boolean = false
    override var afterEachCandleHandlers: Boolean = false
}

fun <T : BaseCandleStreamProcessor> T.runBeforeEachCandleHandlers(): T {
    this.beforeEachCandleHandlers = true
    return this
}

fun <T : BaseCandleStreamProcessor> T.runAfterEachCandleBookHandlers(): T {
    this.afterEachCandleHandlers = true
    return this
}

fun BlockingCandleStreamProcessorAdapter.toMarketDataProcessor(): BlockingMarketDataStreamProcessorAdapter =
    BlockingMarketDataStreamProcessorAdapter {
        if (it.hasCandle()) {
            process(it.candle)
        }
    }.apply {
        if (afterEachCandleHandlers) runAfterCandleHandlers()
        if (beforeEachCandleHandlers) runBeforeCandleHandlers()
    }

fun AsyncCandleStreamProcessorAdapter.toMarketDataProcessor() =
    AsyncMarketDataStreamProcessorAdapter {
        CompletableFuture.runAsync {
            if (it.hasCandle()) {
                process(it.candle)
            }
        }
    }.apply {
        if (afterEachCandleHandlers) runAfterCandleHandlers()
        if (beforeEachCandleHandlers) runBeforeCandleHandlers()
    }

fun CoroutineCandleStreamProcessorAdapter.toMarketDataProcessor() =
    CoroutineMarketDataStreamProcessorAdapter {
        if (it.hasCandle()) {
            process(it.candle)
        }
    }.apply {
        if (afterEachCandleHandlers) runAfterCandleHandlers()
        if (beforeEachCandleHandlers) runBeforeCandleHandlers()
    }