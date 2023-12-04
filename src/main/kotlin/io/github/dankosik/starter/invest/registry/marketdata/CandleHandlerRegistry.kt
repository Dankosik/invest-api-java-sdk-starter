package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleCandle
import io.github.dankosik.starter.invest.contract.candle.AsyncCandleHandler
import io.github.dankosik.starter.invest.contract.candle.BaseCandleHandler
import io.github.dankosik.starter.invest.contract.candle.BlockingCandleHandler
import io.github.dankosik.starter.invest.contract.candle.CoroutineCandleHandler
import mu.KLogging
import org.springframework.context.ApplicationContext
import ru.tinkoff.piapi.contract.v1.Candle
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

internal class CandleHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private val handlersByFigi = HashMap<SubscriptionInterval, Map<String, BaseCandleHandler>>()
    private val handlersByInstrumentUid = HashMap<SubscriptionInterval, Map<String, BaseCandleHandler>>()


    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleCandle::class.java).values
        val coroutineHandlers = annotatedBeans.filterIsInstance<CoroutineCandleHandler>()
        val blockingHandlers = annotatedBeans.filterIsInstance<BlockingCandleHandler>()
        val asyncHandlers = annotatedBeans.filterIsInstance<AsyncCandleHandler>()
        blockingHandlers.forEach { it.addIntervalToHandlerMap() }
        coroutineHandlers.forEach { it.addIntervalToHandlerMap() }
        asyncHandlers.forEach { it.addIntervalToHandlerMap() }
    }

    fun getHandler(candle: Candle) = getHandlerByUidAndInterval(candle.instrumentUid, candle.interval)
        ?: getHandlerByFigiAndInterval(candle.figi, candle.interval)

    fun getHandlerByUidAndInterval(uId: String?, subscriptionInterval: SubscriptionInterval): BaseCandleHandler? =
        handlersByInstrumentUid[subscriptionInterval]?.get(uId)

    fun getHandlerByFigiAndInterval(figi: String?, subscriptionInterval: SubscriptionInterval): BaseCandleHandler? =
        handlersByFigi[subscriptionInterval]?.get(figi)


    private fun BaseCandleHandler.addIntervalToHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleCandle::class.java)
        val figi = annotation.figi
        val instrumentUid = annotation.instrumentUid
        val subscriptionInterval = annotation.subscriptionInterval
        if (figi.isNotBlank()) {
            handlersByFigi[subscriptionInterval] = mapOf(figi to this)
        } else if (instrumentUid.isNotBlank()) {
            handlersByInstrumentUid[subscriptionInterval] = mapOf(instrumentUid to this)
        } else {
            val ticker = annotation.ticker
            if (ticker.isNotBlank()) {
                val uId = tickerToUidMap[ticker]!!
                handlersByInstrumentUid[subscriptionInterval] = mapOf(uId to this)
            }
        }
    }

    private companion object : KLogging()
}