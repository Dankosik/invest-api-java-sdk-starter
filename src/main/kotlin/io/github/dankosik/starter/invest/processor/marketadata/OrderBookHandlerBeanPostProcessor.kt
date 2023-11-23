package io.github.dankosik.starter.invest.processor.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleOrderBook
import io.github.dankosik.starter.invest.contract.orderbook.AsyncOrderBookHandler
import io.github.dankosik.starter.invest.contract.orderbook.BlockingOrderBookHandler
import io.github.dankosik.starter.invest.contract.orderbook.CoroutineOrderBookHandler
import io.github.dankosik.starter.invest.extension.addAllNotBlank
import org.springframework.beans.factory.config.BeanPostProcessor

internal class OrderBookHandlerBeanPostProcessor : BeanPostProcessor {

    private val uniqueInstruments = mutableSetOf<String>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        check(
            !(bean.javaClass.declaredAnnotations.filterIsInstance<HandleOrderBook>().isNotEmpty()
                    && (bean !is CoroutineOrderBookHandler && bean !is BlockingOrderBookHandler && bean !is AsyncOrderBookHandler))
        ) {
            "Class: ${bean.javaClass.name} that annotated of HandleOrderBook should be implements AsyncOrderBookHandler or BlockingOrderBookHandler or CoroutineOrderBookHandler"
        }
        if (bean is CoroutineOrderBookHandler || bean is BlockingOrderBookHandler || bean is AsyncOrderBookHandler) {
            val classNameInMessage = when (bean) {
                is CoroutineOrderBookHandler -> "CoroutineOrderBookHandler"
                is AsyncOrderBookHandler -> "AsyncOrderBookHandler"
                else -> "BlockingOrderBookHandler"
            }
            check(bean.javaClass.declaredAnnotations.filterIsInstance<HandleOrderBook>().isNotEmpty()) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandleOrderBook"
            }
            val tickerValue = bean.javaClass.getAnnotation(HandleOrderBook::class.java).ticker
            val figiValue = bean.javaClass.getAnnotation(HandleOrderBook::class.java).figi
            val instrumentIdValue = bean.javaClass.getAnnotation(HandleOrderBook::class.java).instrumentUid
            check(tickerValue.isNotBlank() || figiValue.isNotBlank() || instrumentIdValue.isNotBlank()) {
                "At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided in ${bean.javaClass.name}"
            }
            check(tickerValue !in uniqueInstruments) {
                "Duplicate ticker value found: $tickerValue for bean: ${bean.javaClass.name}"
            }
            check(instrumentIdValue !in uniqueInstruments) {
                "Duplicate instrumentIdValue value found: $instrumentIdValue for bean: ${bean.javaClass.name}"
            }
            check(figiValue !in uniqueInstruments) {
                "Duplicate figiValue value found: $figiValue for bean: ${bean.javaClass.name}"
            }
            uniqueInstruments.addAllNotBlank(listOf(tickerValue, figiValue, instrumentIdValue))
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean
}