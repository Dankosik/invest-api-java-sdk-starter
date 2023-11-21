package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.annotation.marketdata.HandleCandle
import io.github.dankosik.starter.invest.annotation.marketdata.HandleLastPrice
import io.github.dankosik.starter.invest.annotation.marketdata.HandleOrderBook
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTrades
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTradingStatus
import io.github.dankosik.starter.invest.annotation.operation.HandlePortfolio
import io.github.dankosik.starter.invest.annotation.operation.HandlePositions
import io.github.dankosik.starter.invest.annotation.order.HandleOrders
import io.github.dankosik.starter.invest.contract.candle.AsyncCandleHandler
import io.github.dankosik.starter.invest.contract.candle.BlockingCandleHandler
import io.github.dankosik.starter.invest.contract.candle.CoroutineCandleHandler
import io.github.dankosik.starter.invest.contract.lastprice.AsyncLastPriceHandler
import io.github.dankosik.starter.invest.contract.lastprice.BlockingLastPriceHandler
import io.github.dankosik.starter.invest.contract.lastprice.CoroutineLastPriceHandler
import io.github.dankosik.starter.invest.contract.orderbook.AsyncOrderBookHandler
import io.github.dankosik.starter.invest.contract.orderbook.BlockingOrderBookHandler
import io.github.dankosik.starter.invest.contract.orderbook.CoroutineOrderBookHandler
import io.github.dankosik.starter.invest.contract.portfolio.AsyncPortfolioHandler
import io.github.dankosik.starter.invest.contract.portfolio.BlockingPortfolioHandler
import io.github.dankosik.starter.invest.contract.portfolio.CoroutinePortfolioHandler
import io.github.dankosik.starter.invest.contract.positions.AsyncPositionsHandler
import io.github.dankosik.starter.invest.contract.positions.BlockingPositionsHandler
import io.github.dankosik.starter.invest.contract.positions.CoroutinePositionsHandler
import io.github.dankosik.starter.invest.contract.status.AsyncTradingStatusHandler
import io.github.dankosik.starter.invest.contract.status.BlockingTradingStatusHandler
import io.github.dankosik.starter.invest.contract.status.CoroutineTradingStatusHandler
import io.github.dankosik.starter.invest.contract.trade.AsyncTradesHandler
import io.github.dankosik.starter.invest.contract.trade.BlockingTradesHandler
import io.github.dankosik.starter.invest.contract.trade.CoroutineTradesHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

