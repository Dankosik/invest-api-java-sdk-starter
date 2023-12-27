package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.exception.CommonException
import io.github.dankosik.starter.invest.exception.ErrorCode
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.MarketDataStreamProcessorAdapterFactory
import ru.tinkoff.piapi.contract.v1.Candle
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

interface BaseCandleStreamProcessor {
    var beforeEachCandleHandler: Boolean
    var afterEachCandleHandler: Boolean
    var tickers: List<String>
    var figies: List<String>
    var instruemntUids: List<String>
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

class CandleStreamProcessorAdapterFactory {

    companion object {
        private var afterEachCandleHandlerCompanion: Boolean = false
        private var beforeEachCandleHandlerCompanion: Boolean = false
        private var tickersCompanion: List<String> = emptyList()
        private var figiesCompanion: List<String> = emptyList()
        private var instrumentUidsCompanion: List<String> = emptyList()

        @JvmStatic
        fun createBlockingHandler(consumer: Consumer<Candle>): BlockingCandleStreamProcessorAdapter =
            object : BlockingCandleStreamProcessorAdapter {
                override fun process(candle: Candle) = consumer.accept(candle)
                override var beforeEachCandleHandler: Boolean = afterEachCandleHandlerCompanion
                override var afterEachCandleHandler: Boolean = beforeEachCandleHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion

            }.also {
                afterEachCandleHandlerCompanion = false
                beforeEachCandleHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun createAsyncHandler(consumer: Function<Candle, CompletableFuture<Void>>): AsyncCandleStreamProcessorAdapter =
            object : AsyncCandleStreamProcessorAdapter {
                override fun process(candle: Candle) = consumer.apply(candle)
                override var beforeEachCandleHandler: Boolean = afterEachCandleHandlerCompanion
                override var afterEachCandleHandler: Boolean = beforeEachCandleHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
            }.also {
                afterEachCandleHandlerCompanion = false
                beforeEachCandleHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun createCoroutineHandler(block: suspend (Candle) -> Unit): CoroutineCandleStreamProcessorAdapter =
            object : CoroutineCandleStreamProcessorAdapter {
                override suspend fun process(candle: Candle): Unit = block(candle)
                override var beforeEachCandleHandler: Boolean = afterEachCandleHandlerCompanion
                override var afterEachCandleHandler: Boolean = beforeEachCandleHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
            }.also {
                afterEachCandleHandlerCompanion = false
                beforeEachCandleHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun runBeforeEachCandleHandler(value: Boolean): Companion {
            this.beforeEachCandleHandlerCompanion = value
            return Companion
        }

        @JvmStatic
        fun runAfterEachCandleHandler(value: Boolean): Companion {
            this.afterEachCandleHandlerCompanion = value
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

fun BaseCandleStreamProcessor.toMarketDataProcessor(): BaseMarketDataStreamProcessor = when (this) {
    is BlockingCandleStreamProcessorAdapter -> this.toMarketDataProcessor()

    is AsyncCandleStreamProcessorAdapter -> this.toMarketDataProcessor()

    is CoroutineCandleStreamProcessorAdapter -> this.toMarketDataProcessor()

    else -> throw CommonException(ErrorCode.STREAM_PROCESSOR_ADAPTER_NOT_FOUND)
}

fun BlockingCandleStreamProcessorAdapter.toMarketDataProcessor(): BlockingMarketDataStreamProcessorAdapter =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachCandleHandler(this@toMarketDataProcessor.beforeEachCandleHandler)
        .runAfterEachCandleHandler(this@toMarketDataProcessor.afterEachCandleHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createBlockingHandler {
            if (it.hasCandle()) {
                process(it.candle)
            }
        }

fun AsyncCandleStreamProcessorAdapter.toMarketDataProcessor() =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachCandleHandler(this@toMarketDataProcessor.beforeEachCandleHandler)
        .runAfterEachCandleHandler(this@toMarketDataProcessor.afterEachCandleHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createAsyncHandler {
            CompletableFuture.runAsync {
                if (it.hasCandle()) {
                    process(it.candle)
                }
            }
        }

fun CoroutineCandleStreamProcessorAdapter.toMarketDataProcessor() =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachCandleHandler(this@toMarketDataProcessor.beforeEachCandleHandler)
        .runAfterEachCandleHandler(this@toMarketDataProcessor.afterEachCandleHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createCoroutineHandler {
            if (it.hasCandle()) {
                process(it.candle)
            }
        }