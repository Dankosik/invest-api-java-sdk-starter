package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.exception.CommonException
import io.github.dankosik.starter.invest.exception.ErrorCode
import io.github.dankosik.starter.invest.processor.marketdata.common.AsyncMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.CoroutineMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.runAfterEachLastPriceHandler
import io.github.dankosik.starter.invest.processor.marketdata.common.runBeforeEachLastPriceHandler
import ru.tinkoff.piapi.contract.v1.LastPrice
import java.util.concurrent.CompletableFuture

interface BaseLastPriceStreamProcessor {
    var beforeEachLastPriceHandler: Boolean
    var afterEachLastPriceHandler: Boolean
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
    override var beforeEachLastPriceHandler: Boolean = false
    override var afterEachLastPriceHandler: Boolean = false
}


inline fun AsyncLastPriceStreamProcessorAdapter(
    crossinline block: (LastPrice) -> CompletableFuture<Void>
): AsyncLastPriceStreamProcessorAdapter = object : AsyncLastPriceStreamProcessorAdapter {
    override fun process(lastPrice: LastPrice): CompletableFuture<Void> = block(lastPrice)
    override var beforeEachLastPriceHandler: Boolean = false
    override var afterEachLastPriceHandler: Boolean = false
}

inline fun CoroutineLastPriceStreamProcessorAdapter(
    crossinline block: suspend (LastPrice) -> Unit
): CoroutineLastPriceStreamProcessorAdapter = object : CoroutineLastPriceStreamProcessorAdapter {
    override suspend fun process(lastPrice: LastPrice): Unit = block(lastPrice)
    override var beforeEachLastPriceHandler: Boolean = false
    override var afterEachLastPriceHandler: Boolean = false
}

fun <T : BaseLastPriceStreamProcessor> T.runBeforeEachLastPriceHandler(): T {
    this.beforeEachLastPriceHandler = true
    return this
}

fun <T : BaseLastPriceStreamProcessor> T.runAfterEachLastPriceBookHandler(): T {
    this.afterEachLastPriceHandler = true
    return this
}

fun BaseLastPriceStreamProcessor.toMarketDataProcessor(): BaseMarketDataStreamProcessor = when (this) {
    is BlockingLastPriceStreamProcessorAdapter -> this.toMarketDataProcessor()

    is AsyncLastPriceStreamProcessorAdapter -> this.toMarketDataProcessor()

    is CoroutineLastPriceStreamProcessorAdapter -> this.toMarketDataProcessor()

    else -> {
        throw CommonException(ErrorCode.STREAM_PROCESSOR_ADAPTER_NOT_FOUND)
    }
}

fun BlockingLastPriceStreamProcessorAdapter.toMarketDataProcessor(): BlockingMarketDataStreamProcessorAdapter =
    BlockingMarketDataStreamProcessorAdapter {
        if (it.hasLastPrice()) {
            process(it.lastPrice)
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachLastPriceHandler) runAfterEachLastPriceHandler()
        if (this@toMarketDataProcessor.beforeEachLastPriceHandler) runBeforeEachLastPriceHandler()
    }

fun AsyncLastPriceStreamProcessorAdapter.toMarketDataProcessor() =
    AsyncMarketDataStreamProcessorAdapter {
        CompletableFuture.runAsync {
            if (it.hasLastPrice()) {
                process(it.lastPrice)
            }
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachLastPriceHandler) runAfterEachLastPriceHandler()
        if (this@toMarketDataProcessor.beforeEachLastPriceHandler) runBeforeEachLastPriceHandler()
    }

fun CoroutineLastPriceStreamProcessorAdapter.toMarketDataProcessor() =
    CoroutineMarketDataStreamProcessorAdapter {
        if (it.hasLastPrice()) {
            process(it.lastPrice)
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachLastPriceHandler) runAfterEachLastPriceHandler()
        if (this@toMarketDataProcessor.beforeEachLastPriceHandler) runBeforeEachLastPriceHandler()
    }