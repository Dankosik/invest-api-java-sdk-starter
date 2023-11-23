package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.annotation.marketdata.HandleCandle
import io.github.dankosik.starter.invest.annotation.marketdata.HandleLastPrice
import io.github.dankosik.starter.invest.annotation.marketdata.HandleOrderBook
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTrades
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTradingStatus
import io.github.dankosik.starter.invest.annotation.marketdata.extractFigiToInstrumentTypeMap
import io.github.dankosik.starter.invest.annotation.marketdata.extractTickerToInstrumentTypeMap
import io.github.dankosik.starter.invest.annotation.marketdata.extractTickersWithoutInstrumentType
import io.github.dankosik.starter.invest.annotation.marketdata.extractUidToInstrumentTypeMap
import io.github.dankosik.starter.invest.annotation.order.HandleOrders
import io.github.dankosik.starter.invest.annotation.order.extractFigiToInstrumentTypeMap
import io.github.dankosik.starter.invest.annotation.order.extractTickerToInstrumentTypeMap
import io.github.dankosik.starter.invest.annotation.order.extractTickersWithoutInstrumentType
import io.github.dankosik.starter.invest.annotation.order.extractUidToInstrumentTypeMap
import io.github.dankosik.starter.invest.contract.candle.AsyncCandleHandler
import io.github.dankosik.starter.invest.contract.candle.BlockingCandleHandler
import io.github.dankosik.starter.invest.contract.candle.CoroutineCandleHandler
import io.github.dankosik.starter.invest.contract.lastprice.AsyncLastPriceHandler
import io.github.dankosik.starter.invest.contract.lastprice.BlockingLastPriceHandler
import io.github.dankosik.starter.invest.contract.lastprice.CoroutineLastPriceHandler
import io.github.dankosik.starter.invest.contract.orderbook.AsyncOrderBookHandler
import io.github.dankosik.starter.invest.contract.orderbook.BlockingOrderBookHandler
import io.github.dankosik.starter.invest.contract.orderbook.CoroutineOrderBookHandler
import io.github.dankosik.starter.invest.contract.orders.AsyncOrdersHandler
import io.github.dankosik.starter.invest.contract.orders.BlockingOrdersHandler
import io.github.dankosik.starter.invest.contract.orders.CoroutineOrdersHandler
import io.github.dankosik.starter.invest.contract.status.AsyncTradingStatusHandler
import io.github.dankosik.starter.invest.contract.status.BlockingTradingStatusHandler
import io.github.dankosik.starter.invest.contract.status.CoroutineTradingStatusHandler
import io.github.dankosik.starter.invest.contract.trade.AsyncTradesHandler
import io.github.dankosik.starter.invest.contract.trade.BlockingTradesHandler
import io.github.dankosik.starter.invest.contract.trade.CoroutineTradesHandler
import io.github.dankosik.starter.invest.extension.awaitSingle
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.tinkoff.piapi.contract.v1.InstrumentStatus
import ru.tinkoff.piapi.contract.v1.InstrumentType
import ru.tinkoff.piapi.core.InstrumentsService

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(value = [InstrumentsService::class])
class InstrumentsMapAutoConfiguration(
    private val applicationContext: ApplicationContext,
    private val instrumentsServices: List<InstrumentsService>,
) {
    private val tickerToUidMap = HashMap<String, String>()

    private val instrumentsService = instrumentsServices.first()

    @Bean
    fun tickerToUidMap(): Map<String, String> = runBlocking {
        val beansWithHandleOrderBook = applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values
        val beansWithHandleTrades = applicationContext.getBeansWithAnnotation(HandleTrades::class.java).values
        val beansWithHandleLastPrice = applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values
        val beansWithHandleCandle = applicationContext.getBeansWithAnnotation(HandleCandle::class.java).values
        val beansWithHandleTradingStatus =
            applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values
        val beansWithHandleOrders = applicationContext.getBeansWithAnnotation(HandleOrders::class.java).values

        val orderBookHandlers = beansWithHandleOrderBook.filterIsInstance<CoroutineOrderBookHandler>() +
                beansWithHandleOrderBook.filterIsInstance<AsyncOrderBookHandler>() +
                beansWithHandleOrderBook.filterIsInstance<BlockingOrderBookHandler>()
        val tradesHandlers = beansWithHandleTrades.filterIsInstance<CoroutineTradesHandler>() +
                beansWithHandleTrades.filterIsInstance<BlockingTradesHandler>() +
                beansWithHandleTrades.filterIsInstance<AsyncTradesHandler>()
        val lastPriceHandlers = beansWithHandleLastPrice.filterIsInstance<CoroutineLastPriceHandler>() +
                beansWithHandleLastPrice.filterIsInstance<BlockingLastPriceHandler>() +
                beansWithHandleLastPrice.filterIsInstance<AsyncLastPriceHandler>()
        val candleHandlers = beansWithHandleCandle.filterIsInstance<CoroutineCandleHandler>() +
                beansWithHandleCandle.filterIsInstance<BlockingCandleHandler>() +
                beansWithHandleCandle.filterIsInstance<AsyncCandleHandler>()
        val tradingStatusHandlers = beansWithHandleTradingStatus.filterIsInstance<CoroutineTradingStatusHandler>() +
                beansWithHandleTradingStatus.filterIsInstance<BlockingTradingStatusHandler>() +
                beansWithHandleTradingStatus.filterIsInstance<AsyncTradingStatusHandler>()
        val ordersHandlers = beansWithHandleOrders.filterIsInstance<CoroutineOrdersHandler>() +
                beansWithHandleOrders.filterIsInstance<BlockingOrdersHandler>() +
                beansWithHandleOrders.filterIsInstance<AsyncOrdersHandler>()
        val tradeHandlersWithInstrumentType = tradesHandlers.extractTickerToInstrumentTypeMap()
        val orderBookHandlersWithInstrumentType = orderBookHandlers.extractTickerToInstrumentTypeMap()
        val lastPriceHandlersWithInstrumentType = lastPriceHandlers.extractTickerToInstrumentTypeMap()
        val candleHandlersWithInstrumentType = candleHandlers.extractTickerToInstrumentTypeMap()
        val tradingStatusHandlersWithInstrumentType = tradingStatusHandlers.extractTickerToInstrumentTypeMap()
        val ordersHandlersWithInstrumentType = ordersHandlers.extractTickerToInstrumentTypeMap()

        listOf(
            tradeHandlersWithInstrumentType.replaceKeysAndValues(),
            orderBookHandlersWithInstrumentType.replaceKeysAndValues(),
            lastPriceHandlersWithInstrumentType.replaceKeysAndValues(),
            candleHandlersWithInstrumentType.replaceKeysAndValues(),
            tradingStatusHandlersWithInstrumentType.replaceKeysAndValues(),
            ordersHandlersWithInstrumentType.replaceKeysAndValues(),
        ).mergeMaps().validateOnNotSameInstrumentType(instrumentIdentifier = "Ticker")

        listOf(
            tradesHandlers.extractUidToInstrumentTypeMap().replaceKeysAndValues(),
            orderBookHandlers.extractUidToInstrumentTypeMap().replaceKeysAndValues(),
            lastPriceHandlers.extractUidToInstrumentTypeMap().replaceKeysAndValues(),
            candleHandlers.extractUidToInstrumentTypeMap().replaceKeysAndValues(),
            tradingStatusHandlers.extractUidToInstrumentTypeMap().replaceKeysAndValues(),
            ordersHandlers.extractUidToInstrumentTypeMap().replaceKeysAndValues(),
        ).mergeMaps().validateOnNotSameInstrumentType(
            warnMessage = "InstrumentType for `instrumentUid` is useless, InstrumentType needed only for `ticker`",
            instrumentIdentifier = "InstrumentUid"
        )

        listOf(
            tradesHandlers.extractFigiToInstrumentTypeMap().replaceKeysAndValues(),
            orderBookHandlers.extractFigiToInstrumentTypeMap().replaceKeysAndValues(),
            lastPriceHandlers.extractFigiToInstrumentTypeMap().replaceKeysAndValues(),
            candleHandlers.extractFigiToInstrumentTypeMap().replaceKeysAndValues(),
            tradingStatusHandlers.extractFigiToInstrumentTypeMap().replaceKeysAndValues(),
            ordersHandlers.extractFigiToInstrumentTypeMap().replaceKeysAndValues(),
        ).mergeMaps().validateOnNotSameInstrumentType(
            warnMessage = "InstrumentType for `figi` is useless, InstrumentType needed only for `ticker`",
            instrumentIdentifier = "Figi"
        )

        (tradeHandlersWithInstrumentType + orderBookHandlersWithInstrumentType + tradingStatusHandlersWithInstrumentType +
                lastPriceHandlersWithInstrumentType + candleHandlersWithInstrumentType + ordersHandlersWithInstrumentType)
            .map { (ticker, type) ->
                async {
                    if (tickerToUidMap[ticker] == null) {
                        val uId = getUidByTicker(ticker, type)
                        tickerToUidMap[ticker] = uId
                    }
                }
            }.awaitAll()

        val tickerWithoutInstrumentType = (orderBookHandlers.extractTickersWithoutInstrumentType() +
                tradesHandlers.extractTickersWithoutInstrumentType() +
                lastPriceHandlers.extractTickersWithoutInstrumentType() +
                candleHandlers.extractTickersWithoutInstrumentType() +
                tradingStatusHandlers.extractTickersWithoutInstrumentType() +
                ordersHandlers.extractTickersWithoutInstrumentType()).toSet()
        val tickerWithInstrumentType = tradeHandlersWithInstrumentType.keys + orderBookHandlersWithInstrumentType.keys +
                lastPriceHandlersWithInstrumentType.keys + candleHandlersWithInstrumentType.keys +
                tradingStatusHandlersWithInstrumentType.keys + ordersHandlersWithInstrumentType.keys


        (tickerWithoutInstrumentType - tickerWithInstrumentType).filter { it.isNotBlank() }
            .distinct()
            .map { ticker ->
                async {
                    val uId = getUidByTicker(ticker)
                    tickerToUidMap[ticker] = uId
                }
            }.awaitAll().toSet()
        tickerToUidMap.also { logger.info { "Tickers getting from requests ${it.keys}" } }
    }

    private suspend fun getUidByTicker(ticker: String): String =
        instrumentsService.getFutures(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
            .find { it.ticker == ticker }?.uid
            ?: instrumentsService.getShares(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                .find { it.ticker == ticker }?.uid
            ?: instrumentsService.getCurrencies(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                .find { it.ticker == ticker }?.uid
            ?: instrumentsService.getBonds(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                .find { it.ticker == ticker }?.uid
            ?: instrumentsService.getEtfs(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                .find { it.ticker == ticker }?.uid
            ?: instrumentsService.getOptions(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                .find { it.ticker == ticker }?.uid
            ?: throw IllegalArgumentException("Instrument by ticker: $ticker is not found")

    private suspend fun getUidByTicker(ticker: String, instrumentType: InstrumentType): String =
        when (instrumentType) {
            InstrumentType.INSTRUMENT_TYPE_FUTURES -> {
                instrumentsService.getFutures(InstrumentStatus.INSTRUMENT_STATUS_BASE)
                    .awaitSingle()
                    .find { it.ticker == ticker }?.uid
                    ?: throw throw IllegalArgumentException("Instrument by ticker: $ticker and InstrumentType: $instrumentType is not found")
            }

            InstrumentType.INSTRUMENT_TYPE_SHARE -> {
                instrumentsService.getShares(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                    .find { it.ticker == ticker }?.uid
                    ?: throw throw IllegalArgumentException("Instrument by ticker: $ticker and InstrumentType: $instrumentType is not found")
            }

            InstrumentType.INSTRUMENT_TYPE_BOND -> {
                instrumentsService.getBonds(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                    .find { it.ticker == ticker }?.uid
                    ?: throw throw IllegalArgumentException("Instrument by ticker: $ticker and InstrumentType: $instrumentType is not found")
            }

            InstrumentType.INSTRUMENT_TYPE_OPTION -> {
                instrumentsService.getOptions(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                    .find { it.ticker == ticker }?.uid
                    ?: throw throw IllegalArgumentException("Instrument by ticker: $ticker and InstrumentType: $instrumentType is not found")
            }

            InstrumentType.INSTRUMENT_TYPE_CURRENCY -> {
                instrumentsService.getCurrencies(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                    .find { it.ticker == ticker }?.uid
                    ?: throw throw IllegalArgumentException("Instrument by ticker: $ticker and InstrumentType: $instrumentType is not found")
            }

            InstrumentType.INSTRUMENT_TYPE_ETF -> {
                instrumentsService.getEtfs(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                    .find { it.ticker == ticker }?.uid
                    ?: throw throw IllegalArgumentException("Instrument by ticker: $ticker and InstrumentType: $instrumentType is not found")
            }

            else -> throw IllegalArgumentException("InstrumentType: $instrumentType not supported")
        }

    private fun Map<String, InstrumentType>.replaceKeysAndValues(): Map<InstrumentType, List<String>> {
        val transformedMap = mutableMapOf<InstrumentType, MutableList<String>>()
        forEach {
            transformedMap.getOrPut(it.value) { mutableListOf() }.add(it.key)
        }
        return transformedMap
    }

    private fun Map<InstrumentType, List<String>>.validateOnNotSameInstrumentType(
        warnMessage: String? = null,
        instrumentIdentifier: String
    ) {
        val tickersCount = values.flatten().groupingBy { it }.eachCount()
        tickersCount.forEach { (instrument, countOfTypes) ->
            require(countOfTypes == 1) {
                "$instrumentIdentifier: $instrument any InstrumentType found, check your handlers. InstrumentType should be the same"
            }.also { warnMessage?.let { logger.warn { it } } }
        }
    }

    private fun List<Map<InstrumentType, List<String>>>.mergeMaps(): Map<InstrumentType, List<String>> {
        val mergedMap = mutableMapOf<InstrumentType, MutableList<String>>()
        forEach { map ->
            for ((key, value) in map) {
                mergedMap.getOrPut(key) { mutableListOf() }.addAll(value)
                mergedMap[key] = mergedMap[key]?.distinct()?.toMutableList()!!
            }
        }
        return mergedMap
    }

    private companion object : KLogging()
}