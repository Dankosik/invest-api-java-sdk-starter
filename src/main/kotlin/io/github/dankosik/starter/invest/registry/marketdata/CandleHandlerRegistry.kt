package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllCandles
import io.github.dankosik.starter.invest.annotation.marketdata.HandleCandle
import io.github.dankosik.starter.invest.contract.marketdata.candle.BaseCandleHandler
import io.github.dankosik.starter.invest.contract.marketdata.candle.getCandleHandlers
import org.springframework.context.ApplicationContext
import ru.tinkoff.piapi.contract.v1.Candle
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

internal class CandleHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private val handlersByFigi = HashMap<SubscriptionInterval, MutableMap<String, MutableList<BaseCandleHandler>>>()
    private val handlersByInstrumentUid =
        HashMap<SubscriptionInterval, MutableMap<String, MutableList<BaseCandleHandler>>>()
    val commonHandlersBySubscription = HashMap<SubscriptionInterval, MutableList<BaseCandleHandler>>()

    init {
        applicationContext.getBeansWithAnnotation(HandleCandle::class.java).values.getCandleHandlers()
            .forEach {
                it.addIntervalToHandlerMap()
            }
        applicationContext.getBeansWithAnnotation(HandleAllCandles::class.java).values.getCandleHandlers()
            .forEach {
                it.addIntervalToAllHandlerMap()
            }
    }

    fun getHandlers(candle: Candle): MutableList<BaseCandleHandler>? =
        getHandlersByUidAndInterval(candle.instrumentUid, candle.interval)
            ?: getHandlersByFigiAndInterval(candle.figi, candle.interval)

    fun getCommonHandlers(candle: Candle): MutableList<BaseCandleHandler>? =
        commonHandlersBySubscription[candle.interval]

    private fun getHandlersByUidAndInterval(uId: String?, subscriptionInterval: SubscriptionInterval) =
        handlersByInstrumentUid[subscriptionInterval]?.get(uId)

    private fun getHandlersByFigiAndInterval(figi: String?, subscriptionInterval: SubscriptionInterval) =
        handlersByFigi[subscriptionInterval]?.get(figi)

    private fun BaseCandleHandler.addIntervalToHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleCandle::class.java)
        val figi = annotation.figi
        val instrumentUid = annotation.instrumentUid
        val subscriptionInterval = annotation.subscriptionInterval
        if (figi.isNotBlank()) {
            if (handlersByFigi[subscriptionInterval] == null) {
                handlersByFigi[subscriptionInterval] = mutableMapOf(figi to mutableListOf(this))
            } else {
                if (handlersByFigi[subscriptionInterval]!![figi] != null) {
                    handlersByFigi[subscriptionInterval]!![figi]!!.add(this)
                } else {
                    handlersByFigi[subscriptionInterval]!![figi] = mutableListOf(this)
                }
            }
        } else if (instrumentUid.isNotBlank()) {
            if (handlersByInstrumentUid[subscriptionInterval] == null) {
                handlersByInstrumentUid[subscriptionInterval] = mutableMapOf(instrumentUid to mutableListOf(this))
            } else {
                if (handlersByInstrumentUid[subscriptionInterval]!![instrumentUid] != null) {
                    handlersByInstrumentUid[subscriptionInterval]!![instrumentUid]!!.add(this)
                } else {
                    handlersByInstrumentUid[subscriptionInterval]!![instrumentUid] = mutableListOf(this)
                }
            }
        } else {
            val ticker = annotation.ticker
            if (ticker.isNotBlank()) {
                val uId = tickerToUidMap[ticker]!!
                if (handlersByInstrumentUid[subscriptionInterval] == null) {
                    handlersByInstrumentUid[subscriptionInterval] = mutableMapOf(uId to mutableListOf(this))
                } else {
                    if (handlersByInstrumentUid[subscriptionInterval]!![uId] != null) {
                        handlersByInstrumentUid[subscriptionInterval]!![uId]!!.add(this)
                    } else {
                        handlersByInstrumentUid[subscriptionInterval]!![uId] = mutableListOf(this)
                    }
                }
            }
        }
    }

    private fun BaseCandleHandler.addInstrumentIdToAllHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleAllCandles::class.java)
        val subscriptionInterval = annotation.subscriptionInterval
        annotation.figies.takeIf { it.isNotEmpty() }?.forEach { figi ->
            if (handlersByFigi[subscriptionInterval] == null) {
                handlersByFigi[subscriptionInterval] = mutableMapOf(figi to mutableListOf(this))
            } else {
                if (handlersByFigi[subscriptionInterval]!![figi] != null) {
                    handlersByFigi[subscriptionInterval]!![figi]!!.add(this)
                } else {
                    handlersByFigi[subscriptionInterval]!![figi] = mutableListOf(this)
                }
            }
        }
        annotation.instrumentsUids.takeIf { it.isNotEmpty() }?.forEach { instrumentUid ->
            if (handlersByInstrumentUid[subscriptionInterval] == null) {
                handlersByInstrumentUid[subscriptionInterval] = mutableMapOf(instrumentUid to mutableListOf(this))
            } else {
                if (handlersByInstrumentUid[subscriptionInterval]!![instrumentUid] != null) {
                    handlersByInstrumentUid[subscriptionInterval]!![instrumentUid]!!.add(this)
                } else {
                    handlersByInstrumentUid[subscriptionInterval]!![instrumentUid] = mutableListOf(this)
                }
            }
        }
        annotation.tickers.takeIf { it.isNotEmpty() }?.forEach { ticker ->
            val uId = tickerToUidMap[ticker]!!
            if (handlersByInstrumentUid[subscriptionInterval] == null) {
                handlersByInstrumentUid[subscriptionInterval] = mutableMapOf(uId to mutableListOf(this))
            } else {
                if (handlersByInstrumentUid[subscriptionInterval]!![uId] != null) {
                    handlersByInstrumentUid[subscriptionInterval]!![uId]!!.add(this)
                } else {
                    handlersByInstrumentUid[subscriptionInterval]!![uId] = mutableListOf(this)
                }
            }
        }
    }

    private fun BaseCandleHandler.addIntervalToAllHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleAllCandles::class.java)
        if (annotation.instrumentsUids.isEmpty() && annotation.tickers.isEmpty() && annotation.figies.isEmpty()) {
            val subscriptionInterval = annotation.subscriptionInterval
            if (commonHandlersBySubscription[subscriptionInterval] == null) {
                commonHandlersBySubscription[subscriptionInterval] = mutableListOf(this)
            } else {
                commonHandlersBySubscription[subscriptionInterval]?.add(this)
            }
        } else {
            addInstrumentIdToAllHandlerMap()
        }
    }
}