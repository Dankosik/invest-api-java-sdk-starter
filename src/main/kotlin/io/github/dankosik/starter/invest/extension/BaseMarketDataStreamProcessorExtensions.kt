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

internal fun List<BaseMarketDataStreamProcessor>.extractCommonHandlersByTicker(sourceTickerToInstrumentMap: Map<String, String>) =
    filter {
        !it.beforeEachTradeHandler && !it.afterEachTradeHandler
                && !it.beforeEachTradingStatusHandler && !it.afterEachTradingStatusHandler
                && !it.beforeEachCandleHandler && !it.afterEachCandleHandler
                && !it.beforeEachOrderBookHandler && !it.afterEachOrderBookHandler
                && !it.beforeEachLastPriceHandler && !it.afterEachLastPriceHandler
                && it.tickers.isNotEmpty()
    }.takeIf { it.isNotEmpty() }?.toHandlersMapFromTickers(sourceTickerToInstrumentMap)

internal fun List<BaseMarketDataStreamProcessor>.extractCommonHandlersByFigi() =
    filter {
        !it.beforeEachTradeHandler && !it.afterEachTradeHandler
                && !it.beforeEachTradingStatusHandler && !it.afterEachTradingStatusHandler
                && !it.beforeEachCandleHandler && !it.afterEachCandleHandler
                && !it.beforeEachOrderBookHandler && !it.afterEachOrderBookHandler
                && !it.beforeEachLastPriceHandler && !it.afterEachLastPriceHandler
                && it.figies.isNotEmpty()
    }.takeIf { it.isNotEmpty() }?.toHandlersMapFromFigies()

