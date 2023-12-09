package io.github.dankosik.starter.invest.validation.orders

import io.github.dankosik.starter.invest.annotation.order.HandleAllOrders
import io.github.dankosik.starter.invest.annotation.order.HandleOrder
import io.github.dankosik.starter.invest.contract.orders.AsyncOrderHandler
import io.github.dankosik.starter.invest.contract.orders.BlockingOrderHandler
import io.github.dankosik.starter.invest.contract.orders.CoroutineOrderHandler
import org.springframework.beans.factory.config.BeanPostProcessor

internal class OrdersBeanPostProcessor : BeanPostProcessor {

    private val uniquePairs = mutableSetOf<Pair<String, String>>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandleOrder = bean.javaClass.declaredAnnotations.filterIsInstance<HandleOrder>().isNotEmpty()
        val isAllHandleOrders = bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllOrders>().isNotEmpty()
        check(
            !(isHandleOrder
                    && (bean !is CoroutineOrderHandler && bean !is BlockingOrderHandler && bean !is AsyncOrderHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleOrder should be implements AsyncOrdersHandler or BlockingOrdersHandler or CoroutineOrdersHandler" }
        check(
            !(isAllHandleOrders
                    && (bean !is CoroutineOrderHandler && bean !is BlockingOrderHandler && bean !is AsyncOrderHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleAllOrders should be implements AsyncOrdersHandler or BlockingOrdersHandler or CoroutineOrdersHandler" }
        if (isHandleOrder) {
            val classNameInMessage = when (bean) {
                is CoroutineOrderHandler -> "CoroutineOrdersHandler"
                is BlockingOrderHandler -> "BlockingOrdersHandler"
                else -> "AsyncOrdersHandler"
            }
            check(isHandleOrder) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandleOrders"
            }
            val annotation = bean.javaClass.getAnnotation(HandleOrder::class.java)
            val tickerValue = annotation.ticker
            val figiValue = annotation.figi
            val instrumentIdValue = annotation.instrumentUid

            check(tickerValue.isNotBlank() || figiValue.isNotBlank() || instrumentIdValue.isNotBlank()) {
                "At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided in ${bean.javaClass.name}"
            }
            val account = annotation.account
            check(account.isNotBlank()) {
                "Argument 'account' must be provided in ${bean.javaClass.name}"
            }
            val instrumentId = when {
                tickerValue.isNotBlank() -> tickerValue
                instrumentIdValue.isNotBlank() -> instrumentIdValue
                else -> figiValue
            }
            val pair = instrumentId to account
            check(pair !in uniquePairs) {
                "$instrumentId with $account already exist ${bean.javaClass.name}"
            }
            uniquePairs.add(pair)
        }
        if (isAllHandleOrders) {
            check(bean.javaClass.getAnnotation(HandleAllOrders::class.java).accounts.isNotEmpty()) {
                "${bean.javaClass.name}: At least one element should be in 'accounts'"
            }
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean
}
