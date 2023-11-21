package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleLastPrice
import io.github.dankosik.starter.invest.contract.lastprice.AsyncLastPriceHandler
import io.github.dankosik.starter.invest.contract.lastprice.BaseLastPriceHandler
import io.github.dankosik.starter.invest.contract.lastprice.BlockingLastPriceHandler
import io.github.dankosik.starter.invest.contract.lastprice.CoroutineLastPriceHandler
import mu.KLogging
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class LastPriceHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private val handlersByFigi = HashMap<String, BaseLastPriceHandler>()
    private val handlersByInstrumentUid = HashMap<String, BaseLastPriceHandler>()


    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleLastPrice::class.java).values
        val coroutineHandlers = annotatedBeans.filterIsInstance<CoroutineLastPriceHandler>()
        val blockingHandlers = annotatedBeans.filterIsInstance<BlockingLastPriceHandler>()
        val asyncHandlers = annotatedBeans.filterIsInstance<AsyncLastPriceHandler>()
        blockingHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        coroutineHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        asyncHandlers.forEach { it.addInstrumentIdToHandlerMap() }
    }

    fun getHandlerByUid(uId: String?): BaseLastPriceHandler? = handlersByInstrumentUid[uId]
    fun getHandlerByFigi(figi: String?): BaseLastPriceHandler? = handlersByFigi[figi]


    private fun BaseLastPriceHandler.addInstrumentIdToHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleLastPrice::class.java)
        val figi = annotation.figi
        val instrumentUid = annotation.instrumentUid
        if (figi.isNotBlank()) {
            handlersByFigi[figi] = this
        } else if (instrumentUid.isNotBlank()) {
            handlersByInstrumentUid[instrumentUid] = this
        } else {
            val ticker = annotation.ticker
            if (ticker.isNotBlank()) {
                val uId = tickerToUidMap[ticker]!!
                handlersByInstrumentUid[uId] = this
            }
        }
    }

    private companion object : KLogging()
}