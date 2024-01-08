package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTradingStatuses
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTradingStatus
import io.github.dankosik.starter.invest.contract.marketdata.status.BaseTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.getTradingStatusHandlers
import org.springframework.context.ApplicationContext
import ru.tinkoff.piapi.contract.v1.TradingStatus

internal class TradingStatusHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private val handlersByFigi = HashMap<String, MutableList<BaseTradingStatusHandler>>()
    private val handlersByInstrumentUid = HashMap<String, MutableList<BaseTradingStatusHandler>>()

    init {
        applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values.getTradingStatusHandlers()
            .forEach {
                it.addInstrumentIdToHandlerMap()
            }
        applicationContext.getBeansWithAnnotation(HandleAllTradingStatuses::class.java).values.getTradingStatusHandlers()
            .forEach {
                it.addInstrumentIdToAllHandlerMap()
            }
    }

    fun getHandlers(tradingStatus: TradingStatus): MutableList<BaseTradingStatusHandler>? =
        getHandlersByUid(tradingStatus.instrumentUid) ?: getHandlersByFigi(tradingStatus.figi)

    private fun getHandlersByUid(uId: String?) = handlersByInstrumentUid[uId]

    private fun getHandlersByFigi(figi: String?) = handlersByFigi[figi]

    private fun BaseTradingStatusHandler.addInstrumentIdToHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleTradingStatus::class.java)
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

    private fun BaseTradingStatusHandler.addInstrumentIdToAllHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleAllTradingStatuses::class.java)
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