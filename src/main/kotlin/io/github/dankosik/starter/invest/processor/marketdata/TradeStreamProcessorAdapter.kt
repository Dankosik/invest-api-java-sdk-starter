package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.exception.CommonException
import io.github.dankosik.starter.invest.exception.ErrorCode
import io.github.dankosik.starter.invest.processor.marketdata.common.AsyncMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.CoroutineMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.runAfterEachTradeHandler
import io.github.dankosik.starter.invest.processor.marketdata.common.runBeforeEachTradeHandler
import ru.tinkoff.piapi.contract.v1.Trade
import java.util.concurrent.CompletableFuture

interface BaseTradeStreamProcessor {
    var beforeEachTradeHandler: Boolean
    var afterEachTradeHandler: Boolean
}

interface BlockingTradeStreamProcessorAdapter : BaseTradeStreamProcessor {
    fun process(trade: Trade)
}

interface AsyncTradeStreamProcessorAdapter : BaseTradeStreamProcessor {
    fun process(trade: Trade): CompletableFuture<Void>
}

interface CoroutineTradeStreamProcessorAdapter : BaseTradeStreamProcessor {
    suspend fun process(trade: Trade)
}

inline fun BlockingTradeStreamProcessorAdapter(
    crossinline block: (Trade) -> Unit
): BlockingTradeStreamProcessorAdapter = object : BlockingTradeStreamProcessorAdapter {
    override fun process(trade: Trade) = block(trade)
    override var beforeEachTradeHandler: Boolean = false
    override var afterEachTradeHandler: Boolean = false
}


inline fun AsyncTradeStreamProcessorAdapter(
    crossinline block: (Trade) -> CompletableFuture<Void>
): AsyncTradeStreamProcessorAdapter = object : AsyncTradeStreamProcessorAdapter {
    override fun process(trade: Trade): CompletableFuture<Void> = block(trade)
    override var beforeEachTradeHandler: Boolean = false
    override var afterEachTradeHandler: Boolean = false
}

inline fun CoroutineTradeStreamProcessorAdapter(
    crossinline block: suspend (Trade) -> Unit
): CoroutineTradeStreamProcessorAdapter = object : CoroutineTradeStreamProcessorAdapter {
    override suspend fun process(trade: Trade): Unit = block(trade)
    override var beforeEachTradeHandler: Boolean = false
    override var afterEachTradeHandler: Boolean = false
}

fun <T : BaseTradeStreamProcessor> T.runBeforeEachTradeHandler(): T {
    this.beforeEachTradeHandler = true
    return this
}

fun <T : BaseTradeStreamProcessor> T.runAfterEachTradeHandler(): T {
    this.afterEachTradeHandler = true
    return this
}

fun BaseTradeStreamProcessor.toMarketDataProcessor(): BaseMarketDataStreamProcessor = when (this) {
    is BlockingTradeStreamProcessorAdapter -> this.toMarketDataProcessor()

    is AsyncTradeStreamProcessorAdapter -> this.toMarketDataProcessor()

    is CoroutineTradeStreamProcessorAdapter -> this.toMarketDataProcessor()

    else -> {
        throw CommonException(ErrorCode.STREAM_PROCESSOR_ADAPTER_NOT_FOUND)
    }
}

fun BlockingTradeStreamProcessorAdapter.toMarketDataProcessor(): BlockingMarketDataStreamProcessorAdapter =
    BlockingMarketDataStreamProcessorAdapter {
        if (it.hasTrade()) {
            process(it.trade)
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachTradeHandler) runAfterEachTradeHandler()
        if (this@toMarketDataProcessor.beforeEachTradeHandler) runBeforeEachTradeHandler()
    }

fun AsyncTradeStreamProcessorAdapter.toMarketDataProcessor() =
    AsyncMarketDataStreamProcessorAdapter {
        CompletableFuture.runAsync {
            if (it.hasTrade()) {
                process(it.trade)
            }
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachTradeHandler) runAfterEachTradeHandler()
        if (this@toMarketDataProcessor.beforeEachTradeHandler) runBeforeEachTradeHandler()
    }

fun CoroutineTradeStreamProcessorAdapter.toMarketDataProcessor() =
    CoroutineMarketDataStreamProcessorAdapter {
        if (it.hasTrade()) {
            process(it.trade)
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachTradeHandler) runAfterEachTradeHandler()
        if (this@toMarketDataProcessor.beforeEachTradeHandler) runBeforeEachTradeHandler()
    }