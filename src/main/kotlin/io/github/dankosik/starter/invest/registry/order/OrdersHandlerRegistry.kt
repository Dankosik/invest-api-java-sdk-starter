package io.github.dankosik.starter.invest.registry.order

import io.github.dankosik.starter.invest.annotation.order.HandleAllOrders
import io.github.dankosik.starter.invest.annotation.order.HandleOrder
import io.github.dankosik.starter.invest.contract.orders.AsyncOrderHandler
import io.github.dankosik.starter.invest.contract.orders.BaseOrderHandler
import io.github.dankosik.starter.invest.contract.orders.BlockingOrderHandler
import io.github.dankosik.starter.invest.contract.orders.CoroutineOrderHandler
import mu.KLogging
import org.springframework.context.ApplicationContext
import ru.tinkoff.piapi.contract.v1.OrderTrades

internal class OrdersHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private val handlersByFigi = HashMap<String, Map<String, BaseOrderHandler>>()
    private val handlersByInstrumentUid = HashMap<String, Map<String, BaseOrderHandler>>()

    val allHandlersByAccount = HashMap<String, MutableList<BaseOrderHandler>>()

    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandleOrder::class.java).values
        val coroutineHandlers = annotatedBeans.filterIsInstance<CoroutineOrderHandler>()
        val blockingHandlers = annotatedBeans.filterIsInstance<BlockingOrderHandler>()
        val asyncHandlers = annotatedBeans.filterIsInstance<AsyncOrderHandler>()
        blockingHandlers.forEach { it.addAccountIdToHandlerMap() }
        coroutineHandlers.forEach { it.addAccountIdToHandlerMap() }
        asyncHandlers.forEach { it.addAccountIdToHandlerMap() }

        val annotatedBeansAll = applicationContext.getBeansWithAnnotation(HandleAllOrders::class.java).values
        val coroutineHandlersAll = annotatedBeansAll.filterIsInstance<CoroutineOrderHandler>()
        val blockingHandlersAll = annotatedBeansAll.filterIsInstance<BlockingOrderHandler>()
        val asyncHandlersAll = annotatedBeansAll.filterIsInstance<AsyncOrderHandler>()
        blockingHandlersAll.forEach { it.addAccountIdToAllHandlerMap() }
        coroutineHandlersAll.forEach { it.addAccountIdToAllHandlerMap() }
        asyncHandlersAll.forEach { it.addAccountIdToAllHandlerMap() }
    }

    fun getHandler(orderTrades: OrderTrades) =
        getHandlerByUidAndAccountId(orderTrades.instrumentUid, orderTrades.accountId)
            ?: getHandlerByFigiAndAccountId(orderTrades.figi, orderTrades.accountId)

    fun getAllHandlersByAccountId(orderTrades: OrderTrades) = allHandlersByAccount[orderTrades.accountId]

    fun getHandlerByUidAndAccountId(uId: String?, accountId: String): BaseOrderHandler? =
        handlersByInstrumentUid[accountId]?.get(uId)

    fun getHandlerByFigiAndAccountId(figi: String?, accountId: String): BaseOrderHandler? =
        handlersByFigi[accountId]?.get(figi)

    private fun BaseOrderHandler.addAccountIdToHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleOrder::class.java)
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

    private fun BaseOrderHandler.addAccountIdToAllHandlerMap() =
        this::class.java.getAnnotation(HandleAllOrders::class.java).accounts.forEach { account ->
            allHandlersByAccount[account]?.add(this) ?: mutableListOf(this)
        }

    private companion object : KLogging()
}