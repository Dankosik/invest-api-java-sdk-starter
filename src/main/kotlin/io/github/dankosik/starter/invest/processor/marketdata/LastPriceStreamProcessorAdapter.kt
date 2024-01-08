package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.exception.CommonException
import io.github.dankosik.starter.invest.exception.ErrorCode
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.MarketDataStreamProcessorAdapterFactory
import ru.tinkoff.piapi.contract.v1.LastPrice
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

interface BaseLastPriceStreamProcessor {
    var beforeEachLastPriceHandler: Boolean
    var afterEachLastPriceHandler: Boolean
    var tickers: List<String>
    var figies: List<String>
    var instruemntUids: List<String>
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

fun BaseLastPriceStreamProcessor.extractInstruments(sourceTickerMap: Map<String, String>): List<String> {
    val map = tickers.mapNotNull { ticker ->
        sourceTickerMap[ticker]
    }
    return (map + figies + instruemntUids)
        .filter { it.isNotEmpty() }
}

class LastPriceStreamProcessorAdapterFactory {

    companion object {
        private var beforeEachLastPriceHandlerCompanion: Boolean = false
        private var afterEachLastPriceHandlerCompanion: Boolean = false
        private var tickersCompanion: List<String> = emptyList()
        private var figiesCompanion: List<String> = emptyList()
        private var instrumentUidsCompanion: List<String> = emptyList()

        @JvmStatic
        fun createBlockingHandler(consumer: Consumer<LastPrice>): BlockingLastPriceStreamProcessorAdapter =
            object : BlockingLastPriceStreamProcessorAdapter {
                override fun process(lastPrice: LastPrice) = consumer.accept(lastPrice)
                override var beforeEachLastPriceHandler: Boolean = beforeEachLastPriceHandlerCompanion
                override var afterEachLastPriceHandler: Boolean = afterEachLastPriceHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion

            }.also {
                beforeEachLastPriceHandlerCompanion = false
                afterEachLastPriceHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun createAsyncHandler(consumer: Function<LastPrice, CompletableFuture<Void>>): AsyncLastPriceStreamProcessorAdapter =
            object : AsyncLastPriceStreamProcessorAdapter {
                override fun process(lastPrice: LastPrice) = consumer.apply(lastPrice)
                override var beforeEachLastPriceHandler: Boolean = beforeEachLastPriceHandlerCompanion
                override var afterEachLastPriceHandler: Boolean = afterEachLastPriceHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
            }.also {
                beforeEachLastPriceHandlerCompanion = false
                afterEachLastPriceHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun createCoroutineHandler(block: suspend (LastPrice) -> Unit): CoroutineLastPriceStreamProcessorAdapter =
            object : CoroutineLastPriceStreamProcessorAdapter {
                override suspend fun process(lastPrice: LastPrice): Unit = block(lastPrice)
                override var beforeEachLastPriceHandler: Boolean = beforeEachLastPriceHandlerCompanion
                override var afterEachLastPriceHandler: Boolean = afterEachLastPriceHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
            }.also {
                beforeEachLastPriceHandlerCompanion = false
                afterEachLastPriceHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun runBeforeEachLastPriceHandler(value: Boolean): Companion {
            this.beforeEachLastPriceHandlerCompanion = value
            return Companion
        }

        @JvmStatic
        fun runAfterEachLastPriceHandler(value: Boolean): Companion {
            this.afterEachLastPriceHandlerCompanion = value
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

fun BaseLastPriceStreamProcessor.toMarketDataProcessor(): BaseMarketDataStreamProcessor = when (this) {
    is BlockingLastPriceStreamProcessorAdapter -> this.toMarketDataProcessor()

    is AsyncLastPriceStreamProcessorAdapter -> this.toMarketDataProcessor()

    is CoroutineLastPriceStreamProcessorAdapter -> this.toMarketDataProcessor()

    else -> throw CommonException(ErrorCode.STREAM_PROCESSOR_ADAPTER_NOT_FOUND)
}

fun BlockingLastPriceStreamProcessorAdapter.toMarketDataProcessor(): BlockingMarketDataStreamProcessorAdapter =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachLastPriceHandler(this@toMarketDataProcessor.beforeEachLastPriceHandler)
        .runAfterEachLastPriceHandler(this@toMarketDataProcessor.afterEachLastPriceHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createBlockingHandler {
            if (it.hasLastPrice()) {
                process(it.lastPrice)
            }
        }

fun AsyncLastPriceStreamProcessorAdapter.toMarketDataProcessor() =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachLastPriceHandler(this@toMarketDataProcessor.beforeEachLastPriceHandler)
        .runAfterEachLastPriceHandler(this@toMarketDataProcessor.afterEachLastPriceHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createAsyncHandler {
            CompletableFuture.runAsync {
                if (it.hasLastPrice()) {
                    process(it.lastPrice)
                }
            }
        }

fun CoroutineLastPriceStreamProcessorAdapter.toMarketDataProcessor() =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachLastPriceHandler(this@toMarketDataProcessor.beforeEachLastPriceHandler)
        .runAfterEachLastPriceHandler(this@toMarketDataProcessor.afterEachLastPriceHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createCoroutineHandler {
            if (it.hasLastPrice()) {
                process(it.lastPrice)
            }
        }