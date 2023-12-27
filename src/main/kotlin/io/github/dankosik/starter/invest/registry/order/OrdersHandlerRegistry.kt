package io.github.dankosik.starter.invest.registry.order

import io.github.dankosik.starter.invest.annotation.order.HandleAllOrders
import io.github.dankosik.starter.invest.annotation.order.HandleOrder
import io.github.dankosik.starter.invest.contract.orders.BaseOrderHandler
import io.github.dankosik.starter.invest.contract.orders.getOrderHandlers
import org.springframework.context.ApplicationContext
import ru.tinkoff.piapi.contract.v1.OrderTrades

internal class OrdersHandlerRegistry(
    private val applicationContext: ApplicationContext,
    private val tickerToUidMap: Map<String, String>,
) {
    private val handlersByFigi = HashMap<String, MutableMap<String, MutableList<BaseOrderHandler>>>()
    private val handlersByInstrumentUid = HashMap<String, MutableMap<String, MutableList<BaseOrderHandler>>>()

    val commonHandlersByAccount = HashMap<String, MutableList<BaseOrderHandler>>()

    init {
        applicationContext.getBeansWithAnnotation(HandleOrder::class.java).values.getOrderHandlers()
            .forEach {
                it.addAccountIdToHandlerMap()
            }
        applicationContext.getBeansWithAnnotation(HandleAllOrders::class.java).values.getOrderHandlers()
            .forEach {
                it.addAccountIdToAllHandlerMap()
            }
    }

    fun getHandlers(orderTrades: OrderTrades): MutableList<BaseOrderHandler>? =
        getHandlersByUidAndAccountId(orderTrades.instrumentUid, orderTrades.accountId)
            ?: getHandlersByFigiAndAccountId(orderTrades.figi, orderTrades.accountId)

    fun getCommonHandlersByAccountId(orderTrades: OrderTrades): MutableList<BaseOrderHandler>? =
        commonHandlersByAccount[orderTrades.accountId]

    private fun getHandlersByUidAndAccountId(uId: String?, accountId: String) =
        handlersByInstrumentUid[accountId]?.get(uId)

    private fun getHandlersByFigiAndAccountId(figi: String?, accountId: String) =
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

    private fun BaseOrderHandler.addInstrumentIdToAllHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleAllOrders::class.java)
        val accounts = annotation.accounts
        accounts.takeIf { it.isNotEmpty() }?.forEach { account ->
            annotation.figies.takeIf { it.isNotEmpty() }?.forEach { figi ->
                if (handlersByFigi[account] == null) {
                    handlersByFigi[account] = mutableMapOf(figi to mutableListOf(this))
                } else {
                    if (handlersByFigi[account]!![figi] != null) {
                        handlersByFigi[account]!![figi]!!.add(this)
                    } else {
                        handlersByFigi[account]!![figi] = mutableListOf(this)
                    }
                }
            }
            annotation.instrumentsUids.takeIf { it.isNotEmpty() }?.forEach { instrumentUid ->
                if (handlersByInstrumentUid[account] == null) {
                    handlersByInstrumentUid[account] = mutableMapOf(instrumentUid to mutableListOf(this))
                } else {
                    if (handlersByInstrumentUid[account]!![instrumentUid] != null) {
                        handlersByInstrumentUid[account]!![instrumentUid]!!.add(this)
                    } else {
                        handlersByInstrumentUid[account]!![instrumentUid] = mutableListOf(this)
                    }
                }
            }
            annotation.tickers.takeIf { it.isNotEmpty() }?.forEach { ticker ->
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

    private fun BaseOrderHandler.addAccountIdToAllHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandleAllOrders::class.java)
        if (annotation.instrumentsUids.isEmpty() && annotation.tickers.isEmpty() && annotation.figies.isEmpty()) {
            annotation.accounts.forEach { account ->
                if (commonHandlersByAccount[account] == null) {
                    commonHandlersByAccount[account] = mutableListOf(this)
                } else {
                    commonHandlersByAccount[account]?.add(this)
                }
            }
        } else {
            addInstrumentIdToAllHandlerMap()
        }
    }
}