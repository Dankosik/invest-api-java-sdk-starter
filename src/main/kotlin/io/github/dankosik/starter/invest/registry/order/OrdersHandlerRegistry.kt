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
    private val handlersByFigi = HashMap<String, MutableMap<String, MutableList<BaseOrderHandler>>>()
    private val handlersByInstrumentUid = HashMap<String, MutableMap<String, MutableList<BaseOrderHandler>>>()

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

    fun getHandlers(orderTrades: OrderTrades) =
        getHandlersByUidAndAccountId(orderTrades.instrumentUid, orderTrades.accountId)
            ?: getHandlersByFigiAndAccountId(orderTrades.figi, orderTrades.accountId)

    fun getCommonHandlersByAccountId(orderTrades: OrderTrades) = allHandlersByAccount[orderTrades.accountId]

    fun getHandlersByUidAndAccountId(uId: String?, accountId: String) =
        handlersByInstrumentUid[accountId]?.get(uId)

    fun getHandlersByFigiAndAccountId(figi: String?, accountId: String) =
        handlersByFigi[accountId]?.get(figi)

    private fun BaseOrderHandler.addAccountIdToHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleOrder::class.java)
        val figi = annotation.figi
        val instrumentUid = annotation.instrumentUid
        val account = annotation.account
        if (figi.isNotBlank()) {
            if (handlersByFigi[account] == null) {
                handlersByFigi[account] = mutableMapOf(figi to mutableListOf(this))
            } else {
                if (handlersByFigi[account]!![figi] != null) {
                    handlersByFigi[account]!![figi]!!.add(this)
                } else {
                    handlersByFigi[account]!![figi] = mutableListOf(this)
                }
            }
        } else if (instrumentUid.isNotBlank()) {
            if (handlersByInstrumentUid[account] == null) {
                handlersByInstrumentUid[account] = mutableMapOf(instrumentUid to mutableListOf(this))
            } else {
                if (handlersByInstrumentUid[account]!![instrumentUid] != null) {
                    handlersByInstrumentUid[account]!![instrumentUid]!!.add(this)
                } else {
                    handlersByInstrumentUid[account]!![instrumentUid] = mutableListOf(this)
                }
            }
        } else {
            val ticker = annotation.ticker
            if (ticker.isNotBlank()) {
                val uId = tickerToUidMap[ticker]!!
                if (handlersByInstrumentUid[account] == null) {
                    handlersByInstrumentUid[account] = mutableMapOf(uId to mutableListOf(this))
                } else {
                    if (handlersByInstrumentUid[account]!![uId] != null) {
                        handlersByInstrumentUid[account]!![uId]!!.add(this)
                    } else {
                        handlersByInstrumentUid[account]!![uId] = mutableListOf(this)
                    }
                }
            }
        }
    }

    private fun BaseOrderHandler.addAccountIdToAllHandlerMap() =
        this::class.java.getAnnotation(HandleAllOrders::class.java).accounts.forEach { account ->
            if (allHandlersByAccount[account] == null) {
                allHandlersByAccount[account] = mutableListOf(this)
            } else {
                allHandlersByAccount[account]?.add(this)
            }
        }

    private companion object : KLogging()
}