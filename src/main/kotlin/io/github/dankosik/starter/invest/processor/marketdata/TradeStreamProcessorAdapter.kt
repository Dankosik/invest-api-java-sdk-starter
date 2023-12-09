package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.processor.marketdata.common.AsyncMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.CoroutineMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.runAfterTradesHandlers
import io.github.dankosik.starter.invest.processor.marketdata.common.runBeforeTradesHandlers
import ru.tinkoff.piapi.contract.v1.Trade
import java.util.concurrent.CompletableFuture

interface BaseTradeStreamProcessor {
    var beforeEachTradeHandlers: Boolean
    var afterEachTradeHandlers: Boolean
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
    override var beforeEachTradeHandlers: Boolean = false
    override var afterEachTradeHandlers: Boolean = false
}


inline fun AsyncTradeStreamProcessorAdapter(
    crossinline block: (Trade) -> CompletableFuture<Void>
): AsyncTradeStreamProcessorAdapter = object : AsyncTradeStreamProcessorAdapter {
    override fun process(trade: Trade): CompletableFuture<Void> = block(trade)
    override var beforeEachTradeHandlers: Boolean = false
    override var afterEachTradeHandlers: Boolean = false
}

inline fun CoroutineTradeStreamProcessorAdapter(
    crossinline block: suspend (Trade) -> Unit
): CoroutineTradeStreamProcessorAdapter = object : CoroutineTradeStreamProcessorAdapter {
    override suspend fun process(trade: Trade): Unit = block(trade)
    override var beforeEachTradeHandlers: Boolean = false
    override var afterEachTradeHandlers: Boolean = false
}

fun <T : BaseTradeStreamProcessor> T.runBeforeEachTradingStatusHandlers(): T {
    this.beforeEachTradeHandlers = true
    return this
}

fun <T : BaseTradeStreamProcessor> T.runAfterEachTradingStatusHandlers(): T {
    this.afterEachTradeHandlers = true
    return this
}

fun BlockingTradeStreamProcessorAdapter.toMarketDataProcessor(): BlockingMarketDataStreamProcessorAdapter =
    BlockingMarketDataStreamProcessorAdapter {
        if (it.hasTrade()) {
            process(it.trade)
        }
    }.apply {
        if (afterEachTradeHandlers) runAfterTradesHandlers()
        if (beforeEachTradeHandlers) runBeforeTradesHandlers()
    }

fun AsyncTradeStreamProcessorAdapter.toMarketDataProcessor() =
    AsyncMarketDataStreamProcessorAdapter {
        CompletableFuture.runAsync {
            if (it.hasTrade()) {
                process(it.trade)
            }
        }
    }.apply {
        if (afterEachTradeHandlers) runAfterTradesHandlers()
        if (beforeEachTradeHandlers) runBeforeTradesHandlers()
    }

fun CoroutineTradeStreamProcessorAdapter.toMarketDataProcessor() =
    CoroutineMarketDataStreamProcessorAdapter {
        if (it.hasTrade()) {
            process(it.trade)
        }
    }.apply {
        if (afterEachTradeHandlers) runAfterTradesHandlers()
        if (beforeEachTradeHandlers) runBeforeTradesHandlers()
    }