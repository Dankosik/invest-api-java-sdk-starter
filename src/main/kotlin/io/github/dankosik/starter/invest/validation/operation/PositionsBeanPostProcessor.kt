package io.github.dankosik.starter.invest.validation.operation

import io.github.dankosik.starter.invest.annotation.operation.HandleAllPositions
import io.github.dankosik.starter.invest.annotation.operation.HandlePosition
import io.github.dankosik.starter.invest.annotation.order.HandleAllOrders
import io.github.dankosik.starter.invest.contract.operation.positions.AsyncPositionHandler
import io.github.dankosik.starter.invest.contract.operation.positions.BlockingPositionHandler
import io.github.dankosik.starter.invest.contract.operation.positions.CoroutinePositionHandler
import org.springframework.beans.factory.config.BeanPostProcessor

internal class PositionsBeanPostProcessor : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandlePosition = bean.javaClass.declaredAnnotations.filterIsInstance<HandlePosition>().isNotEmpty()
        val isAllHandlePosition = bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllPositions>().isNotEmpty()
        check(
            !(isHandlePosition
                    && (bean !is CoroutinePositionHandler && bean !is BlockingPositionHandler && bean !is AsyncPositionHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandlePosition should be implements AsyncPositionsHandler or BlockingPositionsHandler or CoroutinePositionsHandler" }
        check(
            !(isAllHandlePosition
                    && (bean !is CoroutinePositionHandler && bean !is BlockingPositionHandler && bean !is AsyncPositionHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleAllPositions should be implements AsyncPositionsHandler or BlockingPositionsHandler or CoroutinePositionsHandler" }
        if (isHandlePosition) {
            val classNameInMessage = when (bean) {
                is CoroutinePositionHandler -> "CoroutinePositionsHandler"
                is BlockingPositionHandler -> "BlockingPositionsHandler"
                else -> "AsyncPositionsHandler"
            }
            check(isAllHandlePosition) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandlePositions"
            }
            val account = bean.javaClass.getAnnotation(HandlePosition::class.java).account
            check(account.isNotBlank()) {
                "Argument 'account' must be provided in ${bean.javaClass.name}"
            }
        }
        if (isAllHandlePosition) {
            check(bean.javaClass.getAnnotation(HandleAllPositions::class.java).accounts.isNotEmpty()) {
                "${bean.javaClass.name}: At least one element should be in 'accounts'"
            }
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean
}
