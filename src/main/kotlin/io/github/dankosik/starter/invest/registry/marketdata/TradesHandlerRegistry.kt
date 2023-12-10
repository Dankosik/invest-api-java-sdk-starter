package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleTrade
import io.github.dankosik.starter.invest.contract.marketdata.trade.AsyncTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.BaseTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.BlockingTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.CoroutineTradeHandler
import mu.KLogging
import org.springframework.context.ApplicationContext
import ru.tinkoff.piapi.contract.v1.Trade

internal class TradesHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private val handlersByFigi = HashMap<String, MutableList<BaseTradeHandler>>()
    private val handlersByInstrumentUid = HashMap<String, MutableList<BaseTradeHandler>>()


    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTrade::class.java).values
        val coroutineTradesHandlers = annotatedBeans.filterIsInstance<CoroutineTradeHandler>()
        val blockingTradesHandlers = annotatedBeans.filterIsInstance<BlockingTradeHandler>()
        val asyncTradesHandlers = annotatedBeans.filterIsInstance<AsyncTradeHandler>()
        blockingTradesHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        coroutineTradesHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        asyncTradesHandlers.forEach { it.addInstrumentIdToHandlerMap() }
    }

    fun getHandlers(trade: Trade) = getHandlersByUid(trade.instrumentUid) ?: getHandlersByFigi(trade.figi)

    fun getHandlersByUid(uId: String?) = handlersByInstrumentUid[uId]

    fun getHandlersByFigi(figi: String?) = handlersByFigi[figi]


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

    private companion object : KLogging()
}