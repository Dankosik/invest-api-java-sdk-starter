package io.github.dankosik.starter.invest.validation.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllOrderBooks
import io.github.dankosik.starter.invest.annotation.marketdata.HandleOrderBook
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.AsyncOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.BlockingOrderBookHandler
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.CoroutineOrderBookHandler
import org.springframework.beans.factory.config.BeanPostProcessor

internal class OrderBookHandlerBeanPostProcessor : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandleOrderBook = bean.javaClass.declaredAnnotations.filterIsInstance<HandleOrderBook>().isNotEmpty()
        val isAllHandleOrderBook =
            bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllOrderBooks>().isNotEmpty()
        val javaClassName = bean.javaClass.name
        check(
            !(isHandleOrderBook
                    && (bean !is CoroutineOrderBookHandler && bean !is BlockingOrderBookHandler && bean !is AsyncOrderBookHandler))
        ) {
            "Class: $javaClassName annotated with HandleOrderBook should implement AsyncOrderBookHandler or BlockingOrderBookHandler or CoroutineOrderBookHandler"
        }
        check(
            !(isAllHandleOrderBook
                    && (bean !is CoroutineOrderBookHandler && bean !is BlockingOrderBookHandler && bean !is AsyncOrderBookHandler))
        ) {
            "Class: $javaClassName annotated with HandleAllOrderBooks should implement AsyncOrderBookHandler or BlockingOrderBookHandler or CoroutineOrderBookHandler"
        }
        if (isHandleOrderBook) {
            val classNameInMessage = when (bean) {
                is CoroutineOrderBookHandler -> "CoroutineOrderBookHandler"
                is AsyncOrderBookHandler -> "AsyncOrderBookHandler"
                else -> "BlockingOrderBookHandler"
            }
            check(isHandleOrderBook) {
                "$classNameInMessage: $javaClassName must have an annotated of HandleOrderBook"
            }
            val tickerValue = bean.javaClass.getAnnotation(HandleOrderBook::class.java).ticker
            val figiValue = bean.javaClass.getAnnotation(HandleOrderBook::class.java).figi
            val instrumentIdValue = bean.javaClass.getAnnotation(HandleOrderBook::class.java).instrumentUid
            check(tickerValue.isNotBlank() || figiValue.isNotBlank() || instrumentIdValue.isNotBlank()) {
                "At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided in $javaClassName"
            }
        }

        if (isAllHandleOrderBook) {
            val annotation = bean.javaClass.getAnnotation(HandleAllOrderBooks::class.java)
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