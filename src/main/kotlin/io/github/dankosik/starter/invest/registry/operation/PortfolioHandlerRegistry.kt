package io.github.dankosik.starter.invest.registry.operation

import io.github.dankosik.starter.invest.annotation.operation.HandlePortfolio
import io.github.dankosik.starter.invest.contract.portfolio.AsyncPortfolioHandler
import io.github.dankosik.starter.invest.contract.portfolio.BasePortfolioHandler
import io.github.dankosik.starter.invest.contract.portfolio.BlockingPortfolioHandler
import io.github.dankosik.starter.invest.contract.portfolio.CoroutinePortfolioHandler
import mu.KLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
@ConditionalOnBean(name = ["tickerToUidMap"])
class PortfolioHandlerRegistry(
    private val applicationContext: ApplicationContext,
) {
    private val handlersByAccount = HashMap<String, BasePortfolioHandler>()

    init {
        val annotatedBeans = applicationContext.getBeansWithAnnotation(HandlePortfolio::class.java).values
        val coroutineHandlers = annotatedBeans.filterIsInstance<CoroutinePortfolioHandler>()
        val blockingHandlers = annotatedBeans.filterIsInstance<BlockingPortfolioHandler>()
        val asyncHandlers = annotatedBeans.filterIsInstance<AsyncPortfolioHandler>()
        blockingHandlers.forEach { it.addAccountIdToHandlerMap() }
        coroutineHandlers.forEach { it.addAccountIdToHandlerMap() }
        asyncHandlers.forEach { it.addAccountIdToHandlerMap() }
    }

    fun getHandlerByAccountId(accountId: String?): BasePortfolioHandler? = handlersByAccount[accountId]

    private fun BasePortfolioHandler.addAccountIdToHandlerMap() {
        val annotation = this::class.java.getAnnotation(HandlePortfolio::class.java)
        handlersByAccount[annotation.account] = this
    }

    private companion object : KLogging()
}