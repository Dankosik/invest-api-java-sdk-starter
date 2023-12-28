package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.exception.CommonException
import io.github.dankosik.starter.invest.exception.ErrorCode
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.MarketDataStreamProcessorAdapterFactory
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

fun BaseTradeStreamProcessor.extractInstruments(sourceTickerMap: Map<String, String>): List<String> {
    val map = tickers.mapNotNull { ticker ->
        sourceTickerMap[ticker]
    }
    return (map + figies + instruemntUids)
        .filter { it.isNotEmpty() }
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
        fun runBeforeEachTradeHandler(value: Boolean): Companion {
            this.beforeEachTradeHandlerCompanion = value
            return Companion
        }

        @JvmStatic
        fun runAfterEachTradeHandler(value: Boolean): Companion {
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
    }
}

fun BaseTradeStreamProcessor.toMarketDataProcessor(): BaseMarketDataStreamProcessor = when (this) {
    is BlockingTradeStreamProcessorAdapter -> this.toMarketDataProcessor()

    is AsyncTradeStreamProcessorAdapter -> this.toMarketDataProcessor()

    is CoroutineTradeStreamProcessorAdapter -> this.toMarketDataProcessor()

    else -> throw CommonException(ErrorCode.STREAM_PROCESSOR_ADAPTER_NOT_FOUND)
}

fun BlockingTradeStreamProcessorAdapter.toMarketDataProcessor() =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachTradeHandler(this@toMarketDataProcessor.beforeEachTradeHandler)
        .runAfterEachTradeHandler(this@toMarketDataProcessor.afterEachTradeHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createBlockingHandler {
            if (it.hasTrade()) {
                process(it.trade)
            }
        }

fun AsyncTradeStreamProcessorAdapter.toMarketDataProcessor() =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachTradeHandler(this@toMarketDataProcessor.beforeEachTradeHandler)
        .runAfterEachTradeHandler(this@toMarketDataProcessor.afterEachTradeHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createAsyncHandler {
            CompletableFuture.runAsync {
                if (it.hasTrade()) {
                    process(it.trade)
                }
            }
        }

fun CoroutineTradeStreamProcessorAdapter.toMarketDataProcessor() =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachTradeHandler(this@toMarketDataProcessor.beforeEachTradeHandler)
        .runAfterEachTradeHandler(this@toMarketDataProcessor.afterEachTradeHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createCoroutineHandler {
            if (it.hasTrade()) {
                process(it.trade)
            }
        }