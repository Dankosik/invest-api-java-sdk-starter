package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllOrderBooks
import io.github.dankosik.starter.invest.annotation.marketdata.HandleOrderBook
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.BaseOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.getOrderBookHandlers
import org.springframework.context.ApplicationContext
import ru.tinkoff.piapi.contract.v1.OrderBook

internal class OrderBookHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private var handlersByInstrumentUid: MutableMap<String, MutableList<BaseOrderBookHandler>> = mutableMapOf()
    private var handlersByFigi: MutableMap<String, MutableList<BaseOrderBookHandler>> = mutableMapOf()

    init {
        applicationContext.getBeansWithAnnotation(HandleOrderBook::class.java).values.getOrderBookHandlers()
            .forEach {
                it.addInstrumentIdToHandlerMap()
            }

        applicationContext.getBeansWithAnnotation(HandleAllOrderBooks::class.java).values.getOrderBookHandlers()
            .forEach {
                it.addInstrumentIdToAllHandlerMap()
            }
    }

    fun getHandlers(orderBook: OrderBook): MutableList<BaseOrderBookHandler>? =
        getHandlersByUid(orderBook.instrumentUid) ?: getHandlersByFigi(orderBook.figi)

    private fun getHandlersByFigi(figi: String?) = handlersByFigi[figi]

    private fun getHandlersByUid(uId: String?) = handlersByInstrumentUid[uId]

    private fun BaseOrderBookHandler.addInstrumentIdToHandlerMap() {
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

    private fun BaseOrderBookHandler.addInstrumentIdToAllHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleAllOrderBooks::class.java)
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