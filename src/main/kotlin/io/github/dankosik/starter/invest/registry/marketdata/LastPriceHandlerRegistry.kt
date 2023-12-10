package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleLastPrice
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.AsyncLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.BaseLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.BlockingLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.CoroutineLastPriceHandler
import mu.KLogging
import org.springframework.context.ApplicationContext
import ru.tinkoff.piapi.contract.v1.LastPrice

internal class LastPriceHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private val handlersByFigi = HashMap<String, MutableList<BaseLastPriceHandler>>()
    private val handlersByInstrumentUid = HashMap<String, MutableList<BaseLastPriceHandler>>()


    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values
        val coroutineHandlers = annotatedBeans.filterIsInstance<CoroutineLastPriceHandler>()
        val blockingHandlers = annotatedBeans.filterIsInstance<BlockingLastPriceHandler>()
        val asyncHandlers = annotatedBeans.filterIsInstance<AsyncLastPriceHandler>()
        blockingHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        coroutineHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        asyncHandlers.forEach { it.addInstrumentIdToHandlerMap() }
    }

    fun getHandlers(lastPrice: LastPrice) =
        getHandlersByUid(lastPrice.instrumentUid) ?: getHandlersByFigi(lastPrice.figi)

    fun getHandlersByUid(uId: String?) = handlersByInstrumentUid[uId]

    fun getHandlersByFigi(figi: String?) = handlersByFigi[figi]


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

    private companion object : KLogging()
}