package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllCandles
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllLastPrices
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllOrderBooks
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTrades
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTradingStatuses
import io.github.dankosik.starter.invest.annotation.marketdata.HandleCandle
import io.github.dankosik.starter.invest.annotation.marketdata.HandleLastPrice
import io.github.dankosik.starter.invest.annotation.marketdata.HandleOrderBook
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTrade
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTradingStatus
import io.github.dankosik.starter.invest.annotation.marketdata.extractFigiToInstrumentTypeMap
import io.github.dankosik.starter.invest.annotation.marketdata.extractTickerToInstrumentTypeMap
import io.github.dankosik.starter.invest.annotation.marketdata.extractTickersFromAll
import io.github.dankosik.starter.invest.annotation.marketdata.extractTickersWithoutInstrumentType
import io.github.dankosik.starter.invest.annotation.marketdata.extractUidToInstrumentTypeMap
import io.github.dankosik.starter.invest.annotation.order.HandleAllOrders
import io.github.dankosik.starter.invest.annotation.order.HandleOrder
import io.github.dankosik.starter.invest.annotation.order.extractFigiToInstrumentTypeMap
import io.github.dankosik.starter.invest.annotation.order.extractTickerToInstrumentTypeMap
import io.github.dankosik.starter.invest.annotation.order.extractTickersFromAll
import io.github.dankosik.starter.invest.annotation.order.extractTickersWithoutInstrumentType
import io.github.dankosik.starter.invest.annotation.order.extractUidToInstrumentTypeMap
import io.github.dankosik.starter.invest.contract.marketdata.candle.getCandleHandlers
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.getLastPriceHandlers
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.getOrderBookHandlers
import io.github.dankosik.starter.invest.contract.marketdata.status.getTradingStatusHandlers
import io.github.dankosik.starter.invest.contract.marketdata.trade.getTradesHandlers
import io.github.dankosik.starter.invest.contract.orders.getOrderHandlers
import io.github.dankosik.starter.invest.exception.CommonException
import io.github.dankosik.starter.invest.exception.ErrorCode
import io.github.dankosik.starter.invest.extension.awaitSingle
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KLogging
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
        val lastPriceAllHandlers =
            applicationContext.getBeansWithAnnotation(HandleAllLastPrices::class.java).values.getLastPriceHandlers()
        val orderBookAllHandlers =
            applicationContext.getBeansWithAnnotation(HandleAllOrderBooks::class.java).values.getOrderBookHandlers()
        val tradesAllHandlers =
            applicationContext.getBeansWithAnnotation(HandleAllTrades::class.java).values.getTradesHandlers()
        val tradingStatusAllHandlers =
            applicationContext.getBeansWithAnnotation(HandleAllTradingStatuses::class.java).values.getTradingStatusHandlers()
        val candleAllHandlers =
            applicationContext.getBeansWithAnnotation(HandleAllCandles::class.java).values.getCandleHandlers()
        val ordersAllHandlers =
            applicationContext.getBeansWithAnnotation(HandleAllOrders::class.java).values.getOrderHandlers()

        val orderBookHandlers =
            applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values.getOrderBookHandlers()
        val tradesHandlers =
            applicationContext.getBeansWithAnnotation(HandleTrade::class.java).values.getTradesHandlers()
        val lastPriceHandlers =
            applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values.getLastPriceHandlers()
        val candleHandlers =
            applicationContext.getBeansWithAnnotation(HandleCandle::class.java).values.getCandleHandlers()
        val tradingStatusHandlers =
            applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values.getTradingStatusHandlers()
        val ordersHandlers =
            applicationContext.getBeansWithAnnotation(HandleOrder::class.java).values.getOrderHandlers()
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

        val tickerWithoutInstrumentType = (
                lastPriceAllHandlers.extractTickersFromAll() +
                        orderBookAllHandlers.extractTickersFromAll() +
                        ordersAllHandlers.extractTickersFromAll() +
                        tradesAllHandlers.extractTickersFromAll() +
                        tradingStatusAllHandlers.extractTickersFromAll() +
                        candleAllHandlers.extractTickersFromAll() +
                        tradesHandlers.extractTickersWithoutInstrumentType() +
                        orderBookHandlers.extractTickersWithoutInstrumentType() +
                        lastPriceHandlers.extractTickersWithoutInstrumentType() +
                        candleHandlers.extractTickersWithoutInstrumentType() +
                        tradingStatusHandlers.extractTickersWithoutInstrumentType() +
                        ordersHandlers.extractTickersWithoutInstrumentType()
                ).toSet()
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
        tickerToUidMap
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
            ?: throw CommonException(ErrorCode.INSTRUMENT_NOT_FOUND).also { logger.error { "Instrument by ticker: $ticker is not found" } }

    private suspend fun getUidByTicker(ticker: String, instrumentType: InstrumentType): String {
        val commonNotFoundMessage = "Instrument by ticker: $ticker and InstrumentType: $instrumentType is not found"
        return when (instrumentType) {
            InstrumentType.INSTRUMENT_TYPE_FUTURES -> {
                instrumentsService.getFutures(InstrumentStatus.INSTRUMENT_STATUS_BASE)
                    .awaitSingle()
                    .find { it.ticker == ticker }?.uid
                    ?: throw throw CommonException(ErrorCode.INSTRUMENT_NOT_FOUND).also { logger.error { commonNotFoundMessage } }
            }

            InstrumentType.INSTRUMENT_TYPE_SHARE -> {
                instrumentsService.getShares(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                    .find { it.ticker == ticker }?.uid
                    ?: throw throw CommonException(ErrorCode.INSTRUMENT_NOT_FOUND).also { logger.error { commonNotFoundMessage } }
            }

            InstrumentType.INSTRUMENT_TYPE_BOND -> {
                instrumentsService.getBonds(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                    .find { it.ticker == ticker }?.uid
                    ?: throw throw CommonException(ErrorCode.INSTRUMENT_NOT_FOUND).also { logger.error { commonNotFoundMessage } }
            }

            InstrumentType.INSTRUMENT_TYPE_OPTION -> {
                instrumentsService.getOptions(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                    .find { it.ticker == ticker }?.uid
                    ?: throw throw CommonException(ErrorCode.INSTRUMENT_NOT_FOUND).also { logger.error { commonNotFoundMessage } }
            }

            InstrumentType.INSTRUMENT_TYPE_CURRENCY -> {
                instrumentsService.getCurrencies(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                    .find { it.ticker == ticker }?.uid
                    ?: throw throw CommonException(ErrorCode.INSTRUMENT_NOT_FOUND).also { logger.error { commonNotFoundMessage } }
            }

            InstrumentType.INSTRUMENT_TYPE_ETF -> {
                instrumentsService.getEtfs(InstrumentStatus.INSTRUMENT_STATUS_BASE).awaitSingle()
                    .find { it.ticker == ticker }?.uid
                    ?: throw throw CommonException(ErrorCode.INSTRUMENT_NOT_FOUND).also { logger.error { commonNotFoundMessage } }
            }

            else -> throw throw CommonException(ErrorCode.INSTRUMENT_NOT_FOUND).also { logger.error { commonNotFoundMessage } }
        }
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
        val tickersCount: Map<String, Int> = values.flatten().groupingBy { it }.eachCount()
        tickersCount.forEach { (instrument, countOfTypes) ->
            require(countOfTypes == 1) {
                "$instrumentIdentifier: $instrument different InstrumentType found, check your handlers. InstrumentType should be the same"
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