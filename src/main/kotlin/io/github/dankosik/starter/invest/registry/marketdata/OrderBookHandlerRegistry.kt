package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleOrderBook
import io.github.dankosik.starter.invest.contract.orderbook.AsyncOrderBookHandler
import io.github.dankosik.starter.invest.contract.orderbook.BaseOrderBookHandler
import io.github.dankosik.starter.invest.contract.orderbook.BlockingOrderBookHandler
import io.github.dankosik.starter.invest.contract.orderbook.CoroutineOrderBookHandler
import mu.KLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
@ConditionalOnBean(name = ["tickerToUidMap"])
class OrderBookHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private var handlersByInstrumentUid: MutableMap<String, BaseOrderBookHandler> = mutableMapOf()
    private var handlersByFigi: MutableMap<String, BaseOrderBookHandler> = mutableMapOf()

    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values
        val coroutineTradesHandlers = annotatedBeans.filterIsInstance<CoroutineOrderBookHandler>()
        val blockingTradesHandlers = annotatedBeans.filterIsInstance<BlockingOrderBookHandler>()
        val tradesHandlers = annotatedBeans.filterIsInstance<AsyncOrderBookHandler>()
        blockingTradesHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        coroutineTradesHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        tradesHandlers.forEach { it.addInstrumentIdToHandlerMap() }
    }

    fun getHandlerByFigi(figi: String?): BaseOrderBookHandler? = handlersByFigi[figi]
    fun getHandlerByUid(uId: String?): BaseOrderBookHandler? = handlersByInstrumentUid[uId]


    private fun BaseOrderBookHandler.addInstrumentIdToHandlerMap(
    ) {
        val annotation = this::class.java.getAnnotation(HandleOrderBook::class.java)
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