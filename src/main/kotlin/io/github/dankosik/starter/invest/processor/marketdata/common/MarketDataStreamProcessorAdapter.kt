package io.github.dankosik.starter.invest.processor.marketdata.common

import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

interface BaseMarketDataStreamProcessor {
    var beforeEachOrderBookHandler: Boolean
    var afterEachOrderBookHandler: Boolean
    var beforeEachTradeHandler: Boolean
    var afterEachTradeHandler: Boolean
    var beforeEachLastPriceHandler: Boolean
    var afterEachLastPriceHandler: Boolean
    var beforeEachCandleHandler: Boolean
    var afterEachCandleHandler: Boolean
    var beforeEachTradingStatusHandler: Boolean
    var afterEachTradingStatusHandler: Boolean
    var tickers: List<String>
    var figies: List<String>
    var instruemntUids: List<String>
}

fun BaseMarketDataStreamProcessor.extractInstruments(sourceTickerMap: Map<String, String>): List<String> {
    val map = tickers.mapNotNull { ticker ->
        sourceTickerMap[ticker]
    }
    return (map + figies + instruemntUids)
        .filter { it.isNotEmpty() }
}

fun List<BaseMarketDataStreamProcessor>.toHandlersMapFromTickers(sourceTickerToInstrumentMap: Map<String, String>) =
    associateBy(
        keySelector = { it.tickers },
        valueTransform = { it }
    ).transformMap(sourceTickerToInstrumentMap)

fun List<BaseMarketDataStreamProcessor>.toHandlersMapFromFigies() =
    associateBy(
        keySelector = { it.figies },
        valueTransform = { it }
    ).transformMap()

fun List<BaseMarketDataStreamProcessor>.toHandlersMapFromInstrumentUids() =
    associateBy(
        keySelector = { it.instruemntUids },
        valueTransform = { it }
    ).transformMap()

private fun Map<List<String>, BaseMarketDataStreamProcessor>.transformMap(sourceTickerToInstrumentMap: Map<String, String>): Map<String, List<BaseMarketDataStreamProcessor>> =
    flatMap { (keys, value) ->
        keys.map { key -> sourceTickerToInstrumentMap[key]!! to value }
    }.groupBy({ it.first }, { it.second })

private fun Map<List<String>, BaseMarketDataStreamProcessor>.transformMap(): Map<String, List<BaseMarketDataStreamProcessor>> =
    flatMap { (keys, value) ->
        keys.map { key -> key to value }
    }.groupBy({ it.first }, { it.second })

interface BlockingMarketDataStreamProcessorAdapter : BaseMarketDataStreamProcessor {
    fun process(marketDataResponse: MarketDataResponse)
}

interface AsyncMarketDataStreamProcessorAdapter : BaseMarketDataStreamProcessor {
    fun process(marketDataResponse: MarketDataResponse): CompletableFuture<Void>
}

interface CoroutineMarketDataStreamProcessorAdapter : BaseMarketDataStreamProcessor {
    suspend fun process(marketDataResponse: MarketDataResponse)
}

class MarketDataStreamProcessorAdapterFactory {

