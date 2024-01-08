package io.github.dankosik.starter.invest.registry.operation

import io.github.dankosik.starter.invest.annotation.operation.HandleAllPositions
import io.github.dankosik.starter.invest.annotation.operation.HandlePosition
import io.github.dankosik.starter.invest.contract.operation.positions.BasePositionHandler
import io.github.dankosik.starter.invest.contract.operation.positions.getPositionHandlers
import org.springframework.context.ApplicationContext

internal class PositionsHandlerRegistry(
    private val applicationContext: ApplicationContext,
) {
    private val handlersByAccount = HashMap<String, MutableList<BasePositionHandler>>()
    val commonHandlersByAccount = HashMap<String, MutableList<BasePositionHandler>>()

    init {
        applicationContext.getBeansWithAnnotation(HandlePosition::class.java).values.getPositionHandlers()
            .forEach {
                it.addAccountIdToHandlerMap()
            }
        applicationContext.getBeansWithAnnotation(HandleAllPositions::class.java).values.getPositionHandlers()
            .forEach {
                it.addAccountIdToAllHandlerMap()
            }
    }

    fun getHandlersByAccountId(accountId: String?): MutableList<BasePositionHandler>? =
        handlersByAccount[accountId]

    fun getCommonHandlersByAccountId(accountId: String?): MutableList<BasePositionHandler>? =
        commonHandlersByAccount[accountId]

    private fun BasePositionHandler.addAccountIdToHandlerMap() {
        val account = this::class.java.getAnnotation(HandlePosition::class.java).account
        if (handlersByAccount[account] == null) {
            handlersByAccount[account] = mutableListOf(this)
        } else {
            handlersByAccount[account]!!.add(this)
        }
    }

    private fun BasePositionHandler.addAccountIdToAllHandlerMap() =
        this::class.java.getAnnotation(HandleAllPositions::class.java).accounts.forEach { account ->
            if (commonHandlersByAccount[account] == null) {
                commonHandlersByAccount[account] = mutableListOf(this)
            } else {
                commonHandlersByAccount[account]?.add(this)
            }
        }
}