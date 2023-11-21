package io.github.dankosik.starter.invest.processor.operation

import io.github.dankosik.starter.invest.annotation.operation.HandlePositions
import io.github.dankosik.starter.invest.contract.positions.AsyncPositionsHandler
import io.github.dankosik.starter.invest.contract.positions.BlockingPositionsHandler
import io.github.dankosik.starter.invest.contract.positions.CoroutinePositionsHandler
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class PositionsBeanPostProcessor : BeanPostProcessor {

    private val uniqueAccounts = mutableSetOf<String>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        check(
            !(bean.javaClass.declaredAnnotations.filterIsInstance<HandlePositions>().isNotEmpty()
                    && (bean !is CoroutinePositionsHandler && bean !is BlockingPositionsHandler && bean !is AsyncPositionsHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleCandle should be implements AsyncPositionsHandler or BlockingPositionsHandler or CoroutinePositionsHandler" }
        if (bean is CoroutinePositionsHandler || bean is BlockingPositionsHandler || bean is AsyncPositionsHandler) {
            val classNameInMessage = when (bean) {
                is CoroutinePositionsHandler -> "CoroutinePositionsHandler"
                is BlockingPositionsHandler -> "BlockingPositionsHandler"
                else -> "AsyncPositionsHandler"
            }
            check(bean.javaClass.declaredAnnotations.filterIsInstance<HandlePositions>().isNotEmpty()) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandlePositions"
            }
            val account = bean.javaClass.getAnnotation(HandlePositions::class.java).account
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
