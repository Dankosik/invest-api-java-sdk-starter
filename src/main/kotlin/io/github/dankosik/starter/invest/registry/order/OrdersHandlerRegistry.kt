package io.github.dankosik.starter.invest.registry.order

import io.github.dankosik.starter.invest.annotation.order.HandleOrders
import io.github.dankosik.starter.invest.contract.orders.AsyncOrdersHandler
import io.github.dankosik.starter.invest.contract.orders.BaseOrdersHandler
import io.github.dankosik.starter.invest.contract.orders.BlockingOrdersHandler
import io.github.dankosik.starter.invest.contract.orders.CoroutineOrdersHandler
import mu.KLogging
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class OrdersHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private val handlersByFigi = HashMap<String, Map<String, BaseOrdersHandler>>()
    private val handlersByInstrumentUid = HashMap<String, Map<String, BaseOrdersHandler>>()

    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrders::class.java).values
        val coroutineHandlers = annotatedBeans.filterIsInstance<CoroutineOrdersHandler>()
        val blockingHandlers = annotatedBeans.filterIsInstance<BlockingOrdersHandler>()
        val asyncHandlers = annotatedBeans.filterIsInstance<AsyncOrdersHandler>()
        blockingHandlers.forEach { it.addAccountIdToHandlerMap() }
        coroutineHandlers.forEach { it.addAccountIdToHandlerMap() }
        asyncHandlers.forEach { it.addAccountIdToHandlerMap() }
    }

    fun getHandlerByUidAndAccountId(uId: String?, accountId: String): BaseOrdersHandler? =
        handlersByInstrumentUid[accountId]?.get(uId)

    fun getHandlerByFigiAndAccountId(figi: String?, accountId: String): BaseOrdersHandler? =
        handlersByFigi[accountId]?.get(figi)

    private fun BaseOrdersHandler.addAccountIdToHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleOrders::class.java)
        val figi = annotation.figi
        val instrumentUid = annotation.instrumentUid
        val account = annotation.account
        if (figi.isNotBlank()) {
            handlersByFigi[account] = mapOf(figi to this)
        } else if (instrumentUid.isNotBlank()) {
            handlersByInstrumentUid[account] = mapOf(instrumentUid to this)
        } else {
            val ticker = annotation.ticker
            if (ticker.isNotBlank()) {
                val uId = tickerToUidMap[ticker]!!
                handlersByInstrumentUid[account] = mapOf(uId to this)
            }
        }
    }

    private companion object : KLogging()
}