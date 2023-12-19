package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleTradingStatus
import io.github.dankosik.starter.invest.contract.marketdata.status.AsyncTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.BaseTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.BlockingTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.CoroutineTradingStatusHandler
import mu.KLogging
import org.springframework.context.ApplicationContext
import ru.tinkoff.piapi.contract.v1.TradingStatus

internal class TradingStatusHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private val handlersByFigi = HashMap<String, MutableList<BaseTradingStatusHandler>>()
    private val handlersByInstrumentUid = HashMap<String, MutableList<BaseTradingStatusHandler>>()

    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values
        val coroutineHandlers = annotatedBeans.filterIsInstance<CoroutineTradingStatusHandler>()
        val blockingHandlers = annotatedBeans.filterIsInstance<BlockingTradingStatusHandler>()
        val asyncHandlers = annotatedBeans.filterIsInstance<AsyncTradingStatusHandler>()
        blockingHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        coroutineHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        asyncHandlers.forEach { it.addInstrumentIdToHandlerMap() }
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
}