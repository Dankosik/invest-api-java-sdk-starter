package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTrades
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTrade
import io.github.dankosik.starter.invest.contract.marketdata.trade.BaseTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.getTradesHandlers
import org.springframework.context.ApplicationContext
import ru.tinkoff.piapi.contract.v1.Trade

internal class TradesHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private val handlersByFigi = HashMap<String, MutableList<BaseTradeHandler>>()
    private val handlersByInstrumentUid = HashMap<String, MutableList<BaseTradeHandler>>()

    init {
        applicationContext.getBeansWithAnnotation(HandleTrade::class.java).values.getTradesHandlers()
            .forEach {
                it.addInstrumentIdToHandlerMap()
            }
        applicationContext.getBeansWithAnnotation(HandleAllTrades::class.java).values.getTradesHandlers()
            .forEach {
                it.addInstrumentIdToAllHandlerMap()
            }
    }

    fun getHandlers(trade: Trade): MutableList<BaseTradeHandler>? =
        getHandlersByUid(trade.instrumentUid) ?: getHandlersByFigi(trade.figi)

    private fun getHandlersByUid(uId: String?) = handlersByInstrumentUid[uId]

    private fun getHandlersByFigi(figi: String?) = handlersByFigi[figi]

    private fun BaseTradeHandler.addInstrumentIdToHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleTrade::class.java)
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

    private fun BaseTradeHandler.addInstrumentIdToAllHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleAllTrades::class.java)
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
            val instrumentUid = tickerToUidMap[ticker]!!
            if (handlersByInstrumentUid[instrumentUid] == null) {
                handlersByInstrumentUid[instrumentUid] = mutableListOf(this)
            } else {
                handlersByInstrumentUid[instrumentUid]!!.add(this)
            }
        }
    }
}