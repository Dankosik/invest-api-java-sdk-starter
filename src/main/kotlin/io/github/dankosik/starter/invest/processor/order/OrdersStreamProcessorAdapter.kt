package io.github.dankosik.starter.invest.processor.order

import io.github.dankosik.starter.invest.processor.marketdata.BaseTradingStatusStreamProcessor
import io.github.dankosik.starter.invest.processor.operation.PositionsStreamProcessorAdapterFactory
import ru.tinkoff.piapi.contract.v1.TradesStreamResponse
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

interface BaseOrdersStreamProcessor {
    var beforeEachOrdersHandler: Boolean
    var afterEachOrdersHandler: Boolean
    var tickers: List<String>
    var figies: List<String>
    var instruemntUids: List<String>
    val accounts: List<String>
}

interface BlockingOrdersStreamProcessorAdapter : BaseOrdersStreamProcessor {
    fun process(tradesStreamResponse: TradesStreamResponse)
}

interface AsyncOrdersStreamProcessorAdapter : BaseOrdersStreamProcessor {
    fun process(tradesStreamResponse: TradesStreamResponse): CompletableFuture<Void>
}

interface CoroutineOrdersStreamProcessorAdapter : BaseOrdersStreamProcessor {
    suspend fun process(tradesStreamResponse: TradesStreamResponse)
}

fun BaseOrdersStreamProcessor.extractInstruments(sourceTickerMap: Map<String, String>): List<String> {
    val map = tickers.mapNotNull { ticker ->
        sourceTickerMap[ticker]
    }
    return (map + figies + instruemntUids)
        .filter { it.isNotEmpty() }
}

class OrdersStreamProcessorAdapterFactory {

    companion object {
        private var beforeEachOrdersHandlerCompanion: Boolean = false
        private var afterEachOrdersHandlerCompanion: Boolean = false
        private var tickersCompanion: List<String> = emptyList()
        private var figiesCompanion: List<String> = emptyList()
        private var instrumentUidsCompanion: List<String> = emptyList()
        private var accountsCompanion: List<String> = emptyList()

        @JvmStatic
        fun createBlockingHandler(consumer: Consumer<TradesStreamResponse>): BlockingOrdersStreamProcessorAdapter =
            object : BlockingOrdersStreamProcessorAdapter {
                override fun process(tradesStreamResponse: TradesStreamResponse) = consumer.accept(tradesStreamResponse)
                override var beforeEachOrdersHandler: Boolean = beforeEachOrdersHandlerCompanion
                override var afterEachOrdersHandler: Boolean = afterEachOrdersHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
                override var accounts: List<String> = accountsCompanion
            }.also {
                beforeEachOrdersHandlerCompanion = false
                afterEachOrdersHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
                accountsCompanion = emptyList()
            }

        @JvmStatic
        fun createAsyncHandler(consumer: Function<TradesStreamResponse, CompletableFuture<Void>>): AsyncOrdersStreamProcessorAdapter =
            object : AsyncOrdersStreamProcessorAdapter {
                override fun process(tradesStreamResponse: TradesStreamResponse) = consumer.apply(tradesStreamResponse)
                override var beforeEachOrdersHandler: Boolean = beforeEachOrdersHandlerCompanion
                override var afterEachOrdersHandler: Boolean = afterEachOrdersHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
                override var accounts: List<String> = accountsCompanion
            }.also {
                beforeEachOrdersHandlerCompanion = false
                afterEachOrdersHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
                accountsCompanion = emptyList()
            }

        @JvmStatic
        fun createCoroutineHandler(block: suspend (TradesStreamResponse) -> Unit): CoroutineOrdersStreamProcessorAdapter =
            object : CoroutineOrdersStreamProcessorAdapter {
                override suspend fun process(tradesStreamResponse: TradesStreamResponse): Unit =
                    block(tradesStreamResponse)

                override var beforeEachOrdersHandler: Boolean = beforeEachOrdersHandlerCompanion
                override var afterEachOrdersHandler: Boolean = afterEachOrdersHandlerCompanion
                override var tickers: List<String> = tickersCompanion
                override var figies: List<String> = figiesCompanion
                override var instruemntUids: List<String> = instrumentUidsCompanion
                override var accounts: List<String> = accountsCompanion
            }.also {
                beforeEachOrdersHandlerCompanion = false
                afterEachOrdersHandlerCompanion = false
                tickersCompanion = emptyList()
                figiesCompanion = emptyList()
                instrumentUidsCompanion = emptyList()
                accountsCompanion = emptyList()
            }

        @JvmStatic
        fun runBeforeEachOrdersHandler(value: Boolean): Companion {
            this.beforeEachOrdersHandlerCompanion = value
            return Companion
        }

        @JvmStatic
        fun runEachOrdersHandler(value: Boolean): Companion {
            this.afterEachOrdersHandlerCompanion = value
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
        fun withAccounts(accounts: List<String>): Companion {
            this.accountsCompanion = accounts
            return Companion
        }
    }
}