@Configuration
class InstrumentsAutoConfiguration(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>
) {

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsOrderBook(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values
        val orderBookHandlers = annotatedBeans.filterIsInstance<CoroutineOrderBookHandler>() +
                annotatedBeans.filterIsInstance<AsyncOrderBookHandler>() +
                annotatedBeans.filterIsInstance<BlockingOrderBookHandler>()

        orderBookHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleOrderBook::class.java)
            if (!annotation.sandboxOnly) {
                result.add(annotation.extractInstrumentFromHandleOrderBook())
            }
        }
        return result
    }


    @Bean("instrumentsOrderBook")
    @ConditionalOnMissingBean(name = ["instrumentsOrderBook"])
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    fun instrumentsOrderBookReadonly(): MutableSet<String> {
        val result = mutableSetOf<String>()

        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values
        val orderBookHandlers = annotatedBeans.filterIsInstance<CoroutineOrderBookHandler>() +
                annotatedBeans.filterIsInstance<AsyncOrderBookHandler>() +
                annotatedBeans.filterIsInstance<BlockingOrderBookHandler>()
        orderBookHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleOrderBook::class.java)
            if (!annotation.sandboxOnly) {
                result.add(annotation.extractInstrumentFromHandleOrderBook())
            }
        }
        return result
    }

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsTradingStatus(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values
        val tradingStatusHandlers = annotatedBeans.filterIsInstance<CoroutineTradingStatusHandler>() +
                annotatedBeans.filterIsInstance<AsyncTradingStatusHandler>() +
                annotatedBeans.filterIsInstance<BlockingTradingStatusHandler>()

        tradingStatusHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleTradingStatus::class.java)
            if (!annotation.sandboxOnly) {
                result.add(annotation.extractInstrumentFromHandleTradingStatus())
            }
        }
        return result
    }


    @Bean("instrumentsTradingStatus")
    @ConditionalOnMissingBean(name = ["instrumentsTradingStatus"])
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    fun instrumentsTradingStatusReadonly(): MutableSet<String> {
        val result = mutableSetOf<String>()

        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values
        val tradingStatusHandlers = annotatedBeans.filterIsInstance<CoroutineTradingStatusHandler>() +
                annotatedBeans.filterIsInstance<AsyncTradingStatusHandler>() +
                annotatedBeans.filterIsInstance<BlockingTradingStatusHandler>()
        tradingStatusHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleTradingStatus::class.java)
            if (!annotation.sandboxOnly) {
                result.add(annotation.extractInstrumentFromHandleTradingStatus())
            }
        }
        return result
    }

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsLastPrice(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values
        val lastPriceBookHandlers = annotatedBeans.filterIsInstance<CoroutineLastPriceHandler>() +
                annotatedBeans.filterIsInstance<AsyncLastPriceHandler>() +
                annotatedBeans.filterIsInstance<BlockingLastPriceHandler>()

        lastPriceBookHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleLastPrice::class.java)
            if (!annotation.sandboxOnly) {
                result.add(annotation.extractInstrumentFromHandleLastPrice())
            }
        }
        return result
    }


    @Bean("instrumentsLastPrice")
    @ConditionalOnMissingBean(name = ["instrumentsLastPrice"])
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    fun instrumentsLastPriceReadonly(): MutableSet<String> {
        val result = mutableSetOf<String>()

        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values
        val lastPriceBookHandlers = annotatedBeans.filterIsInstance<CoroutineLastPriceHandler>() +
                annotatedBeans.filterIsInstance<AsyncLastPriceHandler>() +
                annotatedBeans.filterIsInstance<BlockingLastPriceHandler>()
        lastPriceBookHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleLastPrice::class.java)
            if (!annotation.sandboxOnly) {
                result.add(annotation.extractInstrumentFromHandleLastPrice())
            }
        }
        return result
    }

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun instrumentsCandle(): MutableMap<SubscriptionInterval, MutableList<String>> {
        val result = mutableMapOf<SubscriptionInterval, MutableList<String>>()
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
    fun instrumentsCandleReadonly(): MutableMap<SubscriptionInterval, MutableList<String>> {
        val result = mutableMapOf<SubscriptionInterval, MutableList<String>>()
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
    fun instrumentsTrades(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTrades::class.java).values
        val tradesHandlers = annotatedBeans.filterIsInstance<AsyncTradesHandler>() +
                annotatedBeans.filterIsInstance<CoroutineTradesHandler>() +
                annotatedBeans.filterIsInstance<BlockingTradesHandler>()
        tradesHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleTrades::class.java)
            if (!annotation.sandboxOnly) {
                result.add(annotation.extractInstrumentFromHandleTrades())
            }
        }
        return result
    }

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun accountsPortfolio(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePortfolio::class.java).values
        val portfolioHandlers = annotatedBeans.filterIsInstance<CoroutinePortfolioHandler>() +
                annotatedBeans.filterIsInstance<AsyncPortfolioHandler>() +
                annotatedBeans.filterIsInstance<BlockingPortfolioHandler>()
        portfolioHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandlePortfolio::class.java)
            if (!annotation.sandboxOnly) {
                result.add(annotation.account)
            }
        }
        return result
    }

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun accountsPositions(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePositions::class.java).values
        val positionsHandlers = annotatedBeans.filterIsInstance<CoroutinePositionsHandler>() +
                annotatedBeans.filterIsInstance<AsyncPositionsHandler>() +
                annotatedBeans.filterIsInstance<BlockingPositionsHandler>()
        positionsHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandlePositions::class.java)
            if (!annotation.sandboxOnly) {
                result.add(annotation.account)
            }
        }
        return result
    }

    @Bean("instrumentsTrades")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["instrumentsTrades"])
    fun instrumentsTradesReadonly(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTrades::class.java).values
        val tradesHandlers = annotatedBeans.filterIsInstance<AsyncTradesHandler>() +
                annotatedBeans.filterIsInstance<CoroutineTradesHandler>() +
                annotatedBeans.filterIsInstance<BlockingTradesHandler>()
        tradesHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleTrades::class.java)
            if (!annotation.sandboxOnly) {
                result.add(annotation.extractInstrumentFromHandleTrades())
            }
        }
        return result
    }

    @Bean("accountsPortfolio")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["accountsPortfolio"])
    fun accountsPortfolioReadonly(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePortfolio::class.java).values
        val portfolioHandlers = annotatedBeans.filterIsInstance<CoroutinePortfolioHandler>() +
                annotatedBeans.filterIsInstance<AsyncPortfolioHandler>() +
                annotatedBeans.filterIsInstance<BlockingPortfolioHandler>()
        portfolioHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandlePortfolio::class.java)
            if (!annotation.sandboxOnly) {
                result.add(annotation.account)
            }
        }
        return result
    }

    @Bean("accountsPositions")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["accountsPositions"])
    fun accountsPositionsReadonly(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePositions::class.java).values
        val positionsHandlers = annotatedBeans.filterIsInstance<CoroutinePositionsHandler>() +
                annotatedBeans.filterIsInstance<AsyncPositionsHandler>() +
                annotatedBeans.filterIsInstance<BlockingPositionsHandler>()
        positionsHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandlePositions::class.java)
            if (!annotation.sandboxOnly) {
                result.add(annotation.account)
            }
        }
        return result
    }

    @Bean
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.fullAccess"])
    fun accountsOrders(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrders::class.java).values
        val ordersHandlers = annotatedBeans.filterIsInstance<CoroutineOrderBookHandler>() +
                annotatedBeans.filterIsInstance<AsyncOrderBookHandler>() +
                annotatedBeans.filterIsInstance<BlockingOrderBookHandler>()
        ordersHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleOrders::class.java)
            if (!annotation.sandboxOnly) {
                result.add(annotation.account)
            }
        }
        return result
    }

    @Bean("accountsOrders")
    @ConditionalOnProperty(value = ["tinkoff.starter.apiToken.readonly"])
    @ConditionalOnMissingBean(name = ["accountsOrders"])
    fun accountsOrdersReadonly(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrders::class.java).values
        val ordersHandlers = annotatedBeans.filterIsInstance<CoroutineOrderBookHandler>() +
                annotatedBeans.filterIsInstance<AsyncOrderBookHandler>() +
                annotatedBeans.filterIsInstance<BlockingOrderBookHandler>()
        ordersHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleOrders::class.java)
            if (!annotation.sandboxOnly) {
                result.add(annotation.account)
            }
        }
        return result
    }

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsTradesSandbox(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTrades::class.java).values
        val tradesHandlers = annotatedBeans.filterIsInstance<AsyncTradesHandler>() +
                annotatedBeans.filterIsInstance<CoroutineTradesHandler>() +
                annotatedBeans.filterIsInstance<BlockingTradesHandler>()
        tradesHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleTrades::class.java)
            if (annotation.sandboxOnly) {
                result.add(annotation.extractInstrumentFromHandleTrades())
            }
        }

        return result
    }

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsTradingStatusSandbox(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values
        val tradingStatusHandlers = annotatedBeans.filterIsInstance<CoroutineTradingStatusHandler>() +
                annotatedBeans.filterIsInstance<AsyncTradingStatusHandler>() +
                annotatedBeans.filterIsInstance<BlockingTradingStatusHandler>()
        tradingStatusHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleTradingStatus::class.java)
            if (annotation.sandboxOnly) {
                result.add(annotation.extractInstrumentFromHandleTradingStatus())
            }
        }
        return result
    }


    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsOrderBookSandbox(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values
        val orderBookHandlers = annotatedBeans.filterIsInstance<CoroutineOrderBookHandler>() +
                annotatedBeans.filterIsInstance<AsyncOrderBookHandler>() +
                annotatedBeans.filterIsInstance<BlockingOrderBookHandler>()
        orderBookHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleOrderBook::class.java)
            if (annotation.sandboxOnly) {
                result.add(annotation.extractInstrumentFromHandleOrderBook())
            }
        }
        return result
    }


    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsLastPriceSandbox(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values
        val lastPriceBookHandlers = annotatedBeans.filterIsInstance<CoroutineLastPriceHandler>() +
                annotatedBeans.filterIsInstance<AsyncLastPriceHandler>() +
                annotatedBeans.filterIsInstance<BlockingLastPriceHandler>()
        lastPriceBookHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleLastPrice::class.java)
            if (annotation.sandboxOnly) {
                result.add(annotation.extractInstrumentFromHandleLastPrice())
            }
        }
        return result
    }

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun instrumentsCandleSandbox(): MutableMap<SubscriptionInterval, MutableList<String>> {
        val result = mutableMapOf<SubscriptionInterval, MutableList<String>>()
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
    fun accountsPortfolioSandbox(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePortfolio::class.java).values
        val portfolioHandlers = annotatedBeans.filterIsInstance<CoroutinePortfolioHandler>() +
                annotatedBeans.filterIsInstance<AsyncPortfolioHandler>() +
                annotatedBeans.filterIsInstance<BlockingPortfolioHandler>()
        portfolioHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandlePortfolio::class.java)
            if (annotation.sandboxOnly) {
                result.add(annotation.account)
            }
        }
        return result
    }

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun accountsPositionsSandbox(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePositions::class.java).values
        val positionsHandlers = annotatedBeans.filterIsInstance<CoroutinePositionsHandler>() +
                annotatedBeans.filterIsInstance<AsyncPositionsHandler>() +
                annotatedBeans.filterIsInstance<BlockingPositionsHandler>()
        positionsHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandlePositions::class.java)
            if (annotation.sandboxOnly) {
                result.add(annotation.account)
            }
        }
        return result
    }

    @Bean
    @ConditionalOnProperty(name = ["tinkoff.starter.apiToken.sandbox"])
    fun accountsOrdersSandbox(): MutableSet<String> {
        val result = mutableSetOf<String>()
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrders::class.java).values
        val ordersHandlers = annotatedBeans.filterIsInstance<CoroutineOrderBookHandler>() +
                annotatedBeans.filterIsInstance<AsyncOrderBookHandler>() +
                annotatedBeans.filterIsInstance<BlockingOrderBookHandler>()
        ordersHandlers.forEach { bean ->
            val annotation = bean.javaClass.getAnnotation(HandleOrders::class.java)
            if (annotation.sandboxOnly) {
                result.add(annotation.account)
            }
        }
        return result
    }


    private fun HandleTrades.extractInstrumentFromHandleTrades(): String {
        return if (figi.isNotBlank()) figi
        else if (instrumentUid.isNotBlank()) instrumentUid
        else if (ticker.isNotBlank()) tickerToUidMap[ticker]!!
        else throw IllegalStateException("At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided")
    }

    private fun HandleOrderBook.extractInstrumentFromHandleOrderBook(): String {
        return if (figi.isNotBlank()) figi
        else if (instrumentUid.isNotBlank()) instrumentUid
        else if (ticker.isNotBlank()) tickerToUidMap[ticker]!!
        else throw IllegalStateException("At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided")
    }

    private fun HandleTradingStatus.extractInstrumentFromHandleTradingStatus(): String {
        return if (figi.isNotBlank()) figi
        else if (instrumentUid.isNotBlank()) instrumentUid
        else if (ticker.isNotBlank()) tickerToUidMap[ticker]!!
        else throw IllegalStateException("At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided")
    }

    private fun HandleLastPrice.extractInstrumentFromHandleLastPrice(): String {
        return if (figi.isNotBlank()) figi
        else if (instrumentUid.isNotBlank()) instrumentUid
        else if (ticker.isNotBlank()) tickerToUidMap[ticker]!!
        else throw IllegalStateException("At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided")
    }

    private fun HandleCandle.extractInstrumentFromCandle(): Pair<SubscriptionInterval, String> {
        return if (figi.isNotBlank()) subscriptionInterval to figi
        else if (instrumentUid.isNotBlank()) subscriptionInterval to instrumentUid
        else if (ticker.isNotBlank()) subscriptionInterval to tickerToUidMap[ticker]!!
        else throw IllegalStateException("At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided")
    }
}