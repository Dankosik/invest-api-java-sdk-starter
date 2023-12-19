package io.github.dankosik.starter.invest.registry.operation

import io.github.dankosik.starter.invest.annotation.operation.HandleAllPortfolios
import io.github.dankosik.starter.invest.annotation.operation.HandlePortfolio
import io.github.dankosik.starter.invest.contract.operation.portfolio.AsyncPortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.BasePortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.BlockingPortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.CoroutinePortfolioHandler
import org.springframework.context.ApplicationContext

internal class PortfolioHandlerRegistry(
    private val applicationContext: ApplicationContext,
) {
    private val handlersByAccount = HashMap<String, MutableList<BasePortfolioHandler>>()
    val commonHandlersByAccount = HashMap<String, MutableList<BasePortfolioHandler>>()

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

    fun getHandlersByAccountId(accountId: String?): MutableList<BasePortfolioHandler>? =
        handlersByAccount[accountId]

    fun getCommonHandlersByAccountId(accountId: String?): MutableList<BasePortfolioHandler>? =
        commonHandlersByAccount[accountId]

    private fun BasePortfolioHandler.addAccountIdToHandlerMap() {
        val account = this::class.java.getAnnotation(HandlePortfolio::class.java).account
        if (handlersByAccount[account] == null) {
            handlersByAccount[account] = mutableListOf(this)
        } else {
            handlersByAccount[account]!!.add(this)
        }
    }

    private fun BasePortfolioHandler.addAccountIdToAllHandlerMap() =
        this::class.java.getAnnotation(HandleAllPortfolios::class.java).accounts.forEach { account ->
            if (commonHandlersByAccount[account] == null) {
                commonHandlersByAccount[account] = mutableListOf(this)
            } else {
                commonHandlersByAccount[account]?.add(this)
            }
        }
}