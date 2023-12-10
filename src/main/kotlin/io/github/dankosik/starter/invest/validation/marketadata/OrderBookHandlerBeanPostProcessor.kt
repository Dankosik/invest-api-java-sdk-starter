package io.github.dankosik.starter.invest.validation.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllOrderBooks
import io.github.dankosik.starter.invest.annotation.marketdata.HandleOrderBook
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.AsyncOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.BlockingOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.CoroutineOrderBookHandler
import io.github.dankosik.starter.invest.extension.addAllNotBlank
import mu.KLogging
import org.springframework.beans.factory.config.BeanPostProcessor

internal class OrderBookHandlerBeanPostProcessor : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandleOrderBook = bean.javaClass.declaredAnnotations.filterIsInstance<HandleOrderBook>().isNotEmpty()
        val isAllHandleOrderBook =
            bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllOrderBooks>().isNotEmpty()
        check(
            !(isHandleOrderBook
                    && (bean !is CoroutineOrderBookHandler && bean !is BlockingOrderBookHandler && bean !is AsyncOrderBookHandler))
        ) {
            "Class: ${bean.javaClass.name} that annotated of HandleOrderBook should be implements AsyncOrderBookHandler or BlockingOrderBookHandler or CoroutineOrderBookHandler"
        }
        check(
            !(isAllHandleOrderBook
                    && (bean !is CoroutineOrderBookHandler && bean !is BlockingOrderBookHandler && bean !is AsyncOrderBookHandler))
        ) {
            "Class: ${bean.javaClass.name} that annotated of HandleAllOrderBooks should be implements AsyncOrderBookHandler or BlockingOrderBookHandler or CoroutineOrderBookHandler"
        }
        if (isHandleOrderBook) {
            val classNameInMessage = when (bean) {
                is CoroutineOrderBookHandler -> "CoroutineOrderBookHandler"
                is AsyncOrderBookHandler -> "AsyncOrderBookHandler"
                else -> "BlockingOrderBookHandler"
            }
            check(isHandleOrderBook) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandleOrderBook"
            }
            val tickerValue = bean.javaClass.getAnnotation(HandleOrderBook::class.java).ticker
            val figiValue = bean.javaClass.getAnnotation(HandleOrderBook::class.java).figi
            val instrumentIdValue = bean.javaClass.getAnnotation(HandleOrderBook::class.java).instrumentUid
            check(tickerValue.isNotBlank() || figiValue.isNotBlank() || instrumentIdValue.isNotBlank()) {
                "At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided in ${bean.javaClass.name}"
            }
        }

        if (isAllHandleOrderBook) {
            val annotation = bean.javaClass.getAnnotation(HandleAllOrderBooks::class.java)
            if (annotation.beforeEachOrderBookHandler && annotation.afterEachOrderBookHandler) {
                logger.warn { "${bean.javaClass.name} any parameters of annotation 'HandleAllOrderBooks' should be true, your handler will be ignored" }
            }
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean

    private companion object : KLogging()
}