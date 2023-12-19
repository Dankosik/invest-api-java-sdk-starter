package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.annotation.marketdata.HandleCandle
import io.github.dankosik.starter.invest.annotation.marketdata.HandleLastPrice
import io.github.dankosik.starter.invest.annotation.marketdata.HandleOrderBook
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTrade
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTradingStatus
import io.github.dankosik.starter.invest.annotation.operation.HandlePortfolio
import io.github.dankosik.starter.invest.annotation.operation.HandlePosition
import io.github.dankosik.starter.invest.annotation.order.HandleOrder
import io.github.dankosik.starter.invest.contract.marketdata.candle.AsyncCandleHandler
import io.github.dankosik.starter.invest.contract.marketdata.candle.BlockingCandleHandler
import io.github.dankosik.starter.invest.contract.marketdata.candle.CoroutineCandleHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.AsyncLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.BlockingLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.CoroutineLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.AsyncOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.BlockingOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.CoroutineOrderBookHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.AsyncPortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.BlockingPortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.CoroutinePortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.positions.AsyncPositionHandler
import io.github.dankosik.starter.invest.contract.operation.positions.BlockingPositionHandler
import io.github.dankosik.starter.invest.contract.operation.positions.CoroutinePositionHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.AsyncTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.BlockingTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.CoroutineTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.AsyncTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.BlockingTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.CoroutineTradeHandler
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
    fun instrumentsOrderBook(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values
        val orderBookHandlers = annotatedBeans.filterIsInstance<CoroutineOrderBookHandler>() +
                annotatedBeans.filterIsInstance<AsyncOrderBookHandler>() +
                annotatedBeans.filterIsInstance<BlockingOrderBookHandler>()
        return orderBookHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleOrderBook::class.java)?.takeIf { annotation ->
                !annotation.sandboxOnly
            }?.extractInstrumentFromHandleOrderBook()
        }.toSet()
    }

    @Bean("instrumentsOrderBook")
    @ConditionalOnMissingBean(name = ["instrumentsOrderBook"])
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    fun instrumentsOrderBookReadonly(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values
        val orderBookHandlers = annotatedBeans.filterIsInstance<CoroutineOrderBookHandler>() +
                annotatedBeans.filterIsInstance<AsyncOrderBookHandler>() +
                annotatedBeans.filterIsInstance<BlockingOrderBookHandler>()
        return orderBookHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleOrderBook::class.java)?.takeIf { annotation ->
                !annotation.sandboxOnly
            }?.extractInstrumentFromHandleOrderBook()
        }.toSet()
    }

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsTradingStatus(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values
        val tradingStatusHandlers = annotatedBeans.filterIsInstance<CoroutineTradingStatusHandler>() +
                annotatedBeans.filterIsInstance<AsyncTradingStatusHandler>() +
                annotatedBeans.filterIsInstance<BlockingTradingStatusHandler>()
        return tradingStatusHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleTradingStatus::class.java)?.takeIf { annotation ->
                !annotation.sandboxOnly
            }?.extractInstrumentFromHandleTradingStatus()
        }.toSet()
    }


    @Bean("instrumentsTradingStatus")
    @ConditionalOnMissingBean(name = ["instrumentsTradingStatus"])
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    fun instrumentsTradingStatusReadonly(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values
        val tradingStatusHandlers = annotatedBeans.filterIsInstance<CoroutineTradingStatusHandler>() +
                annotatedBeans.filterIsInstance<AsyncTradingStatusHandler>() +
                annotatedBeans.filterIsInstance<BlockingTradingStatusHandler>()
        return tradingStatusHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleTradingStatus::class.java)?.takeIf { annotation ->
                !annotation.sandboxOnly
            }?.extractInstrumentFromHandleTradingStatus()
        }.toSet()
    }

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsLastPrice(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values
        val lastPriceBookHandlers = annotatedBeans.filterIsInstance<CoroutineLastPriceHandler>() +
                annotatedBeans.filterIsInstance<AsyncLastPriceHandler>() +
                annotatedBeans.filterIsInstance<BlockingLastPriceHandler>()
        return lastPriceBookHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleLastPrice::class.java)?.takeIf { annotation ->
                !annotation.sandboxOnly
            }?.extractInstrumentFromHandleLastPrice()
        }.toSet()
    }


    @Bean("instrumentsLastPrice")
    @ConditionalOnMissingBean(name = ["instrumentsLastPrice"])
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    fun instrumentsLastPriceReadonly(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values
        val lastPriceBookHandlers = annotatedBeans.filterIsInstance<CoroutineLastPriceHandler>() +
                annotatedBeans.filterIsInstance<AsyncLastPriceHandler>() +
                annotatedBeans.filterIsInstance<BlockingLastPriceHandler>()
        return lastPriceBookHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleLastPrice::class.java)?.takeIf { annotation ->
                !annotation.sandboxOnly
            }?.extractInstrumentFromHandleLastPrice()
        }.toSet()
    }

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsCandle(): MutableMap<SubscriptionInterval, MutableList<InstrumentIdToWaitingClose>> {
        val result = mutableMapOf<SubscriptionInterval, MutableList<InstrumentIdToWaitingClose>>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleCandle::class.java).values
        val candleBookHandlers = annotatedBeans.filterIsInstance<CoroutineCandleHandler>() +
                annotatedBeans.filterIsInstance<AsyncCandleHandler>() +
                annotatedBeans.filterIsInstance<BlockingCandleHandler>()
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
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleCandle::class.java).values
        val candleBookHandlers = annotatedBeans.filterIsInstance<CoroutineCandleHandler>() +
                annotatedBeans.filterIsInstance<AsyncCandleHandler>() +
                annotatedBeans.filterIsInstance<BlockingCandleHandler>()
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
    fun instrumentsTrades(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTrade::class.java).values
        val tradesHandlers = annotatedBeans.filterIsInstance<AsyncTradeHandler>() +
                annotatedBeans.filterIsInstance<CoroutineTradeHandler>() +
                annotatedBeans.filterIsInstance<BlockingTradeHandler>()
        return tradesHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleTrade::class.java)?.takeIf { annotation ->
                !annotation.sandboxOnly
            }?.extractInstrumentFromHandleTrades()
        }.toSet()
    }

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun accountsPortfolio(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePortfolio::class.java).values
        val portfolioHandlers = annotatedBeans.filterIsInstance<CoroutinePortfolioHandler>() +
                annotatedBeans.filterIsInstance<AsyncPortfolioHandler>() +
                annotatedBeans.filterIsInstance<BlockingPortfolioHandler>()
        return portfolioHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandlePortfolio::class.java)?.takeIf { annotation ->
                !annotation.sandboxOnly
            }?.account
        }.toSet()
    }

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun accountsPositions(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePosition::class.java).values
        val positionsHandlers = annotatedBeans.filterIsInstance<CoroutinePositionHandler>() +
                annotatedBeans.filterIsInstance<AsyncPositionHandler>() +
                annotatedBeans.filterIsInstance<BlockingPositionHandler>()
        return positionsHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandlePosition::class.java)?.takeIf { annotation ->
                !annotation.sandboxOnly
            }?.account
        }.toSet()
    }

    @Bean("instrumentsTrades")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["instrumentsTrades"])
    fun instrumentsTradesReadonly(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTrade::class.java).values
        val tradesHandlers = annotatedBeans.filterIsInstance<AsyncTradeHandler>() +
                annotatedBeans.filterIsInstance<CoroutineTradeHandler>() +
                annotatedBeans.filterIsInstance<BlockingTradeHandler>()
        return tradesHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleTrade::class.java)?.takeIf { annotation ->
                !annotation.sandboxOnly
            }?.extractInstrumentFromHandleTrades()
        }.toSet()
    }

    @Bean("accountsPortfolio")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["accountsPortfolio"])
    fun accountsPortfolioReadonly(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePortfolio::class.java).values
        val portfolioHandlers = annotatedBeans.filterIsInstance<CoroutinePortfolioHandler>() +
                annotatedBeans.filterIsInstance<AsyncPortfolioHandler>() +
                annotatedBeans.filterIsInstance<BlockingPortfolioHandler>()
        return portfolioHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandlePortfolio::class.java)?.takeIf { annotation ->
                !annotation.sandboxOnly
            }?.account
        }.toSet()
    }

    @Bean("accountsPositions")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["accountsPositions"])
    fun accountsPositionsReadonly(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePosition::class.java).values
        val positionsHandlers = annotatedBeans.filterIsInstance<CoroutinePositionHandler>() +
                annotatedBeans.filterIsInstance<AsyncPositionHandler>() +
                annotatedBeans.filterIsInstance<BlockingPositionHandler>()
        return positionsHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandlePosition::class.java)?.takeIf { annotation ->
                !annotation.sandboxOnly
            }?.account
        }.toSet()
    }

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun accountsOrders(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrder::class.java).values
        val ordersHandlers = annotatedBeans.filterIsInstance<CoroutineOrderBookHandler>() +
                annotatedBeans.filterIsInstance<AsyncOrderBookHandler>() +
                annotatedBeans.filterIsInstance<BlockingOrderBookHandler>()
        return ordersHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleOrder::class.java)?.takeIf { annotation ->
                !annotation.sandboxOnly
            }?.account
        }.toSet()
    }

    @Bean("accountsOrders")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["accountsOrders"])
    fun accountsOrdersReadonly(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrder::class.java).values
        val ordersHandlers = annotatedBeans.filterIsInstance<CoroutineOrderBookHandler>() +
                annotatedBeans.filterIsInstance<AsyncOrderBookHandler>() +
                annotatedBeans.filterIsInstance<BlockingOrderBookHandler>()
        return ordersHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleOrder::class.java)?.takeIf { annotation ->
                !annotation.sandboxOnly
            }?.account
        }.toSet()
    }

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsTradesSandbox(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTrade::class.java).values
        val tradesHandlers = annotatedBeans.filterIsInstance<AsyncTradeHandler>() +
                annotatedBeans.filterIsInstance<CoroutineTradeHandler>() +
                annotatedBeans.filterIsInstance<BlockingTradeHandler>()
        return tradesHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleTrade::class.java)?.takeIf { annotation ->
                annotation.sandboxOnly
            }?.extractInstrumentFromHandleTrades()
        }.toSet()
    }

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsTradingStatusSandbox(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values
        val tradingStatusHandlers = annotatedBeans.filterIsInstance<CoroutineTradingStatusHandler>() +
                annotatedBeans.filterIsInstance<AsyncTradingStatusHandler>() +
                annotatedBeans.filterIsInstance<BlockingTradingStatusHandler>()
        return tradingStatusHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleTradingStatus::class.java)?.takeIf { annotation ->
                annotation.sandboxOnly
            }?.extractInstrumentFromHandleTradingStatus()
        }.toSet()
    }


    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsOrderBookSandbox(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values
        val orderBookHandlers = annotatedBeans.filterIsInstance<CoroutineOrderBookHandler>() +
                annotatedBeans.filterIsInstance<AsyncOrderBookHandler>() +
                annotatedBeans.filterIsInstance<BlockingOrderBookHandler>()
        return orderBookHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleOrderBook::class.java)?.takeIf { annotation ->
                annotation.sandboxOnly
            }?.extractInstrumentFromHandleOrderBook()
        }.toSet()
    }


    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsLastPriceSandbox(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values
        val lastPriceBookHandlers = annotatedBeans.filterIsInstance<CoroutineLastPriceHandler>() +
                annotatedBeans.filterIsInstance<AsyncLastPriceHandler>() +
                annotatedBeans.filterIsInstance<BlockingLastPriceHandler>()
        return lastPriceBookHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleLastPrice::class.java)?.takeIf { annotation ->
                annotation.sandboxOnly
            }?.extractInstrumentFromHandleLastPrice()
        }.toSet()
    }

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsCandleSandbox(): MutableMap<SubscriptionInterval, MutableList<InstrumentIdToWaitingClose>> {
        val result = mutableMapOf<SubscriptionInterval, MutableList<InstrumentIdToWaitingClose>>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleCandle::class.java).values
        val candleBookHandlers = annotatedBeans.filterIsInstance<CoroutineCandleHandler>() +
                annotatedBeans.filterIsInstance<AsyncCandleHandler>() +
                annotatedBeans.filterIsInstance<BlockingCandleHandler>()
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
    fun accountsPortfolioSandbox(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePortfolio::class.java).values
        val portfolioHandlers = annotatedBeans.filterIsInstance<CoroutinePortfolioHandler>() +
                annotatedBeans.filterIsInstance<AsyncPortfolioHandler>() +
                annotatedBeans.filterIsInstance<BlockingPortfolioHandler>()
        return portfolioHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandlePortfolio::class.java)?.takeIf { annotation ->
                annotation.sandboxOnly
            }?.account
        }.toSet()
    }

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun accountsPositionsSandbox(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePosition::class.java).values
        val positionsHandlers = annotatedBeans.filterIsInstance<CoroutinePositionHandler>() +
                annotatedBeans.filterIsInstance<AsyncPositionHandler>() +
                annotatedBeans.filterIsInstance<BlockingPositionHandler>()
        return positionsHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandlePosition::class.java)?.takeIf { annotation ->
                annotation.sandboxOnly
            }?.account
        }.toSet()
    }

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun accountsOrdersSandbox(): Set<String> {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrder::class.java).values
        val ordersHandlers = annotatedBeans.filterIsInstance<CoroutineOrderBookHandler>() +
                annotatedBeans.filterIsInstance<AsyncOrderBookHandler>() +
                annotatedBeans.filterIsInstance<BlockingOrderBookHandler>()
        return ordersHandlers.mapNotNull {
            it.javaClass.getAnnotation(HandleOrder::class.java)?.takeIf { annotation ->
                annotation.sandboxOnly
            }?.account
        }.toSet()
    }


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