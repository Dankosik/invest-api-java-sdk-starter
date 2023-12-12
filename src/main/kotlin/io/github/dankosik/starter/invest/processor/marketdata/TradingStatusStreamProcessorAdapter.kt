package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.exception.CommonException
import io.github.dankosik.starter.invest.exception.ErrorCode
import io.github.dankosik.starter.invest.processor.marketdata.common.AsyncMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.CoroutineMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.runAfterEachTradingStatusHandler
import io.github.dankosik.starter.invest.processor.marketdata.common.runBeforeEachTradingStatusHandler
import ru.tinkoff.piapi.contract.v1.TradingStatus
import java.util.concurrent.CompletableFuture

interface BaseTradingStatusStreamProcessor {
    var beforeEachTradingStatusHandler: Boolean
    var afterEachTradingStatusHandler: Boolean
}

interface BlockingTradingStatusStreamProcessorAdapter : BaseTradingStatusStreamProcessor {
    fun process(tradingStatus: TradingStatus)
}

interface AsyncTradingStatusStreamProcessorAdapter : BaseTradingStatusStreamProcessor {
    fun process(tradingStatus: TradingStatus): CompletableFuture<Void>
}

interface CoroutineTradingStatusStreamProcessorAdapter : BaseTradingStatusStreamProcessor {
    suspend fun process(tradingStatus: TradingStatus)
}

inline fun BlockingTradingStatusStreamProcessorAdapter(
    crossinline block: (TradingStatus) -> Unit
): BlockingTradingStatusStreamProcessorAdapter = object : BlockingTradingStatusStreamProcessorAdapter {
    override fun process(tradingStatus: TradingStatus) = block(tradingStatus)
    override var beforeEachTradingStatusHandler: Boolean = false
    override var afterEachTradingStatusHandler: Boolean = false
}


inline fun AsyncTradingStatusStreamProcessorAdapter(
    crossinline block: (TradingStatus) -> CompletableFuture<Void>
): AsyncTradingStatusStreamProcessorAdapter = object : AsyncTradingStatusStreamProcessorAdapter {
    override fun process(tradingStatus: TradingStatus): CompletableFuture<Void> = block(tradingStatus)
    override var beforeEachTradingStatusHandler: Boolean = false
    override var afterEachTradingStatusHandler: Boolean = false
}

inline fun CoroutineTradingStatusStreamProcessorAdapter(
    crossinline block: suspend (TradingStatus) -> Unit
): CoroutineTradingStatusStreamProcessorAdapter = object : CoroutineTradingStatusStreamProcessorAdapter {
    override suspend fun process(tradingStatus: TradingStatus): Unit = block(tradingStatus)
    override var beforeEachTradingStatusHandler: Boolean = false
    override var afterEachTradingStatusHandler: Boolean = false
}

fun <T : BaseTradingStatusStreamProcessor> T.runBeforeEachTradingStatusHandler(): T {
    this.beforeEachTradingStatusHandler = true
    return this
}

fun <T : BaseTradingStatusStreamProcessor> T.runAfterEachTradingStatusHandler(): T {
    this.afterEachTradingStatusHandler = true
    return this
}

fun BaseTradingStatusStreamProcessor.toMarketDataProcessor(): BaseMarketDataStreamProcessor = when (this) {
    is BlockingTradingStatusStreamProcessorAdapter -> this.toMarketDataProcessor()

    is AsyncTradingStatusStreamProcessorAdapter -> this.toMarketDataProcessor()

    is CoroutineTradingStatusStreamProcessorAdapter -> this.toMarketDataProcessor()

    else -> {
        throw CommonException(ErrorCode.STREAM_PROCESSOR_ADAPTER_NOT_FOUND)
    }
}

fun BlockingTradingStatusStreamProcessorAdapter.toMarketDataProcessor(): BlockingMarketDataStreamProcessorAdapter =
    BlockingMarketDataStreamProcessorAdapter {
        if (it.hasTradingStatus()) {
            process(it.tradingStatus)
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachTradingStatusHandler) runAfterEachTradingStatusHandler()
        if (this@toMarketDataProcessor.beforeEachTradingStatusHandler) runBeforeEachTradingStatusHandler()
    }

fun AsyncTradingStatusStreamProcessorAdapter.toMarketDataProcessor() =
    AsyncMarketDataStreamProcessorAdapter {
        CompletableFuture.runAsync {
            if (it.hasTradingStatus()) {
                process(it.tradingStatus)
            }
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachTradingStatusHandler) runAfterEachTradingStatusHandler()
        if (this@toMarketDataProcessor.beforeEachTradingStatusHandler) runBeforeEachTradingStatusHandler()
    }

fun CoroutineTradingStatusStreamProcessorAdapter.toMarketDataProcessor() =
    CoroutineMarketDataStreamProcessorAdapter {
        if (it.hasTradingStatus()) {
            process(it.tradingStatus)
        }
    }.apply {
        if (this@toMarketDataProcessor.afterEachTradingStatusHandler) runAfterEachTradingStatusHandler()
        if (this@toMarketDataProcessor.beforeEachTradingStatusHandler) runBeforeEachTradingStatusHandler()
    }