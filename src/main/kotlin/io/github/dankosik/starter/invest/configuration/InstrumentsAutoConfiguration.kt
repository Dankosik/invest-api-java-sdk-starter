package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.annotation.marketdata.HandleCandle
import io.github.dankosik.starter.invest.annotation.marketdata.HandleLastPrice
import io.github.dankosik.starter.invest.annotation.marketdata.HandleOrderBook
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTrade
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTradingStatus
import io.github.dankosik.starter.invest.annotation.operation.HandlePortfolio
import io.github.dankosik.starter.invest.annotation.operation.HandlePosition
import io.github.dankosik.starter.invest.annotation.order.HandleOrder
import io.github.dankosik.starter.invest.contract.marketdata.candle.getCandleHandlers
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.getLastPriceHandlers
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.getOrderBookHandlers
import io.github.dankosik.starter.invest.contract.marketdata.status.getTradingStatusHandlers
import io.github.dankosik.starter.invest.contract.marketdata.trade.getTradesHandlers
import io.github.dankosik.starter.invest.contract.operation.portfolio.getPortfolioHandlers
import io.github.dankosik.starter.invest.contract.operation.positions.getPositionHandlers
import io.github.dankosik.starter.invest.contract.orders.getOrderHandlers
import io.github.dankosik.starter.invest.exception.CommonException
import io.github.dankosik.starter.invest.exception.ErrorCode
import io.github.dankosik.starter.invest.extension.awaitSingle
import io.github.dankosik.starter.invest.processor.marketdata.BaseCandleStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BaseLastPriceStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BaseOrderBookStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BaseTradeStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.BaseTradingStatusStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.common.BaseMarketDataStreamProcessor
import io.github.dankosik.starter.invest.processor.marketdata.extractInstruments
import io.github.dankosik.starter.invest.processor.operation.BasePortfolioStreamProcessor
import io.github.dankosik.starter.invest.processor.operation.BasePositionsStreamProcessor
import io.github.dankosik.starter.invest.processor.order.BaseOrdersStreamProcessor
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.tinkoff.piapi.contract.v1.InstrumentStatus
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import ru.tinkoff.piapi.core.InstrumentsService

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(StreamProcessorsAutoConfiguration::class)
class InstrumentsAutoConfiguration(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
    private val instrumentsServices: List<InstrumentsService>,
    private val baseMarketDataStreamProcessors: List<BaseMarketDataStreamProcessor>,
    private val baseCandleStreamProcessors: List<BaseCandleStreamProcessor>,
    private val baseOrdersStreamProcessors: List<BaseOrdersStreamProcessor>,
) {

    private val instrumentsService = instrumentsServices.first()

    private val newTickerToUidMap = tickerToUidMap.toMutableMap()


    init {
        runBlocking {
            (baseMarketDataStreamProcessors.map { it.tickers } +
                    baseCandleStreamProcessors.map { it.tickers } +
                    baseOrdersStreamProcessors.map { it.tickers })
                .flatten().distinct().forEach { ticker ->
                    launch {
                        if (newTickerToUidMap[ticker] == null) {
                            newTickerToUidMap[ticker] = getUidByTicker(ticker)
                        }
                    }
                }
        }
    }

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsOrderBook(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values.getOrderBookHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleOrderBook::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleOrderBook()
            }.toSet() +
                applicationContext.getBeansOfType(BaseOrderBookStreamProcessor::class.java).values.map {
                    it.extractInstruments(
                        newTickerToUidMap
                    )
                }
                    .flatten().toSet()

    @Bean("instrumentsOrderBook")
    @ConditionalOnMissingBean(name = ["instrumentsOrderBook"])
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    fun instrumentsOrderBookReadonly(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values.getOrderBookHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleOrderBook::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleOrderBook()
            }.toSet() +
                applicationContext.getBeansOfType(BaseOrderBookStreamProcessor::class.java).values.map {
                    it.extractInstruments(
                        newTickerToUidMap
                    )
                }
                    .flatten().toSet()

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsTradingStatus(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values.getTradingStatusHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleTradingStatus::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleTradingStatus()
            }.toSet() +
                applicationContext.getBeansOfType(BaseTradingStatusStreamProcessor::class.java).values.map {
                    it.extractInstruments(newTickerToUidMap)
                }.flatten().toSet()


    @Bean("instrumentsTradingStatus")
    @ConditionalOnMissingBean(name = ["instrumentsTradingStatus"])
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    fun instrumentsTradingStatusReadonly(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values.getTradingStatusHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleTradingStatus::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleTradingStatus()
            }.toSet() +
                applicationContext.getBeansOfType(BaseTradingStatusStreamProcessor::class.java).values.map {
                    it.extractInstruments(newTickerToUidMap)
                }.flatten().toSet()

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsLastPrice(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values.getLastPriceHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleLastPrice::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleLastPrice()
            }.toSet() +
                applicationContext.getBeansOfType(BaseLastPriceStreamProcessor::class.java).values.map {
                    it.extractInstruments(newTickerToUidMap)
                }.flatten().toSet()


    @Bean("instrumentsLastPrice")
    @ConditionalOnMissingBean(name = ["instrumentsLastPrice"])
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    fun instrumentsLastPriceReadonly(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values.getLastPriceHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleLastPrice::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleLastPrice()
            }.toSet() +
                applicationContext.getBeansOfType(BaseLastPriceStreamProcessor::class.java).values.map {
                    it.extractInstruments(newTickerToUidMap)
                }.flatten().toSet()

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsCandle(): MutableMap<SubscriptionInterval, MutableList<InstrumentIdToWaitingClose>> {
        val result = mutableMapOf<SubscriptionInterval, MutableList<InstrumentIdToWaitingClose>>()
        val candleBookHandlers =
            applicationContext.getBeansWithAnnotation(HandleCandle::class.java).values.getCandleHandlers()
        applicationContext.getBeansOfType(BaseCandleStreamProcessor::class.java).values.map {
            it.extractInstruments(newTickerToUidMap)
        }.forEach { extractInstrumentFromCandle ->
            extractInstrumentFromCandle.second.forEach {
                result[extractInstrumentFromCandle.first]?.add(it)
                    ?: run {
                        result[extractInstrumentFromCandle.first] = mutableListOf(it)
                    }
            }
        }
        candleBookHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleCandle::class.java)
            if (!annotation.sandboxOnly) {
                val extractInstrumentFromCandle = annotation.extractInstrumentFromCandle()
                result[extractInstrumentFromCandle.first]?.add(extractInstrumentFromCandle.second)
                    ?: run {
                        result[extractInstrumentFromCandle.first] = mutableListOf(extractInstrumentFromCandle.second)
                    }
            }
        }
        return result
    }


    @Bean("instrumentsCandle")
    @ConditionalOnMissingBean(name = ["instrumentsCandle"])
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    fun instrumentsCandleReadonly(): MutableMap<SubscriptionInterval, MutableList<InstrumentIdToWaitingClose>> {
        val result = mutableMapOf<SubscriptionInterval, MutableList<InstrumentIdToWaitingClose>>()
        val candleBookHandlers =
            applicationContext.getBeansWithAnnotation(HandleCandle::class.java).values.getCandleHandlers()
        applicationContext.getBeansOfType(BaseCandleStreamProcessor::class.java).values.map {
            it.extractInstruments(newTickerToUidMap)
        }.forEach { extractInstrumentFromCandle ->
            extractInstrumentFromCandle.second.forEach {
                result[extractInstrumentFromCandle.first]?.add(it)
                    ?: run {
                        result[extractInstrumentFromCandle.first] = mutableListOf(it)
                    }
            }
        }
        candleBookHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleCandle::class.java)
            if (!annotation.sandboxOnly) {
                val extractInstrumentFromCandle = annotation.extractInstrumentFromCandle()
                result[extractInstrumentFromCandle.first]?.add(extractInstrumentFromCandle.second)
                    ?: run {
                        result[extractInstrumentFromCandle.first] = mutableListOf(extractInstrumentFromCandle.second)
                    }
            }
        }
        return result
    }


    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsTrades(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleTrade::class.java).values.getTradesHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleTrade::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleTrades()
            }.toSet() +
                applicationContext.getBeansOfType(BaseTradeStreamProcessor::class.java).values.map {
                    it.extractInstruments(newTickerToUidMap)
                }.flatten().toSet()

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun accountsPortfolio(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandlePortfolio::class.java).values.getPortfolioHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandlePortfolio::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.account
            }.toSet() +
                applicationContext.getBeansOfType(BasePortfolioStreamProcessor::class.java).values.map {
                    it.accounts
                }.flatten().toSet()

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun accountsPositions(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandlePosition::class.java).values.getPositionHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandlePosition::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.account
            }.toSet() +
                applicationContext.getBeansOfType(BasePositionsStreamProcessor::class.java).values.map {
                    it.accounts
                }.flatten().toSet()

    @Bean("instrumentsTrades")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["instrumentsTrades"])
    fun instrumentsTradesReadonly(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleTrade::class.java).values.getTradesHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleTrade::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleTrades()
            }.toSet() +
                applicationContext.getBeansOfType(BaseTradeStreamProcessor::class.java).values.map {
                    it.extractInstruments(newTickerToUidMap)
                }.flatten().toSet()

    @Bean("accountsPortfolio")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["accountsPortfolio"])
    fun accountsPortfolioReadonly(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandlePortfolio::class.java).values.getPortfolioHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandlePortfolio::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.account
            }.toSet() +
                applicationContext.getBeansOfType(BasePortfolioStreamProcessor::class.java).values.map {
                    it.accounts
                }.flatten().toSet()

    @Bean("accountsPositions")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["accountsPositions"])
    fun accountsPositionsReadonly(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandlePosition::class.java).values.getPositionHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandlePosition::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.account
            }.toSet() +
                applicationContext.getBeansOfType(BasePositionsStreamProcessor::class.java).values.map {
                    it.accounts
                }.flatten().toSet()

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun accountsOrders(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleOrder::class.java).values.getOrderHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleOrder::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.account
            }.toSet() +
                applicationContext.getBeansOfType(BaseOrdersStreamProcessor::class.java).values.map {
                    it.accounts
                }.flatten().toSet()

    @Bean("accountsOrders")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["accountsOrders"])
    fun accountsOrdersReadonly(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleOrder::class.java).values.getOrderHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleOrder::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.account
            }.toSet() +
                applicationContext.getBeansOfType(BaseOrdersStreamProcessor::class.java).values.map {
                    it.accounts
                }.flatten().toSet()

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsTradesSandbox(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleTrade::class.java).values.getTradesHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleTrade::class.java)?.takeIf { annotation ->
                    annotation.sandboxOnly
                }?.extractInstrumentFromHandleTrades()
            }.toSet() +
                applicationContext.getBeansOfType(BaseTradeStreamProcessor::class.java).values.map {
                    it.extractInstruments(newTickerToUidMap)
                }.flatten().toSet()

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsTradingStatusSandbox(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values.getTradingStatusHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleTradingStatus::class.java)?.takeIf { annotation ->
                    annotation.sandboxOnly
                }?.extractInstrumentFromHandleTradingStatus()
            }.toSet() +
                applicationContext.getBeansOfType(BaseTradingStatusStreamProcessor::class.java).values.map {
                    it.extractInstruments(newTickerToUidMap)
                }.flatten().toSet()


    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsOrderBookSandbox(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values.getOrderBookHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleOrderBook::class.java)?.takeIf { annotation ->
                    annotation.sandboxOnly
                }?.extractInstrumentFromHandleOrderBook()
            }.toSet() +
                applicationContext.getBeansOfType(BaseOrderBookStreamProcessor::class.java).values.map {
                    it.extractInstruments(newTickerToUidMap)
                }.flatten().toSet()


    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsLastPriceSandbox(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values.getLastPriceHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleLastPrice::class.java)?.takeIf { annotation ->
                    annotation.sandboxOnly
                }?.extractInstrumentFromHandleLastPrice()
            }.toSet() +
                applicationContext.getBeansOfType(BaseLastPriceStreamProcessor::class.java).values.map {
                    it.extractInstruments(newTickerToUidMap)
                }.flatten().toSet()

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsCandleSandbox(): MutableMap<SubscriptionInterval, MutableList<InstrumentIdToWaitingClose>> {
        val result = mutableMapOf<SubscriptionInterval, MutableList<InstrumentIdToWaitingClose>>()
        val candleBookHandlers =
            applicationContext.getBeansWithAnnotation(HandleCandle::class.java).values.getCandleHandlers()
        applicationContext.getBeansOfType(BaseCandleStreamProcessor::class.java).values.map {
            it.extractInstruments(newTickerToUidMap)
        }.forEach { extractInstrumentFromCandle ->
            extractInstrumentFromCandle.second.forEach {
                result[extractInstrumentFromCandle.first]?.add(it)
                    ?: run {
                        result[extractInstrumentFromCandle.first] = mutableListOf(it)
                    }
            }
        }
        candleBookHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleCandle::class.java)
            if (annotation.sandboxOnly) {
                val extractInstrumentFromCandle = annotation.extractInstrumentFromCandle()
                result[extractInstrumentFromCandle.first]?.add(extractInstrumentFromCandle.second)
                    ?: run {
                        result[extractInstrumentFromCandle.first] = mutableListOf(extractInstrumentFromCandle.second)
                    }
            }
        }
        return result
    }

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun accountsPortfolioSandbox(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandlePortfolio::class.java).values.getPortfolioHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandlePortfolio::class.java)?.takeIf { annotation ->
                    annotation.sandboxOnly
                }?.account
            }.toSet() +
                applicationContext.getBeansOfType(BasePortfolioStreamProcessor::class.java).values.map {
                    it.accounts
                }.flatten().toSet()

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun accountsPositionsSandbox(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandlePosition::class.java).values.getPositionHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandlePosition::class.java)?.takeIf { annotation ->
                    annotation.sandboxOnly
                }?.account
            }.toSet() +
                applicationContext.getBeansOfType(BasePositionsStreamProcessor::class.java).values.map {
                    it.accounts
                }.flatten().toSet()

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun accountsOrdersSandbox(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleOrder::class.java).values.getOrderHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleOrder::class.java)?.takeIf { annotation ->
                    annotation.sandboxOnly
                }?.account
            }.toSet() +
                applicationContext.getBeansOfType(BaseOrdersStreamProcessor::class.java).values.map {
                    it.accounts
                }.flatten().toSet()


    private fun HandleTrade.extractInstrumentFromHandleTrades(): String = when {
        figi.isNotBlank() -> figi
        instrumentUid.isNotBlank() -> instrumentUid
        ticker.isNotBlank() -> tickerToUidMap[ticker]!!
        else -> throw IllegalStateException("At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided")
    }

    private fun HandleOrderBook.extractInstrumentFromHandleOrderBook(): String = when {
        figi.isNotBlank() -> figi
        instrumentUid.isNotBlank() -> instrumentUid
        ticker.isNotBlank() -> tickerToUidMap[ticker]!!
        else -> throw IllegalStateException("At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided")
    }

    private fun HandleTradingStatus.extractInstrumentFromHandleTradingStatus(): String = when {
        figi.isNotBlank() -> figi
        instrumentUid.isNotBlank() -> instrumentUid
        ticker.isNotBlank() -> tickerToUidMap[ticker]!!
        else -> throw IllegalStateException("At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided")
    }

    private fun HandleLastPrice.extractInstrumentFromHandleLastPrice(): String = when {
        figi.isNotBlank() -> figi
        instrumentUid.isNotBlank() -> instrumentUid
        ticker.isNotBlank() -> tickerToUidMap[ticker]!!
        else -> throw IllegalStateException("At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided")
    }

    private fun HandleCandle.extractInstrumentFromCandle(): Pair<SubscriptionInterval, InstrumentIdToWaitingClose> {
        if (subscriptionInterval != SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE && waitClose) {
            logger.warn { "\'subscriptionInterval\':$subscriptionInterval and \'waitClose\': true not supported" }
        }
        return when {
            figi.isNotBlank() -> subscriptionInterval to InstrumentIdToWaitingClose(figi, waitClose)
            instrumentUid.isNotBlank() -> subscriptionInterval to InstrumentIdToWaitingClose(instrumentUid, waitClose)
            ticker.isNotBlank() -> {
                subscriptionInterval to InstrumentIdToWaitingClose(tickerToUidMap[ticker]!!, waitClose)
            }

            else -> throw IllegalStateException("At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided")
        }
    }

    data class InstrumentIdToWaitingClose(
        val instrumentId: String,
        val waitingClose: Boolean
    )

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

    private companion object : KLogging()
}