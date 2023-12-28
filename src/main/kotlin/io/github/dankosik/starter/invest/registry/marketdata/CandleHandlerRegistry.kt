package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllCandles
import io.github.dankosik.starter.invest.annotation.marketdata.HandleCandle
import io.github.dankosik.starter.invest.contract.marketdata.candle.BaseCandleHandler
import io.github.dankosik.starter.invest.contract.marketdata.candle.getCandleHandlers
import io.github.dankosik.starter.invest.processor.marketdata.BaseCandleStreamProcessor
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
    val commonHandlersBySubscriptionBefore = HashMap<SubscriptionInterval, MutableList<BaseCandleHandler>>()
    val commonHandlersBySubscriptionAfter = HashMap<SubscriptionInterval, MutableList<BaseCandleHandler>>()
    val commonHandlersBySubscription = HashMap<SubscriptionInterval, MutableList<BaseCandleHandler>>()
    val commonAdaptersMapByFigi =
        HashMap<SubscriptionInterval, MutableMap<String, MutableList<BaseCandleStreamProcessor>>>()
    val commonAdaptersMapByInstrumentUid =
        HashMap<SubscriptionInterval, MutableMap<String, MutableList<BaseCandleStreamProcessor>>>()

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

    fun getHandlersFromFactory(candle: Candle): MutableList<BaseCandleStreamProcessor>? =
        getHandlersByUidAndIntervalFromFactory(candle.instrumentUid, candle.interval)
            ?: getHandlersByFigiAndIntervalFromFactory(candle.figi, candle.interval)

    fun getCommonHandlersAfter(candle: Candle): MutableList<BaseCandleHandler>? =
        commonHandlersBySubscriptionAfter[candle.interval]
    fun getCommonHandlersBefore(candle: Candle): MutableList<BaseCandleHandler>? =
        commonHandlersBySubscriptionBefore[candle.interval]
    fun getCommonHandlers(candle: Candle): MutableList<BaseCandleHandler>? =
        commonHandlersBySubscription[candle.interval]

    private fun getHandlersByUidAndInterval(uId: String?, subscriptionInterval: SubscriptionInterval) =
        handlersByInstrumentUid[subscriptionInterval]?.get(uId)

    private fun getHandlersByFigiAndInterval(figi: String?, subscriptionInterval: SubscriptionInterval) =
        handlersByFigi[subscriptionInterval]?.get(figi)

    private fun getHandlersByUidAndIntervalFromFactory(uId: String?, subscriptionInterval: SubscriptionInterval) =
        commonAdaptersMapByInstrumentUid[subscriptionInterval]?.get(uId)

    private fun getHandlersByFigiAndIntervalFromFactory(figi: String?, subscriptionInterval: SubscriptionInterval) =
        commonAdaptersMapByFigi[subscriptionInterval]?.get(figi)

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

    fun addIntervalToHandlerMap(
        streamProcessor: List<BaseCandleStreamProcessor>,
        sourceTickerToInstrumentMap: Map<String, String>
    ) {
        streamProcessor.forEach {
            it.figies.takeIf { list -> list.isEmpty() }?.forEach { figi ->
                if (figi.isNotBlank()) {
                    if (commonAdaptersMapByFigi[it.subscriptionInterval] == null) {
                        commonAdaptersMapByFigi[it.subscriptionInterval] = mutableMapOf(figi to mutableListOf(it))
                    } else {
                        if (commonAdaptersMapByFigi[it.subscriptionInterval]!![figi] != null) {
                            commonAdaptersMapByFigi[it.subscriptionInterval]!![figi]!!.add(it)
                        } else {
                            commonAdaptersMapByFigi[it.subscriptionInterval]!![figi] = mutableListOf(it)
                        }
                    }
                }
            }
            it.instruemntUids.takeIf { list -> list.isEmpty() }?.forEach { instrumentUid ->
                if (instrumentUid.isNotBlank()) {
                    if (commonAdaptersMapByInstrumentUid[it.subscriptionInterval] == null) {
                        commonAdaptersMapByInstrumentUid[it.subscriptionInterval] =
                            mutableMapOf(instrumentUid to mutableListOf(it))
                    } else {
                        if (commonAdaptersMapByInstrumentUid[it.subscriptionInterval]!![instrumentUid] != null) {
                            commonAdaptersMapByInstrumentUid[it.subscriptionInterval]!![instrumentUid]!!.add(it)
                        } else {
                            commonAdaptersMapByInstrumentUid[it.subscriptionInterval]!![instrumentUid] =
                                mutableListOf(it)
                        }
                    }
                }
            }
            it.tickers.takeIf { list -> list.isEmpty() }?.forEach { ticker ->
                if (ticker.isNotBlank()) {
                    val uId = sourceTickerToInstrumentMap[ticker]!!
                    if (commonAdaptersMapByInstrumentUid[it.subscriptionInterval] == null) {
                        commonAdaptersMapByInstrumentUid[it.subscriptionInterval] =
                            mutableMapOf(uId to mutableListOf(it))
                    } else {
                        if (commonAdaptersMapByInstrumentUid[it.subscriptionInterval]!![uId] != null) {
                            commonAdaptersMapByInstrumentUid[it.subscriptionInterval]!![uId]!!.add(it)
                        } else {
                            commonAdaptersMapByInstrumentUid[it.subscriptionInterval]!![uId] = mutableListOf(it)
                        }
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
            if (annotation.afterEachCandleHandler && !annotation.beforeEachCandleHandler) {
                if (commonHandlersBySubscriptionAfter[subscriptionInterval] == null) {
                    commonHandlersBySubscriptionAfter[subscriptionInterval] = mutableListOf(this)
                } else {
                    commonHandlersBySubscriptionAfter[subscriptionInterval]?.add(this)
                }
            } else if (!annotation.afterEachCandleHandler && annotation.beforeEachCandleHandler) {
                if (commonHandlersBySubscriptionBefore[subscriptionInterval] == null) {
                    commonHandlersBySubscriptionBefore[subscriptionInterval] = mutableListOf(this)
                } else {
                    commonHandlersBySubscriptionBefore[subscriptionInterval]?.add(this)
                }
            } else {
                if (commonHandlersBySubscription[subscriptionInterval] == null) {
                    commonHandlersBySubscription[subscriptionInterval] = mutableListOf(this)
                } else {
                    commonHandlersBySubscription[subscriptionInterval]?.add(this)
                }
            }
        } else {
            addInstrumentIdToAllHandlerMap()
        }
    }
}