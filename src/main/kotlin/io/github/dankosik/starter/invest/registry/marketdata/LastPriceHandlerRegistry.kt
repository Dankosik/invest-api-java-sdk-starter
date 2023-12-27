package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllLastPrices
import io.github.dankosik.starter.invest.annotation.marketdata.HandleLastPrice
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.BaseLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.getLastPriceHandlers
import org.springframework.context.ApplicationContext
import ru.tinkoff.piapi.contract.v1.LastPrice

internal class LastPriceHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private val handlersByFigi = HashMap<String, MutableList<BaseLastPriceHandler>>()
    private val handlersByInstrumentUid = HashMap<String, MutableList<BaseLastPriceHandler>>()

    init {
        applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values.getLastPriceHandlers()
            .forEach {
                it.addInstrumentIdToHandlerMap()
            }
        applicationContext.getBeansWithAnnotation(HandleAllLastPrices::class.java).values.getLastPriceHandlers()
            .forEach {
                it.addInstrumentIdToAllHandlerMap()
            }
    }

    fun getHandlers(lastPrice: LastPrice): MutableList<BaseLastPriceHandler>? =
        getHandlersByUid(lastPrice.instrumentUid) ?: getHandlersByFigi(lastPrice.figi)

    private fun getHandlersByUid(uId: String?) = handlersByInstrumentUid[uId]

    private fun getHandlersByFigi(figi: String?) = handlersByFigi[figi]

    private fun BaseLastPriceHandler.addInstrumentIdToHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleLastPrice::class.java)
        val figi = annotation.figi
        val instrumentUid = annotation.instrumentUid
        if (figi.isNotBlank()) {
            if (handlersByFigi[figi] == null) {
                handlersByFigi[figi] = mutableListOf(this)
            } else {
                handlersByFigi[figi]!!.add(this)
            }
        } else if (instrumentUid.isNotBlank()) {
            if (handlersByInstrumentUid[instrumentUid] == null) {
                handlersByInstrumentUid[instrumentUid] = mutableListOf(this)
            } else {
                handlersByInstrumentUid[instrumentUid]!!.add(this)
            }
        } else {
            val ticker = annotation.ticker
            if (ticker.isNotBlank()) {
                val uId = tickerToUidMap[ticker]!!
                if (handlersByInstrumentUid[uId] == null) {
                    handlersByInstrumentUid[uId] = mutableListOf(this)
                } else {
                    handlersByInstrumentUid[uId]!!.add(this)
                }
            }
        }
    }

    private fun BaseLastPriceHandler.addInstrumentIdToAllHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleAllLastPrices::class.java)
        annotation.figies.takeIf { it.isNotEmpty() }?.forEach { figi ->
            if (handlersByFigi[figi] == null) {
                handlersByFigi[figi] = mutableListOf(this)
            } else {
                handlersByFigi[figi]!!.add(this)
            }
        }
        annotation.instrumentsUids.takeIf { it.isNotEmpty() }?.forEach { instrumentUid ->
            if (handlersByInstrumentUid[instrumentUid] == null) {
                handlersByInstrumentUid[instrumentUid] = mutableListOf(this)
            } else {
                handlersByInstrumentUid[instrumentUid]!!.add(this)
            }
        }
        annotation.tickers.takeIf { it.isNotEmpty() }?.forEach { ticker ->
            val uId = tickerToUidMap[ticker]!!
            if (handlersByInstrumentUid[uId] == null) {
                handlersByInstrumentUid[uId] = mutableListOf(this)
            } else {
                handlersByInstrumentUid[uId]!!.add(this)
            }
        }
    }
}