    companion object {
        private var beforeEachLastPriceHandlerCompanion: Boolean = false
        private var afterEachLastPriceHandlerCompanion: Boolean = false
        private var afterEachCandleHandlerCompanion: Boolean = false
        private var beforeEachCandleHandlerCompanion: Boolean = false
        private var beforeEachOrderBookHandlerCompanion: Boolean = false
        private var afterEachOrderBookHandlerCompanion: Boolean = false
        private var beforeEachTradeHandlerCompanion: Boolean = false
        private var afterEachTradeHandlerCompanion: Boolean = false
        private var beforeEachTradingStatusHandlerCompanion: Boolean = false
        private var afterEachTradingStatusHandlerCompanion: Boolean = false
        private var tickersCompanion: List<String> = emptyList()
        private var figiesCompanion: List<String> = emptyList()
        private var instrumentUidsCompanion: List<String> = emptyList()

        @JvmStatic
        fun createBlockingHandler(consumer: Consumer<MarketDataResponse>): BlockingMarketDataStreamProcessorAdapter =
            object : BlockingMarketDataStreamProcessorAdapter {
                override fun process(marketDataResponse: MarketDataResponse) = consumer.accept(marketDataResponse)
                override var beforeEachLastPriceHandler: Boolean = beforeEachLastPriceHandlerCompanion
                override var afterEachLastPriceHandler: Boolean = afterEachLastPriceHandlerCompanion
                override var beforeEachOrderBookHandler: Boolean = beforeEachOrderBookHandlerCompanion
                override var afterEachOrderBookHandler: Boolean = afterEachOrderBookHandlerCompanion
                override var beforeEachTradeHandler: Boolean = beforeEachTradeHandlerCompanion
                override var afterEachTradeHandler: Boolean = afterEachTradeHandlerCompanion
                override var beforeEachTradingStatusHandler: Boolean = beforeEachTradingStatusHandlerCompanion
                override var afterEachTradingStatusHandler: Boolean = afterEachTradingStatusHandlerCompanion
                override var beforeEachCandleHandler: Boolean = beforeEachCandleHandlerCompanion
                override var afterEachCandleHandler: Boolean = afterEachCandleHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
            }.also {
                beforeEachLastPriceHandlerCompanion = false
                afterEachLastPriceHandlerCompanion = false
                beforeEachOrderBookHandlerCompanion = false
                afterEachOrderBookHandlerCompanion = false
                beforeEachTradeHandlerCompanion = false
                afterEachTradeHandlerCompanion = false
                beforeEachTradingStatusHandlerCompanion = false
                afterEachTradingStatusHandlerCompanion = false
                beforeEachCandleHandlerCompanion = false
                afterEachCandleHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun createAsyncHandler(consumer: Function<MarketDataResponse, CompletableFuture<Void>>): AsyncMarketDataStreamProcessorAdapter =
            object : AsyncMarketDataStreamProcessorAdapter {
                override fun process(marketDataResponse: MarketDataResponse) = consumer.apply(marketDataResponse)
                override var beforeEachLastPriceHandler: Boolean = beforeEachLastPriceHandlerCompanion
                override var afterEachLastPriceHandler: Boolean = afterEachLastPriceHandlerCompanion
                override var beforeEachOrderBookHandler: Boolean = beforeEachOrderBookHandlerCompanion
                override var afterEachOrderBookHandler: Boolean = afterEachOrderBookHandlerCompanion
                override var beforeEachTradeHandler: Boolean = beforeEachTradeHandlerCompanion
                override var afterEachTradeHandler: Boolean = afterEachTradeHandlerCompanion
                override var beforeEachTradingStatusHandler: Boolean = beforeEachTradingStatusHandlerCompanion
                override var afterEachTradingStatusHandler: Boolean = afterEachTradingStatusHandlerCompanion
                override var beforeEachCandleHandler: Boolean = beforeEachCandleHandlerCompanion
                override var afterEachCandleHandler: Boolean = afterEachCandleHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
            }.also {
                beforeEachLastPriceHandlerCompanion = false
                afterEachLastPriceHandlerCompanion = false
                beforeEachOrderBookHandlerCompanion = false
                afterEachOrderBookHandlerCompanion = false
                beforeEachTradeHandlerCompanion = false
                afterEachTradeHandlerCompanion = false
                beforeEachTradingStatusHandlerCompanion = false
                afterEachTradingStatusHandlerCompanion = false
                beforeEachCandleHandlerCompanion = false
                afterEachCandleHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun createCoroutineHandler(block: suspend (MarketDataResponse) -> Unit): CoroutineMarketDataStreamProcessorAdapter =
            object : CoroutineMarketDataStreamProcessorAdapter {
                override suspend fun process(marketDataResponse: MarketDataResponse): Unit = block(marketDataResponse)
                override var beforeEachLastPriceHandler: Boolean = beforeEachLastPriceHandlerCompanion
                override var afterEachLastPriceHandler: Boolean = afterEachLastPriceHandlerCompanion
                override var beforeEachOrderBookHandler: Boolean = beforeEachOrderBookHandlerCompanion
                override var afterEachOrderBookHandler: Boolean = afterEachOrderBookHandlerCompanion
                override var beforeEachTradeHandler: Boolean = beforeEachTradeHandlerCompanion
                override var afterEachTradeHandler: Boolean = afterEachTradeHandlerCompanion
                override var beforeEachTradingStatusHandler: Boolean = beforeEachTradingStatusHandlerCompanion
                override var afterEachTradingStatusHandler: Boolean = afterEachTradingStatusHandlerCompanion
                override var beforeEachCandleHandler: Boolean = beforeEachCandleHandlerCompanion
                override var afterEachCandleHandler: Boolean = afterEachCandleHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
            }.also {
                beforeEachLastPriceHandlerCompanion = false
                afterEachLastPriceHandlerCompanion = false
                beforeEachOrderBookHandlerCompanion = false
                afterEachOrderBookHandlerCompanion = false
                beforeEachTradeHandlerCompanion = false
                afterEachTradeHandlerCompanion = false
                beforeEachTradingStatusHandlerCompanion = false
                afterEachTradingStatusHandlerCompanion = false
                beforeEachCandleHandlerCompanion = false
                afterEachCandleHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
            }

        @JvmStatic
        fun runBeforeEachCandleHandler(value: Boolean): Companion {
            this.beforeEachLastPriceHandlerCompanion = value
            return Companion
        }

        @JvmStatic
        fun runAfterEachCandleHandler(value: Boolean): Companion {
            this.afterEachLastPriceHandlerCompanion = value
            return Companion
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