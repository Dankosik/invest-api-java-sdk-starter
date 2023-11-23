package io.github.dankosik.starter.invest.processor.operation

import io.github.dankosik.starter.invest.annotation.operation.HandlePortfolio
import io.github.dankosik.starter.invest.contract.portfolio.AsyncPortfolioHandler
import io.github.dankosik.starter.invest.contract.portfolio.BlockingPortfolioHandler
import io.github.dankosik.starter.invest.contract.portfolio.CoroutinePortfolioHandler
import org.springframework.beans.factory.config.BeanPostProcessor

internal class PortfolioBeanPostProcessor : BeanPostProcessor {

    private val uniqueAccounts = mutableSetOf<String>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        check(
            !(bean.javaClass.declaredAnnotations.filterIsInstance<HandlePortfolio>().isNotEmpty()
                    && (bean !is CoroutinePortfolioHandler && bean !is BlockingPortfolioHandler && bean !is AsyncPortfolioHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleCandle should be implements AsyncPortfolioHandler or BlockingPortfolioHandler or CoroutinePortfolioHandler" }
        if (bean is CoroutinePortfolioHandler || bean is BlockingPortfolioHandler || bean is AsyncPortfolioHandler) {
            val classNameInMessage = when (bean) {
                is CoroutinePortfolioHandler -> "CoroutinePortfolioHandler"
                is BlockingPortfolioHandler -> "BlockingPortfolioHandler"
                else -> "AsyncPortfolioHandler"
            }
            check(bean.javaClass.declaredAnnotations.filterIsInstance<HandlePortfolio>().isNotEmpty()) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandlePortfolio"
            }
            val account = bean.javaClass.getAnnotation(HandlePortfolio::class.java).account
            check(account.isNotBlank()) {
                "Argument 'account' must be provided in ${bean.javaClass.name}"
            }
            check(account !in uniqueAccounts) {
                "Account: $account is already have handlers ${bean.javaClass.name}"
            }
            uniqueAccounts.add(account)
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean
}