internal fun List<BaseMarketDataStreamProcessor>.extractCommonHandlersByUid() =
    filter {
        !it.beforeEachTradeHandler && !it.afterEachTradeHandler
                && !it.beforeEachTradingStatusHandler && !it.afterEachTradingStatusHandler
                && !it.beforeEachCandleHandler && !it.afterEachCandleHandler
                && !it.beforeEachOrderBookHandler && !it.afterEachOrderBookHandler
                && !it.beforeEachLastPriceHandler && !it.afterEachLastPriceHandler && it.instruemntUids.isNotEmpty() }
        .takeIf { it.isNotEmpty() }?.toHandlersMapFromInstrumentUids()

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
internal fun List<BaseMarketDataStreamProcessor>.extractBeforeOrderBookByTicker(sourceTickerToInstrumentMap: Map<String, String>) =
    filter { it.beforeEachOrderBookHandler && it.tickers.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromTickers(sourceTickerToInstrumentMap)

internal fun List<BaseMarketDataStreamProcessor>.extractAfterOrderBookHandlersByTicker(sourceTickerToInstrumentMap: Map<String, String>) =
    filter { it.afterEachOrderBookHandler && it.tickers.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromTickers(sourceTickerToInstrumentMap)

internal fun List<BaseMarketDataStreamProcessor>.extractBeforeOrderBookHandlersByFigi() =
    filter { it.beforeEachOrderBookHandler && it.figies.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromFigies()

internal fun List<BaseMarketDataStreamProcessor>.extractAfterOrderBookHandlersByFigi() =
    filter { it.afterEachOrderBookHandler && it.figies.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromFigies()

internal fun List<BaseMarketDataStreamProcessor>.extractBeforeOrderBookHandlersByUid() =
    filter { it.beforeEachOrderBookHandler && it.instruemntUids.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromInstrumentUids()

internal fun List<BaseMarketDataStreamProcessor>.extractAfterOrderBookHandlersByUid() =
    filter { it.afterEachOrderBookHandler && it.instruemntUids.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromInstrumentUids()

internal fun List<BaseMarketDataStreamProcessor>.extractCommonBeforeOrderBookHandlers() =
    filter { it.beforeEachOrderBookHandler && it.tickers.isEmpty() && it.figies.isEmpty() && it.instruemntUids.isEmpty() }
        .takeIf { it.isNotEmpty() }

internal fun List<BaseMarketDataStreamProcessor>.extractCommonAfterOrderBookHandlers() =
    filter { it.afterEachOrderBookHandler && it.tickers.isEmpty() && it.figies.isEmpty() && it.instruemntUids.isEmpty() }
        .takeIf { it.isNotEmpty() }

//LastPrice
internal fun List<BaseMarketDataStreamProcessor>.extractBeforeLastPriceByTicker(sourceTickerToInstrumentMap: Map<String, String>) =
    filter { it.beforeEachLastPriceHandler && it.tickers.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromTickers(sourceTickerToInstrumentMap)

internal fun List<BaseMarketDataStreamProcessor>.extractAfterLastPriceHandlersByTicker(sourceTickerToInstrumentMap: Map<String, String>) =
    filter { it.afterEachLastPriceHandler && it.tickers.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromTickers(sourceTickerToInstrumentMap)

internal fun List<BaseMarketDataStreamProcessor>.extractBeforeLastPriceHandlersByFigi() =
    filter { it.beforeEachLastPriceHandler && it.figies.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromFigies()

internal fun List<BaseMarketDataStreamProcessor>.extractAfterLastPriceHandlersByFigi() =
    filter { it.afterEachOrderBookHandler && it.figies.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromFigies()

internal fun List<BaseMarketDataStreamProcessor>.extractBeforeLastPriceHandlersByUid() =
    filter { it.beforeEachLastPriceHandler && it.instruemntUids.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromInstrumentUids()

internal fun List<BaseMarketDataStreamProcessor>.extractAfterLastPriceHandlersByUid() =
    filter { it.afterEachLastPriceHandler && it.instruemntUids.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromInstrumentUids()

internal fun List<BaseMarketDataStreamProcessor>.extractCommonBeforeLastPriceHandlers() =
    filter { it.beforeEachLastPriceHandler && it.tickers.isEmpty() && it.figies.isEmpty() && it.instruemntUids.isEmpty() }
        .takeIf { it.isNotEmpty() }

internal fun List<BaseMarketDataStreamProcessor>.extractCommonAfterLastPriceHandlers() =
    filter { it.afterEachLastPriceHandler && it.tickers.isEmpty() && it.figies.isEmpty() && it.instruemntUids.isEmpty() }
        .takeIf { it.isNotEmpty() }

//TradingStatus
internal fun List<BaseMarketDataStreamProcessor>.extractBeforeTradingStatusByTicker(sourceTickerToInstrumentMap: Map<String, String>) =
    filter { it.beforeEachTradingStatusHandler && it.tickers.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromTickers(sourceTickerToInstrumentMap)

internal fun List<BaseMarketDataStreamProcessor>.extractAfterTradingStatusHandlersByTicker(sourceTickerToInstrumentMap: Map<String, String>) =
    filter { it.afterEachTradingStatusHandler && it.tickers.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromTickers(sourceTickerToInstrumentMap)

internal fun List<BaseMarketDataStreamProcessor>.extractBeforeTradingStatusHandlersByFigi() =
    filter { it.beforeEachTradingStatusHandler && it.figies.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromFigies()

internal fun List<BaseMarketDataStreamProcessor>.extractAfterTradingStatusHandlersByFigi() =
    filter { it.afterEachTradingStatusHandler && it.figies.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromFigies()

internal fun List<BaseMarketDataStreamProcessor>.extractBeforeTradingStatusHandlersByUid() =
    filter { it.beforeEachTradingStatusHandler && it.instruemntUids.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromInstrumentUids()

internal fun List<BaseMarketDataStreamProcessor>.extractAfterTradingStatusHandlersByUid() =
    filter { it.afterEachTradingStatusHandler && it.instruemntUids.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromInstrumentUids()

internal fun List<BaseMarketDataStreamProcessor>.extractCommonBeforeTradingStatusHandlers() =
    filter { it.beforeEachTradingStatusHandler && it.tickers.isEmpty() && it.figies.isEmpty() && it.instruemntUids.isEmpty() }
        .takeIf { it.isNotEmpty() }

internal fun List<BaseMarketDataStreamProcessor>.extractCommonAfterTradingStatusHandlers() =
    filter { it.afterEachTradingStatusHandler && it.tickers.isEmpty() && it.figies.isEmpty() && it.instruemntUids.isEmpty() }
        .takeIf { it.isNotEmpty() }

//Candle
internal fun List<BaseMarketDataStreamProcessor>.extractBeforeCandleByTicker(sourceTickerToInstrumentMap: Map<String, String>) =
    filter { it.beforeEachCandleHandler && it.tickers.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromTickers(sourceTickerToInstrumentMap)

internal fun List<BaseMarketDataStreamProcessor>.extractAfterCandleHandlersByTicker(sourceTickerToInstrumentMap: Map<String, String>) =
    filter { it.afterEachCandleHandler && it.tickers.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromTickers(sourceTickerToInstrumentMap)

internal fun List<BaseMarketDataStreamProcessor>.extractBeforeCandleHandlersByFigi() =
    filter { it.beforeEachCandleHandler && it.figies.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromFigies()

internal fun List<BaseMarketDataStreamProcessor>.extractAfterCandleHandlersByFigi() =
    filter { it.afterEachCandleHandler && it.figies.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromFigies()

internal fun List<BaseMarketDataStreamProcessor>.extractBeforeCandleHandlersByUid() =
    filter { it.beforeEachCandleHandler && it.instruemntUids.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromInstrumentUids()

internal fun List<BaseMarketDataStreamProcessor>.extractAfterCandleHandlersByUid() =
    filter { it.afterEachCandleHandler && it.instruemntUids.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.toHandlersMapFromInstrumentUids()

internal fun List<BaseMarketDataStreamProcessor>.extractCommonBeforeCandleHandlers() =
    filter { it.beforeEachCandleHandler && it.tickers.isEmpty() && it.figies.isEmpty() && it.instruemntUids.isEmpty() }
        .takeIf { it.isNotEmpty() }

internal fun List<BaseMarketDataStreamProcessor>.extractCommonAfterCandleHandlers() =
    filter { it.afterEachCandleHandler && it.tickers.isEmpty() && it.figies.isEmpty() && it.instruemntUids.isEmpty() }
        .takeIf { it.isNotEmpty() }
