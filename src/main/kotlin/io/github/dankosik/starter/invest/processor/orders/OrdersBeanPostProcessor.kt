package io.github.dankosik.starter.invest.processor.orders

import io.github.dankosik.starter.invest.annotation.order.HandleOrders
import io.github.dankosik.starter.invest.contract.orders.AsyncOrdersHandler
import io.github.dankosik.starter.invest.contract.orders.BlockingOrdersHandler
import io.github.dankosik.starter.invest.contract.orders.CoroutineOrdersHandler
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class OrdersBeanPostProcessor : BeanPostProcessor {

    private val uniquePairs = mutableSetOf<Pair<String, String>>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        check(
            !(bean.javaClass.declaredAnnotations.filterIsInstance<HandleOrders>().isNotEmpty()
                    && (bean !is CoroutineOrdersHandler && bean !is BlockingOrdersHandler && bean !is AsyncOrdersHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleOrders should be implements AsyncOrdersHandler or BlockingOrdersHandler or CoroutineOrdersHandler" }
        if (bean is CoroutineOrdersHandler || bean is BlockingOrdersHandler || bean is AsyncOrdersHandler) {
            val classNameInMessage = when (bean) {
                is CoroutineOrdersHandler -> "CoroutineOrdersHandler"
                is BlockingOrdersHandler -> "BlockingOrdersHandler"
                else -> "AsyncOrdersHandler"
            }
            check(bean.javaClass.declaredAnnotations.filterIsInstance<HandleOrders>().isNotEmpty()) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandleOrders"
            }
            val tickerValue = bean.javaClass.getAnnotation(HandleOrders::class.java).ticker
            val figiValue = bean.javaClass.getAnnotation(HandleOrders::class.java).figi
            val instrumentIdValue = bean.javaClass.getAnnotation(HandleOrders::class.java).instrumentUid

            check(tickerValue.isNotBlank() || figiValue.isNotBlank() || instrumentIdValue.isNotBlank()) {
                "At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided in ${bean.javaClass.name}"
            }
            val account = bean.javaClass.getAnnotation(HandleOrders::class.java).account
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
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean
}
