package io.github.dankosik.starter.invest.processor.marketdata

import io.github.dankosik.starter.invest.exception.CommonException
import io.github.dankosik.starter.invest.exception.ErrorCode
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.BlockingMarketDataStreamProcessorAdapter
import io.github.dankosik.starter.invest.processor.marketdata.common.MarketDataStreamProcessorAdapterFactory
import ru.tinkoff.piapi.contract.v1.OrderBook
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

interface BaseOrderBookStreamProcessor {
    var beforeEachOrderBookHandler: Boolean
    var afterEachOrderBookHandler: Boolean
    var tickers: List<String>
    var figies: List<String>
    var instruemntUids: List<String>
}

interface BlockingOrderBookStreamProcessorAdapter : BaseOrderBookStreamProcessor {
    fun process(orderBook: OrderBook)
}

interface AsyncOrderBookStreamProcessorAdapter : BaseOrderBookStreamProcessor {
    fun process(orderBook: OrderBook): CompletableFuture<Void>
}

interface CoroutineOrderBookStreamProcessorAdapter : BaseOrderBookStreamProcessor {
    suspend fun process(orderBook: OrderBook)
}

class OrderBookStreamProcessorAdapterFactory {

    companion object {
        private var beforeEachOrderBookHandlerCompanion: Boolean = false
        private var afterEachOrderBookHandlerCompanion: Boolean = false
        private var tickersCompanion: List<String> = emptyList()
        private var figiesCompanion: List<String> = emptyList()
        private var instrumentUidsCompanion: List<String> = emptyList()

        @JvmStatic
        fun createBlockingHandler(consumer: Consumer<OrderBook>): BlockingOrderBookStreamProcessorAdapter =
            object : BlockingOrderBookStreamProcessorAdapter {
                override fun process(orderBook: OrderBook) = consumer.accept(orderBook)
                override var beforeEachOrderBookHandler: Boolean = beforeEachOrderBookHandlerCompanion
                override var afterEachOrderBookHandler: Boolean = afterEachOrderBookHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion

            }.also {
                beforeEachOrderBookHandlerCompanion = false
                afterEachOrderBookHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun createAsyncHandler(consumer: Function<OrderBook, CompletableFuture<Void>>): AsyncOrderBookStreamProcessorAdapter =
            object : AsyncOrderBookStreamProcessorAdapter {
                override fun process(orderBook: OrderBook) = consumer.apply(orderBook)
                override var beforeEachOrderBookHandler: Boolean = beforeEachOrderBookHandlerCompanion
                override var afterEachOrderBookHandler: Boolean = afterEachOrderBookHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
            }.also {
                beforeEachOrderBookHandlerCompanion = false
                afterEachOrderBookHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun createCoroutineHandler(block: suspend (OrderBook) -> Unit): CoroutineOrderBookStreamProcessorAdapter =
            object : CoroutineOrderBookStreamProcessorAdapter {
                override suspend fun process(orderBook: OrderBook): Unit = block(orderBook)
                override var beforeEachOrderBookHandler: Boolean = beforeEachOrderBookHandlerCompanion
                override var afterEachOrderBookHandler: Boolean = afterEachOrderBookHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
            }.also {
                beforeEachOrderBookHandlerCompanion = false
                afterEachOrderBookHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun runBeforeEachOrderBookHandler(value: Boolean): Companion {
            this.beforeEachOrderBookHandlerCompanion = value
            return Companion
        }

        @JvmStatic
        fun runAfterEachOrderBookHandler(value: Boolean): Companion {
            this.afterEachOrderBookHandlerCompanion = value
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

fun BaseOrderBookStreamProcessor.toMarketDataProcessor(): BaseMarketDataStreamProcessor = when (this) {
    is BlockingOrderBookStreamProcessorAdapter -> this.toMarketDataProcessor()

    is AsyncOrderBookStreamProcessorAdapter -> this.toMarketDataProcessor()

    is CoroutineOrderBookStreamProcessorAdapter -> this.toMarketDataProcessor()

    else -> throw CommonException(ErrorCode.STREAM_PROCESSOR_ADAPTER_NOT_FOUND)
}

fun BlockingOrderBookStreamProcessorAdapter.toMarketDataProcessor(): BlockingMarketDataStreamProcessorAdapter =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachOrderBookHandler(this@toMarketDataProcessor.beforeEachOrderBookHandler)
        .runAfterEachOrderBookHandler(this@toMarketDataProcessor.afterEachOrderBookHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createBlockingHandler {
            if (it.hasOrderbook()) {
                process(it.orderbook)
            }
        }

fun AsyncOrderBookStreamProcessorAdapter.toMarketDataProcessor() =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachOrderBookHandler(this@toMarketDataProcessor.beforeEachOrderBookHandler)
        .runAfterEachOrderBookHandler(this@toMarketDataProcessor.afterEachOrderBookHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createAsyncHandler {
            CompletableFuture.runAsync {
                if (it.hasOrderbook()) {
                    process(it.orderbook)
                }
            }
        }

fun CoroutineOrderBookStreamProcessorAdapter.toMarketDataProcessor() =
    MarketDataStreamProcessorAdapterFactory
        .runBeforeEachOrderBookHandler(this@toMarketDataProcessor.beforeEachOrderBookHandler)
        .runAfterEachOrderBookHandler(this@toMarketDataProcessor.afterEachOrderBookHandler)
        .withTickers(this@toMarketDataProcessor.tickers)
        .withFigies(this@toMarketDataProcessor.figies)
        .withInstrumentUids(this@toMarketDataProcessor.instruemntUids)
        .createCoroutineHandler {
            if (it.hasOrderbook()) {
                process(it.orderbook)
            }
        }