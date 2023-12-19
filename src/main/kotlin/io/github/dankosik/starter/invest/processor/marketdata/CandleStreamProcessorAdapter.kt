package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.exception.CommonException
import io.github.dankosik.starter.invest.exception.ErrorCode
import io.github.dankosik.starter.invest.processor.marketdata.common.AsyncMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.CoroutineMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.runAfterEachCandleHandler
import io.github.dankosik.starter.invest.processor.marketdata.common.runBeforeEachCandleHandler
import ru.tinkoff.piapi.contract.v1.Candle
import java.util.concurrent.CompletableFuture

interface BaseCandleStreamProcessor {
    var beforeEachCandleHandler: Boolean
    var afterEachCandleHandler: Boolean
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
    override var beforeEachCandleHandler: Boolean = false
    override var afterEachCandleHandler: Boolean = false
}

inline fun AsyncCandleStreamProcessorAdapter(
    crossinline block: (Candle) -> CompletableFuture<Void>
): AsyncCandleStreamProcessorAdapter = object : AsyncCandleStreamProcessorAdapter {
    override fun process(candle: Candle): CompletableFuture<Void> = block(candle)
    override var beforeEachCandleHandler: Boolean = false
    override var afterEachCandleHandler: Boolean = false
}

inline fun CoroutineCandleStreamProcessorAdapter(
    crossinline block: suspend (Candle) -> Unit
): CoroutineCandleStreamProcessorAdapter = object : CoroutineCandleStreamProcessorAdapter {
    override suspend fun process(candle: Candle): Unit = block(candle)
    override var beforeEachCandleHandler: Boolean = false
    override var afterEachCandleHandler: Boolean = false
}

fun <T : BaseCandleStreamProcessor> T.runBeforeEachCandleHandler(): T {
    this.beforeEachCandleHandler = true
    return this
}

fun <T : BaseCandleStreamProcessor> T.runAfterEachCandleBookHandler(): T {
    this.afterEachCandleHandler = true
    return this
}

fun BaseCandleStreamProcessor.toMarketDataProcessor(): BaseMarketDataStreamProcessor = when (this) {
    is BlockingCandleStreamProcessorAdapter -> this.toMarketDataProcessor()

    is AsyncCandleStreamProcessorAdapter -> this.toMarketDataProcessor()

    is CoroutineCandleStreamProcessorAdapter -> this.toMarketDataProcessor()

    else -> throw CommonException(ErrorCode.STREAM_PROCESSOR_ADAPTER_NOT_FOUND)
}

fun BlockingCandleStreamProcessorAdapter.toMarketDataProcessor(): BlockingMarketDataStreamProcessorAdapter =
    BlockingMarketDataStreamProcessorAdapter {
        if (it.hasCandle()) {
            process(it.candle)
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachCandleHandler) runAfterEachCandleHandler()
        if (this@toMarketDataProcessor.beforeEachCandleHandler) runBeforeEachCandleHandler()
    }

fun AsyncCandleStreamProcessorAdapter.toMarketDataProcessor() =
    AsyncMarketDataStreamProcessorAdapter {
        CompletableFuture.runAsync {
            if (it.hasCandle()) {
                process(it.candle)
            }
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachCandleHandler) runAfterEachCandleHandler()
        if (this@toMarketDataProcessor.beforeEachCandleHandler) runBeforeEachCandleHandler()
    }

fun CoroutineCandleStreamProcessorAdapter.toMarketDataProcessor() =
    CoroutineMarketDataStreamProcessorAdapter {
        if (it.hasCandle()) {
            process(it.candle)
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachCandleHandler) runAfterEachCandleHandler()
        if (this@toMarketDataProcessor.beforeEachCandleHandler) runBeforeEachCandleHandler()
    }