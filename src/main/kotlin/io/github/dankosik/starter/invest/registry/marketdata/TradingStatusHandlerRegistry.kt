package io.github.dankosik.starter.invest.registry.marketdata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleTradingStatus
import io.github.dankosik.starter.invest.contract.status.AsyncTradingStatusHandler
import io.github.dankosik.starter.invest.contract.status.BaseTradingStatusHandler
import io.github.dankosik.starter.invest.contract.status.BlockingTradingStatusHandler
import io.github.dankosik.starter.invest.contract.status.CoroutineTradingStatusHandler
import io.github.dankosik.starter.invest.contract.trade.BaseTradesHandler
import mu.KLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import ru.tinkoff.piapi.contract.v1.Trade
import ru.tinkoff.piapi.contract.v1.TradingStatus

internal class TradingStatusHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private val handlersByFigi = HashMap<String, BaseTradingStatusHandler>()
    private val handlersByInstrumentUid = HashMap<String, BaseTradingStatusHandler>()


    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleTradingStatus::class.java).values
        val coroutineHandlers = annotatedBeans.filterIsInstance<CoroutineTradingStatusHandler>()
        val blockingHandlers = annotatedBeans.filterIsInstance<BlockingTradingStatusHandler>()
        val asyncHandlers = annotatedBeans.filterIsInstance<AsyncTradingStatusHandler>()
        blockingHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        coroutineHandlers.forEach { it.addInstrumentIdToHandlerMap() }
        asyncHandlers.forEach { it.addInstrumentIdToHandlerMap() }
    }

    fun getHandler(tradingStatus: TradingStatus): BaseTradingStatusHandler? =
        getHandlerByUid(tradingStatus.instrumentUid) ?: getHandlerByFigi(tradingStatus.figi)

    fun getHandlerByUid(uId: String?): BaseTradingStatusHandler? = handlersByInstrumentUid[uId]
    fun getHandlerByFigi(figi: String?): BaseTradingStatusHandler? = handlersByFigi[figi]


    private fun BaseTradingStatusHandler.addInstrumentIdToHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleTradingStatus::class.java)
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