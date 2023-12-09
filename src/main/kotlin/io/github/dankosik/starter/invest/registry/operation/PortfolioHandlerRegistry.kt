package io.github.dankosik.starter.invest.registry.operation

import io.github.dankosik.starter.invest.annotation.operation.HandleAllPortfolios
import io.github.dankosik.starter.invest.annotation.operation.HandleAllPositions
import io.github.dankosik.starter.invest.annotation.operation.HandlePortfolio
import io.github.dankosik.starter.invest.contract.operation.portfolio.AsyncPortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.BasePortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.BlockingPortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.CoroutinePortfolioHandler
import mu.KLogging
import org.springframework.context.ApplicationContext

internal class PortfolioHandlerRegistry(
    private val applicationContext: ApplicationContext,
) {
    private val handlersByAccount = HashMap<String, BasePortfolioHandler>()
    val allHandlersByAccount = HashMap<String, MutableList<BasePortfolioHandler>>()


    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePortfolio::class.java).values
        val coroutineHandlers = annotatedBeans.filterIsInstance<CoroutinePortfolioHandler>()
        val blockingHandlers = annotatedBeans.filterIsInstance<BlockingPortfolioHandler>()
        val asyncHandlers = annotatedBeans.filterIsInstance<AsyncPortfolioHandler>()
        blockingHandlers.forEach { it.addAccountIdToHandlerMap() }
        coroutineHandlers.forEach { it.addAccountIdToHandlerMap() }
        asyncHandlers.forEach { it.addAccountIdToHandlerMap() }

        val annotatedBeansAll = applicationContext.getBeansWithAnnotation(HandleAllPortfolios::class.java).values
        val coroutineHandlersAll = annotatedBeansAll.filterIsInstance<CoroutinePortfolioHandler>()
        val blockingHandlersAll = annotatedBeansAll.filterIsInstance<BlockingPortfolioHandler>()
        val asyncHandlersAll = annotatedBeansAll.filterIsInstance<AsyncPortfolioHandler>()
        coroutineHandlersAll.forEach { it.addAccountIdToAllHandlerMap() }
        blockingHandlersAll.forEach { it.addAccountIdToAllHandlerMap() }
        asyncHandlersAll.forEach { it.addAccountIdToAllHandlerMap() }
    }

    fun getHandlerByAccountId(accountId: String?): BasePortfolioHandler? = handlersByAccount[accountId]
    fun getHandlersByAccountId(accountId: String?) = allHandlersByAccount[accountId]

    private fun BasePortfolioHandler.addAccountIdToHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandlePortfolio::class.java)
        handlersByAccount[annotation.account] = this
    }

    private fun BasePortfolioHandler.addAccountIdToAllHandlerMap() =
        this::class.java.getAnnotation(HandleAllPositions::class.java).accounts.forEach { account ->
            allHandlersByAccount[account]?.add(this) ?: mutableListOf(this)
        }

    private companion object : KLogging()
}