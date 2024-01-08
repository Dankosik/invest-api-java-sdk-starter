package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.exception.CommonException
import io.github.dankosik.starter.invest.exception.ErrorCode
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.MarketDataStreamProcessorAdapterFactory
import ru.tinkoff.piapi.contract.v1.TradingStatus
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

interface BaseTradingStatusStreamProcessor {
    var beforeEachTradingStatusHandler: Boolean
    var afterEachTradingStatusHandler: Boolean
    var tickers: List<String>
    var figies: List<String>
    var instruemntUids: List<String>
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

fun BaseTradingStatusStreamProcessor.extractInstruments(sourceTickerMap: Map<String, String>): List<String> {
    val map = tickers.mapNotNull { ticker ->
        sourceTickerMap[ticker]
    }
    return (map + figies + instruemntUids)
        .filter { it.isNotEmpty() }
}

class TradingStatusStreamProcessorAdapterFactory {

    companion object {
        private var beforeEachTradingStatusHandlerCompanion: Boolean = false
        private var afterEachTradingStatusHandlerCompanion: Boolean = false
        private var tickersCompanion: List<String> = emptyList()
        private var figiesCompanion: List<String> = emptyList()
        private var instrumentUidsCompanion: List<String> = emptyList()

        @JvmStatic
        fun createBlockingHandler(consumer: Consumer<TradingStatus>): BlockingTradingStatusStreamProcessorAdapter =
            object : BlockingTradingStatusStreamProcessorAdapter {
                override fun process(tradingStatus: TradingStatus) = consumer.accept(tradingStatus)
                override var beforeEachTradingStatusHandler: Boolean = beforeEachTradingStatusHandlerCompanion
                override var afterEachTradingStatusHandler: Boolean = afterEachTradingStatusHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion

            }.also {
                beforeEachTradingStatusHandlerCompanion = false
                afterEachTradingStatusHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun createAsyncHandler(consumer: Function<TradingStatus, CompletableFuture<Void>>): AsyncTradingStatusStreamProcessorAdapter =
            object : AsyncTradingStatusStreamProcessorAdapter {
                override fun process(tradingStatus: TradingStatus) = consumer.apply(tradingStatus)
                override var beforeEachTradingStatusHandler: Boolean = beforeEachTradingStatusHandlerCompanion
                override var afterEachTradingStatusHandler: Boolean = afterEachTradingStatusHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
            }.also {
                beforeEachTradingStatusHandlerCompanion = false
                afterEachTradingStatusHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun createCoroutineHandler(block: suspend (TradingStatus) -> Unit): CoroutineTradingStatusStreamProcessorAdapter =
            object : CoroutineTradingStatusStreamProcessorAdapter {
                override suspend fun process(tradingStatus: TradingStatus): Unit = block(tradingStatus)
                override var beforeEachTradingStatusHandler: Boolean = beforeEachTradingStatusHandlerCompanion
                override var afterEachTradingStatusHandler: Boolean = afterEachTradingStatusHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
            }.also {
                beforeEachTradingStatusHandlerCompanion = false
                afterEachTradingStatusHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun runBeforeEachTradingStatusHandler(value: Boolean): Companion {
            this.beforeEachTradingStatusHandlerCompanion = value
            return Companion
        }

        @JvmStatic
        fun runAfterEachTradingStatusHandler(value: Boolean): Companion {
            this.afterEachTradingStatusHandlerCompanion = value
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

fun BaseTradingStatusStreamProcessor.toMarketDataProcessor(): BaseMarketDataStreamProcessor = when (this) {
    is BlockingTradingStatusStreamProcessorAdapter -> this.toMarketDataProcessor()

    is AsyncTradingStatusStreamProcessorAdapter -> this.toMarketDataProcessor()

    is CoroutineTradingStatusStreamProcessorAdapter -> this.toMarketDataProcessor()

    else -> throw CommonException(ErrorCode.STREAM_PROCESSOR_ADAPTER_NOT_FOUND)
}

fun BlockingTradingStatusStreamProcessorAdapter.toMarketDataProcessor() =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachTradingStatusHandler(this@toMarketDataProcessor.beforeEachTradingStatusHandler)
        .runAfterEachTradingStatusHandler(this@toMarketDataProcessor.afterEachTradingStatusHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createBlockingHandler {
            if (it.hasTradingStatus()) {
                process(it.tradingStatus)
            }
        }

fun AsyncTradingStatusStreamProcessorAdapter.toMarketDataProcessor() =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachTradingStatusHandler(this@toMarketDataProcessor.beforeEachTradingStatusHandler)
        .runAfterEachTradingStatusHandler(this@toMarketDataProcessor.afterEachTradingStatusHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createAsyncHandler {
            CompletableFuture.runAsync {
                if (it.hasTradingStatus()) {
                    process(it.tradingStatus)
                }
            }
        }

fun CoroutineTradingStatusStreamProcessorAdapter.toMarketDataProcessor() =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachTradingStatusHandler(this@toMarketDataProcessor.beforeEachTradingStatusHandler)
        .runAfterEachTradingStatusHandler(this@toMarketDataProcessor.afterEachTradingStatusHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createCoroutineHandler {
            if (it.hasTradingStatus()) {
                process(it.tradingStatus)
            }
        }