package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleTrades
import io.github.dankosik.starter.invest.contract.trade.AsyncTradesHandler
import io.github.dankosik.starter.invest.contract.trade.BaseTradesHandler
import io.github.dankosik.starter.invest.contract.trade.BlockingTradesHandler
import io.github.dankosik.starter.invest.contract.trade.CoroutineTradesHandler
import mu.KLogging
import org.springframework.context.ApplicationContext
import ru.tinkoff.piapi.contract.v1.Trade

internal class TradesHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private val handlersByFigi = HashMap<String, BaseTradesHandler>()
    private val handlersByInstrumentUid = HashMap<String, BaseTradesHandler>()


    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTrades::class.java).values
        val coroutineTradesHandlers = annotatedBeans.filterIsInstance<CoroutineTradesHandler>()
        val blockingTradesHandlers = annotatedBeans.filterIsInstance<BlockingTradesHandler>()
        val asyncTradesHandlers = annotatedBeans.filterIsInstance<AsyncTradesHandler>()
        blockingTradesHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        coroutineTradesHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        asyncTradesHandlers.forEach { it.addInstrumentIdToHandlerMap() }
    }

    fun getHandler(trade: Trade): BaseTradesHandler? =
        getHandlerByUid(trade.instrumentUid) ?: getHandlerByFigi(trade.figi)

    fun getHandlerByUid(uId: String?): BaseTradesHandler? = handlersByInstrumentUid[uId]
    fun getHandlerByFigi(figi: String?): BaseTradesHandler? = handlersByFigi[figi]


    private fun BaseTradesHandler.addInstrumentIdToHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleTrades::class.java)
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