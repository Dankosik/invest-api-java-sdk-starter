package io.github.dankosik.starter.invest.validation.orders

import io.github.dankosik.starter.invest.annotation.order.HandleAllOrders
import io.github.dankosik.starter.invest.annotation.order.HandleOrder
import io.github.dankosik.starter.invest.contract.orders.AsyncOrderHandler
import io.github.dankosik.starter.invest.contract.orders.BlockingOrderHandler
import io.github.dankosik.starter.invest.contract.orders.CoroutineOrderHandler
import org.springframework.beans.factory.config.BeanPostProcessor

internal class OrdersBeanPostProcessor : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandleOrder = bean.javaClass.declaredAnnotations.filterIsInstance<HandleOrder>().isNotEmpty()
        val isAllHandleOrders = bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllOrders>().isNotEmpty()
        val javaClassName = bean.javaClass.name
        check(
            !(isHandleOrder
                    && (bean !is CoroutineOrderHandler && bean !is BlockingOrderHandler && bean !is AsyncOrderHandler))
        ) { "Class: $javaClassName annotated with HandleOrder should implement AsyncOrdersHandler or BlockingOrdersHandler or CoroutineOrdersHandler" }
        check(
            !(isAllHandleOrders
                    && (bean !is CoroutineOrderHandler && bean !is BlockingOrderHandler && bean !is AsyncOrderHandler))
        ) { "Class: $javaClassName annotated with HandleAllOrders should implement AsyncOrdersHandler or BlockingOrdersHandler or CoroutineOrdersHandler" }
        if (isHandleOrder) {
            val classNameInMessage = when (bean) {
                is CoroutineOrderHandler -> "CoroutineOrdersHandler"
                is BlockingOrderHandler -> "BlockingOrdersHandler"
                else -> "AsyncOrdersHandler"
            }
            check(isHandleOrder) {
                "$classNameInMessage: $javaClassName must have an annotated of HandleOrders"
            }
            val annotation = bean.javaClass.getAnnotation(HandleOrder::class.java)
            val tickerValue = annotation.ticker
            val figiValue = annotation.figi
            val instrumentIdValue = annotation.instrumentUid

            check(tickerValue.isNotBlank() || figiValue.isNotBlank() || instrumentIdValue.isNotBlank()) {
                "At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided in $javaClassName"
            }
            val account = annotation.account
            check(account.isNotBlank()) {
                "Argument 'account' must be provided in $javaClassName"
            }
        }
        if (isAllHandleOrders) {
            val annotation = bean.javaClass.getAnnotation(HandleAllOrders::class.java)
            check(annotation.accounts.isNotEmpty()) {
                "$javaClassName: At least one element should be in 'accounts'"
            }
            annotation.accounts.forEach { account ->
                check(account.isNotBlank()) {
                    "$javaClassName: Account should be not blank"
                }
            }
            annotation.figies.takeIf { it.isNotEmpty() }?.forEach { figi ->
                check(figi.isNotBlank()) {
                    "$javaClassName: Figi should be not blank"
                }
            }
            annotation.tickers.takeIf { it.isNotEmpty() }?.forEach { ticker ->
                check(ticker.isNotBlank()) {
                    "$javaClassName: Ticker should be not blank"
                }
            }
            annotation.instrumentsUids.takeIf { it.isNotEmpty() }?.forEach { uId ->
                check(uId.isNotBlank()) {
                    "$javaClassName: InstrumentsUid should be not blank"
                }
            }
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean
}
