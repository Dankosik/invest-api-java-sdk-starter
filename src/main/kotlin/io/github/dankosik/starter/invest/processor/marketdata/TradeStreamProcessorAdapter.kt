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
import java.util.function.Consumer
import java.util.function.Function

interface BaseTradeStreamProcessor {
    var beforeEachTradeHandler: Boolean
    var afterEachTradeHandler: Boolean
    var tickers: List<String>
    var figies: List<String>
    var instruemntUids: List<String>
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

class TradeStreamProcessorAdapterFactory {

    companion object {
        private var beforeEachTradeHandlerCompanion: Boolean = false
        private var afterEachTradeHandlerCompanion: Boolean = false
        private var tickersCompanion: List<String> = emptyList()
        private var figiesCompanion: List<String> = emptyList()
        private var instrumentUidsCompanion: List<String> = emptyList()

        @JvmStatic
        fun createBlockingHandler(consumer: Consumer<Trade>): BlockingTradeStreamProcessorAdapter =
            object : BlockingTradeStreamProcessorAdapter {
                override fun process(trade: Trade) = consumer.accept(trade)
                override var beforeEachTradeHandler: Boolean = beforeEachTradeHandlerCompanion
                override var afterEachTradeHandler: Boolean = afterEachTradeHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion

            }.also {
                beforeEachTradeHandlerCompanion = false
                afterEachTradeHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun createAsyncHandler(consumer: Function<Trade, CompletableFuture<Void>>): AsyncTradeStreamProcessorAdapter =
            object : AsyncTradeStreamProcessorAdapter {
                override fun process(trade: Trade) = consumer.apply(trade)
                override var beforeEachTradeHandler: Boolean = beforeEachTradeHandlerCompanion
                override var afterEachTradeHandler: Boolean = afterEachTradeHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
            }.also {
                beforeEachTradeHandlerCompanion = false
                afterEachTradeHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun createCoroutineHandler(block: suspend (Trade) -> Unit): CoroutineTradeStreamProcessorAdapter =
            object : CoroutineTradeStreamProcessorAdapter {
                override suspend fun process(trade: Trade): Unit = block(trade)
                override var beforeEachTradeHandler: Boolean = beforeEachTradeHandlerCompanion
                override var afterEachTradeHandler: Boolean = afterEachTradeHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
            }.also {
                beforeEachTradeHandlerCompanion = false
                afterEachTradeHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun runBeforeEachCandleHandler(value: Boolean): Companion {
            this.afterEachTradeHandlerCompanion = value
            return Companion
        }

        @JvmStatic
        fun withTickers(tickers: List<String>): Companion {
            this.tickersCompanion = tickers
            return Companion
        }

        @JvmStatic
        fun withFigies(figies: List<String>): Companion {
            this.figiesCompanion = figies
            return Companion
        }

        @JvmStatic
        fun withInstrumentUids(instrumentUids: List<String>): Companion {
            this.instrumentUidsCompanion = instrumentUids
            return Companion
        }

        @JvmStatic
        fun runAfterEachCandleHandler(value: Boolean): Companion {
            this.beforeEachTradeHandlerCompanion = value
            return Companion
        }
    }
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

    else -> throw CommonException(ErrorCode.STREAM_PROCESSOR_ADAPTER_NOT_FOUND)
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