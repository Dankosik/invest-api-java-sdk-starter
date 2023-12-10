package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.processor.marketdata.common.AsyncMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.CoroutineMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.runAfterTradesHandlers
import io.github.dankosik.starter.invest.processor.marketdata.common.runBeforeTradesHandlers
import io.github.dankosik.starter.invest.processor.marketdata.common.runBeforeTradingStatusHandlers
import ru.tinkoff.piapi.contract.v1.TradingStatus
import java.util.concurrent.CompletableFuture

interface BaseTradingStatusStreamProcessor {
    var beforeEachTradingStatusHandlers: Boolean
    var afterEachTradingStatusHandlers: Boolean
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
    override var beforeEachTradingStatusHandlers: Boolean = false
    override var afterEachTradingStatusHandlers: Boolean = false
}


inline fun AsyncTradingStatusStreamProcessorAdapter(
    crossinline block: (TradingStatus) -> CompletableFuture<Void>
): AsyncTradingStatusStreamProcessorAdapter = object : AsyncTradingStatusStreamProcessorAdapter {
    override fun process(tradingStatus: TradingStatus): CompletableFuture<Void> = block(tradingStatus)
    override var beforeEachTradingStatusHandlers: Boolean = false
    override var afterEachTradingStatusHandlers: Boolean = false
}

inline fun CoroutineTradingStatusStreamProcessorAdapter(
    crossinline block: suspend (TradingStatus) -> Unit
): CoroutineTradingStatusStreamProcessorAdapter = object : CoroutineTradingStatusStreamProcessorAdapter {
    override suspend fun process(tradingStatus: TradingStatus): Unit = block(tradingStatus)
    override var beforeEachTradingStatusHandlers: Boolean = false
    override var afterEachTradingStatusHandlers: Boolean = false
}

fun <T : BaseTradingStatusStreamProcessor> T.runBeforeEachTradingStatusHandlers(): T {
    this.beforeEachTradingStatusHandlers = true
    return this
}

fun <T : BaseTradingStatusStreamProcessor> T.runAfterEachTradingStatusHandlers(): T {
    this.afterEachTradingStatusHandlers = true
    return this
}

fun BaseTradingStatusStreamProcessor.toMarketDataProcessor(): BaseMarketDataStreamProcessor = when (this) {
    is BlockingTradingStatusStreamProcessorAdapter -> this.toMarketDataProcessor()

    is AsyncTradingStatusStreamProcessorAdapter -> this.toMarketDataProcessor()

    is CoroutineTradingStatusStreamProcessorAdapter -> this.toMarketDataProcessor()

    else -> {
        throw RuntimeException()
    }
}

fun BlockingTradingStatusStreamProcessorAdapter.toMarketDataProcessor(): BlockingMarketDataStreamProcessorAdapter =
    BlockingMarketDataStreamProcessorAdapter {
        if (it.hasTradingStatus()) {
            process(it.tradingStatus)
        }
    }.apply {
        if (afterEachTradingStatusHandlers) runAfterEachTradingStatusHandlers()
        if (beforeEachTradingStatusHandlers) runBeforeTradingStatusHandlers()
    }

fun AsyncTradingStatusStreamProcessorAdapter.toMarketDataProcessor() =
    AsyncMarketDataStreamProcessorAdapter {
        CompletableFuture.runAsync {
            if (it.hasTradingStatus()) {
                process(it.tradingStatus)
            }
        }
    }.apply {
        if (afterEachTradingStatusHandlers) runAfterEachTradingStatusHandlers()
        if (beforeEachTradingStatusHandlers) runBeforeTradingStatusHandlers()
    }

fun CoroutineTradingStatusStreamProcessorAdapter.toMarketDataProcessor() =
    CoroutineMarketDataStreamProcessorAdapter {
        if (it.hasTradingStatus()) {
            process(it.tradingStatus)
        }
    }.apply {
        if (afterEachTradingStatusHandlers) runAfterEachTradingStatusHandlers()
        if (beforeEachTradingStatusHandlers) runBeforeTradingStatusHandlers()
    }