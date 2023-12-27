package io.github.dankosik.starter.invest.extension

import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.toHandlersMapFromFigies
import io.github.dankosik.starter.invest.processor.marketdata.common.toHandlersMapFromInstrumentUids
import io.github.dankosik.starter.invest.processor.marketdata.common.toHandlersMapFromTickers


internal fun List<BaseMarketDataStreamProcessor>.extractCommonHandlers() =
    filter {
        !it.beforeEachTradeHandler && !it.afterEachTradeHandler
                && !it.beforeEachTradingStatusHandler && !it.afterEachTradingStatusHandler
                && !it.beforeEachCandleHandler && !it.afterEachCandleHandler
                && !it.beforeEachOrderBookHandler && !it.afterEachOrderBookHandler
                && !it.beforeEachLastPriceHandler && !it.afterEachLastPriceHandler
                && it.tickers.isEmpty() && it.figies.isEmpty() && it.instruemntUids.isEmpty()
    }.takeIf { it.isNotEmpty() }

//Trades
internal fun List<BaseMarketDataStreamProcessor>.extractBeforeTradesHandlersByTicker(sourceTickerToInstrumentMap: Map<String, String>) =
    filter { it.beforeEachTradeHandler && it.tickers.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromTickers(sourceTickerToInstrumentMap)

internal fun List<BaseMarketDataStreamProcessor>.extractAfterTradesHandlersByTicker(sourceTickerToInstrumentMap: Map<String, String>) =
    filter { it.afterEachTradeHandler && it.tickers.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromTickers(sourceTickerToInstrumentMap)

internal fun List<BaseMarketDataStreamProcessor>.extractBeforeTradesHandlersByFigi() =
    filter { it.beforeEachTradeHandler && it.figies.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromFigies()

internal fun List<BaseMarketDataStreamProcessor>.extractAfterTradesHandlersByFigi() =
    filter { it.afterEachTradeHandler && it.figies.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromFigies()

internal fun List<BaseMarketDataStreamProcessor>.extractBeforeTradesHandlersByUid() =
    filter { it.beforeEachTradeHandler && it.instruemntUids.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromInstrumentUids()

internal fun List<BaseMarketDataStreamProcessor>.extractAfterTradesHandlersByUid() =
    filter { it.afterEachTradeHandler && it.instruemntUids.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromInstrumentUids()

internal fun List<BaseMarketDataStreamProcessor>.extractCommonBeforeTradesHandlers() =
    filter { it.beforeEachTradeHandler && it.tickers.isEmpty() && it.figies.isEmpty() && it.instruemntUids.isEmpty() }
        .takeIf { it.isNotEmpty() }

internal fun List<BaseMarketDataStreamProcessor>.extractCommonAfterTradesHandlers() =
    filter { it.afterEachTradeHandler && it.tickers.isEmpty() && it.figies.isEmpty() && it.instruemntUids.isEmpty() }
        .takeIf { it.isNotEmpty() }

//OrderBooks