package io.github.dankosik.starter.invest.validation.operation

import io.github.dankosik.starter.invest.annotation.operation.HandleAllPortfolios
import io.github.dankosik.starter.invest.annotation.operation.HandlePortfolio
import io.github.dankosik.starter.invest.contract.operation.portfolio.AsyncPortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.BlockingPortfolioHandler
import io.github.dankosik.starter.invest.contract.operation.portfolio.CoroutinePortfolioHandler
import org.springframework.beans.factory.config.BeanPostProcessor

internal class PortfolioBeanPostProcessor : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandlePortfolio = bean.javaClass.declaredAnnotations.filterIsInstance<HandlePortfolio>().isNotEmpty()
        val isAllHandlePortfolio =
            bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllPortfolios>().isNotEmpty()
        val javaClassName = bean.javaClass.name
        check(
            !(isHandlePortfolio
                    && (bean !is CoroutinePortfolioHandler && bean !is BlockingPortfolioHandler && bean !is AsyncPortfolioHandler))
        ) { "Class: $javaClassName annotated with HandlePortfolio should implement AsyncPortfolioHandler or BlockingPortfolioHandler or CoroutinePortfolioHandler" }
        check(
            !(isAllHandlePortfolio
                    && (bean !is CoroutinePortfolioHandler && bean !is BlockingPortfolioHandler && bean !is AsyncPortfolioHandler))
        ) { "Class: $javaClassName annotated with HandleAllPortfolios should implement AsyncPortfolioHandler or BlockingPortfolioHandler or CoroutinePortfolioHandler" }
        if (isHandlePortfolio) {
            val classNameInMessage = when (bean) {
                is CoroutinePortfolioHandler -> "CoroutinePortfolioHandler"
                is BlockingPortfolioHandler -> "BlockingPortfolioHandler"
                else -> "AsyncPortfolioHandler"
            }
            check(isHandlePortfolio) {
                "$classNameInMessage: $javaClassName must have an annotated of HandlePortfolio"
            }
            val account = bean.javaClass.getAnnotation(HandlePortfolio::class.java).account
            check(account.isNotBlank()) {
                "Argument 'account' must be provided in $javaClassName"
            }
        }

        if (isAllHandlePortfolio) {
            val annotation = bean.javaClass.getAnnotation(HandleAllPortfolios::class.java)
            check(annotation.accounts.isNotEmpty()) {
                "$javaClassName: At least one element should be in 'accounts'"
            }
            annotation.accounts.forEach { account ->
                check(account.isNotBlank()) {
                    "$javaClassName: Account should be not blank"
                }
            }
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean
}
