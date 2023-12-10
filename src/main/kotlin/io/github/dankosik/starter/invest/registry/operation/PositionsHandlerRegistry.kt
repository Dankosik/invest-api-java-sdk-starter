package io.github.dankosik.starter.invest.registry.operation

import io.github.dankosik.starter.invest.annotation.operation.HandleAllPositions
import io.github.dankosik.starter.invest.annotation.operation.HandlePosition
import io.github.dankosik.starter.invest.contract.operation.positions.AsyncPositionHandler
import io.github.dankosik.starter.invest.contract.operation.positions.BasePositionHandler
import io.github.dankosik.starter.invest.contract.operation.positions.BlockingPositionHandler
import io.github.dankosik.starter.invest.contract.operation.positions.CoroutinePositionHandler
import mu.KLogging
import org.springframework.context.ApplicationContext

internal class PositionsHandlerRegistry(
    private val applicationContext: ApplicationContext,
) {
    private val handlersByAccount = HashMap<String, MutableList<BasePositionHandler>>()
    val allHandlersByAccount = HashMap<String, MutableList<BasePositionHandler>>()

    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePosition::class.java).values
        val coroutineHandlers = annotatedBeans.filterIsInstance<CoroutinePositionHandler>()
        val blockingHandlers = annotatedBeans.filterIsInstance<BlockingPositionHandler>()
        val asyncHandlers = annotatedBeans.filterIsInstance<AsyncPositionHandler>()
        blockingHandlers.forEach { it.addAccountIdToHandlerMap() }
        coroutineHandlers.forEach { it.addAccountIdToHandlerMap() }
        asyncHandlers.forEach { it.addAccountIdToHandlerMap() }

        val annotatedBeansAll = applicationContext.getBeansWithAnnotation(HandleAllPositions::class.java).values
        val coroutineHandlersAll = annotatedBeansAll.filterIsInstance<CoroutinePositionHandler>()
        val blockingHandlersAll = annotatedBeansAll.filterIsInstance<BlockingPositionHandler>()
        val asyncHandlersAll = annotatedBeansAll.filterIsInstance<AsyncPositionHandler>()
        coroutineHandlersAll.forEach { it.addAccountIdToAllHandlerMap() }
        blockingHandlersAll.forEach { it.addAccountIdToAllHandlerMap() }
        asyncHandlersAll.forEach { it.addAccountIdToAllHandlerMap() }
    }

    fun getHandlersByAccountId(accountId: String?) = handlersByAccount[accountId]
    fun getCommonHandlersByAccountId(accountId: String?) = allHandlersByAccount[accountId]

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
            allHandlersByAccount[account]?.add(this) ?: mutableListOf(this)
        }


    private companion object : KLogging()
}