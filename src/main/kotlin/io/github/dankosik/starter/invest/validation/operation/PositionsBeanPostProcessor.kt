package io.github.dankosik.starter.invest.validation.operation

import io.github.dankosik.starter.invest.annotation.operation.HandleAllPositions
import io.github.dankosik.starter.invest.annotation.operation.HandlePosition
import io.github.dankosik.starter.invest.contract.operation.positions.AsyncPositionHandler
import io.github.dankosik.starter.invest.contract.operation.positions.BlockingPositionHandler
import io.github.dankosik.starter.invest.contract.operation.positions.CoroutinePositionHandler
import org.springframework.beans.factory.config.BeanPostProcessor

internal class PositionsBeanPostProcessor : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandlePosition = bean.javaClass.declaredAnnotations.filterIsInstance<HandlePosition>().isNotEmpty()
        val isAllHandlePosition = bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllPositions>().isNotEmpty()
        val javaClassName = bean.javaClass.name
        check(
            !(isHandlePosition
                    && (bean !is CoroutinePositionHandler && bean !is BlockingPositionHandler && bean !is AsyncPositionHandler))
        ) { "Class: $javaClassName annotated with HandlePosition should implement AsyncPositionsHandler or BlockingPositionsHandler or CoroutinePositionsHandler" }
        check(
            !(isAllHandlePosition
                    && (bean !is CoroutinePositionHandler && bean !is BlockingPositionHandler && bean !is AsyncPositionHandler))
        ) { "Class: $javaClassName annotated with HandleAllPositions should implement AsyncPositionsHandler or BlockingPositionsHandler or CoroutinePositionsHandler" }
        if (isHandlePosition) {
            val classNameInMessage = when (bean) {
                is CoroutinePositionHandler -> "CoroutinePositionsHandler"
                is BlockingPositionHandler -> "BlockingPositionsHandler"
                else -> "AsyncPositionsHandler"
            }
            check(isHandlePosition) {
                "$classNameInMessage: $javaClassName must have an annotated of HandlePosition"
            }
            val account = bean.javaClass.getAnnotation(HandlePosition::class.java).account
            check(account.isNotBlank()) {
                "Argument 'account' must be provided in $javaClassName"
            }
        }
        if (isAllHandlePosition) {
            val annotation = bean.javaClass.getAnnotation(HandleAllPositions::class.java)
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
