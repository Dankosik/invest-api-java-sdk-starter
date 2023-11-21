package io.github.dankosik.starter.invest.registry.operation

import io.github.dankosik.starter.invest.annotation.operation.HandlePositions
import io.github.dankosik.starter.invest.contract.positions.AsyncPositionsHandler
import io.github.dankosik.starter.invest.contract.positions.BasePositionsHandler
import io.github.dankosik.starter.invest.contract.positions.BlockingPositionsHandler
import io.github.dankosik.starter.invest.contract.positions.CoroutinePositionsHandler
import mu.KLogging
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class PositionsHandlerRegistry(
    private val applicationContext: ApplicationContext,
) {
    private val handlersByAccount = HashMap<String, BasePositionsHandler>()

    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePositions::class.java).values
        val coroutineHandlers = annotatedBeans.filterIsInstance<CoroutinePositionsHandler>()
        val blockingHandlers = annotatedBeans.filterIsInstance<BlockingPositionsHandler>()
        val asyncHandlers = annotatedBeans.filterIsInstance<AsyncPositionsHandler>()
        blockingHandlers.forEach { it.addAccountIdToHandlerMap() }
        coroutineHandlers.forEach { it.addAccountIdToHandlerMap() }
        asyncHandlers.forEach { it.addAccountIdToHandlerMap() }
    }

    fun getHandlerByAccountId(accountId: String?): BasePositionsHandler? = handlersByAccount[accountId]

    private fun BasePositionsHandler.addAccountIdToHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandlePositions::class.java)
        handlersByAccount[annotation.account] = this
    }

    private companion object : KLogging()
}