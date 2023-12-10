package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleOrderBook
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.AsyncOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.BaseOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.BlockingOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.CoroutineOrderBookHandler
import mu.KLogging
import org.springframework.context.ApplicationContext
import ru.tinkoff.piapi.contract.v1.OrderBook

internal class OrderBookHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private var handlersByInstrumentUid: MutableMap<String, MutableList<BaseOrderBookHandler>> = mutableMapOf()
    private var handlersByFigi: MutableMap<String, MutableList<BaseOrderBookHandler>> = mutableMapOf()

    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values
        val coroutineTradesHandlers = annotatedBeans.filterIsInstance<CoroutineOrderBookHandler>()
        val blockingTradesHandlers = annotatedBeans.filterIsInstance<BlockingOrderBookHandler>()
        val tradesHandlers = annotatedBeans.filterIsInstance<AsyncOrderBookHandler>()
        blockingTradesHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        coroutineTradesHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        tradesHandlers.forEach { it.addInstrumentIdToHandlerMap() }
    }

    fun getHandlers(orderBook: OrderBook) = getHandlersByUid(orderBook.instrumentUid) ?: getHandlersByFigi(orderBook.figi)

    fun getHandlersByFigi(figi: String?) = handlersByFigi[figi]

    fun getHandlersByUid(uId: String?) = handlersByInstrumentUid[uId]


    private fun BaseOrderBookHandler.addInstrumentIdToHandlerMap(
    ) {
        val annotation = this::class.java.getAnnotation(HandleOrderBook::class.java)
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