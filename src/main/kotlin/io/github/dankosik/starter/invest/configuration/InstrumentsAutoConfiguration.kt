package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.annotation.marketdata.*
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
import mu.KLogging
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(name = ["tickerToUidMap"])
class InstrumentsAutoConfiguration(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>
) {

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsOrderBook(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values.getOrderBookHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleOrderBook::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleOrderBook()
            }.toSet()

    @Bean("instrumentsOrderBook")
    @ConditionalOnMissingBean(name = ["instrumentsOrderBook"])
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    fun instrumentsOrderBookReadonly(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values.getOrderBookHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleOrderBook::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleOrderBook()
            }.toSet()

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsTradingStatus(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values.getTradingStatusHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleTradingStatus::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleTradingStatus()
            }.toSet()


    @Bean("instrumentsTradingStatus")
    @ConditionalOnMissingBean(name = ["instrumentsTradingStatus"])
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    fun instrumentsTradingStatusReadonly(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values.getTradingStatusHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleTradingStatus::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleTradingStatus()
            }.toSet()

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsLastPrice(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values.getLastPriceHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleLastPrice::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleLastPrice()
            }.toSet()


    @Bean("instrumentsLastPrice")
    @ConditionalOnMissingBean(name = ["instrumentsLastPrice"])
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    fun instrumentsLastPriceReadonly(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values.getLastPriceHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleLastPrice::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleLastPrice()
            }.toSet()

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsCandle(): MutableMap<SubscriptionInterval, MutableList<InstrumentIdToWaitingClose>> {
        val result = mutableMapOf<SubscriptionInterval, MutableList<InstrumentIdToWaitingClose>>()
        val candleBookHandlers =
            applicationContext.getBeansWithAnnotation(HandleCandle::class.java).values.getCandleHandlers()
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
            }.toSet()

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun accountsPortfolio(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandlePortfolio::class.java).values.getPortfolioHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandlePortfolio::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.account
            }.toSet()

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun accountsPositions(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandlePosition::class.java).values.getPositionHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandlePosition::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.account
            }.toSet()

    @Bean("instrumentsTrades")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["instrumentsTrades"])
    fun instrumentsTradesReadonly(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleTrade::class.java).values.getTradesHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleTrade::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.extractInstrumentFromHandleTrades()
            }.toSet()

    @Bean("accountsPortfolio")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["accountsPortfolio"])
    fun accountsPortfolioReadonly(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandlePortfolio::class.java).values.getPortfolioHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandlePortfolio::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.account
            }.toSet()

    @Bean("accountsPositions")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["accountsPositions"])
    fun accountsPositionsReadonly(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandlePosition::class.java).values.getPositionHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandlePosition::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.account
            }.toSet()

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun accountsOrders(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleOrder::class.java).values.getOrderHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleOrder::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.account
            }.toSet()

    @Bean("accountsOrders")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["accountsOrders"])
    fun accountsOrdersReadonly(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleOrder::class.java).values.getOrderHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleOrder::class.java)?.takeIf { annotation ->
                    !annotation.sandboxOnly
                }?.account
            }.toSet()

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsTradesSandbox(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleTrade::class.java).values.getTradesHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleTrade::class.java)?.takeIf { annotation ->
                    annotation.sandboxOnly
                }?.extractInstrumentFromHandleTrades()
            }.toSet()

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsTradingStatusSandbox(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values.getTradingStatusHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleTradingStatus::class.java)?.takeIf { annotation ->
                    annotation.sandboxOnly
                }?.extractInstrumentFromHandleTradingStatus()
            }.toSet()


    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsOrderBookSandbox(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values.getOrderBookHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleOrderBook::class.java)?.takeIf { annotation ->
                    annotation.sandboxOnly
                }?.extractInstrumentFromHandleOrderBook()
            }.toSet()


    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsLastPriceSandbox(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values.getLastPriceHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleLastPrice::class.java)?.takeIf { annotation ->
                    annotation.sandboxOnly
                }?.extractInstrumentFromHandleLastPrice()
            }.toSet()

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsCandleSandbox(): MutableMap<SubscriptionInterval, MutableList<InstrumentIdToWaitingClose>> {
        val result = mutableMapOf<SubscriptionInterval, MutableList<InstrumentIdToWaitingClose>>()
        val candleBookHandlers =
            applicationContext.getBeansWithAnnotation(HandleCandle::class.java).values.getCandleHandlers()
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
            }.toSet()

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun accountsPositionsSandbox(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandlePosition::class.java).values.getPositionHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandlePosition::class.java)?.takeIf { annotation ->
                    annotation.sandboxOnly
                }?.account
            }.toSet()

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun accountsOrdersSandbox(): Set<String> =
        applicationContext.getBeansWithAnnotation(HandleOrder::class.java).values.getOrderHandlers()
            .mapNotNull {
                it.javaClass.getAnnotation(HandleOrder::class.java)?.takeIf { annotation ->
                    annotation.sandboxOnly
                }?.account
            }.toSet()


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

    private companion object : KLogging()
}