package io.github.dankosik.starter.invest.validation.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllLastPrices
import io.github.dankosik.starter.invest.annotation.marketdata.HandleLastPrice
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.AsyncLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.BlockingLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.CoroutineLastPriceHandler
import io.github.dankosik.starter.invest.extension.addAllNotBlank
import mu.KLogging
import org.springframework.beans.factory.config.BeanPostProcessor

internal class LastPriceBeanPostProcessor : BeanPostProcessor {

    private val uniqueInstruments = mutableSetOf<String>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandleLastPrice = bean.javaClass.declaredAnnotations.filterIsInstance<HandleLastPrice>().isNotEmpty()
        val isAllHandleLastPrice =
            bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllLastPrices>().isNotEmpty()
        check(
            !(isHandleLastPrice
                    && (bean !is CoroutineLastPriceHandler && bean !is BlockingLastPriceHandler && bean !is AsyncLastPriceHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleLastPrice should be implements AsyncLastPriceHandler or BlockingLastPriceHandler or CoroutineLastPriceHandler" }
        check(
            !(isAllHandleLastPrice
                    && (bean !is CoroutineLastPriceHandler && bean !is BlockingLastPriceHandler && bean !is AsyncLastPriceHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleAllLastPrices should be implements AsyncLastPriceHandler or BlockingLastPriceHandler or CoroutineLastPriceHandler" }
        if (isHandleLastPrice) {
            val classNameInMessage = when (bean) {
                is CoroutineLastPriceHandler -> "CoroutineLastPriceHandler"
                is BlockingLastPriceHandler -> "BlockingLastPriceHandler"
                else -> "AsyncLastPriceHandler"
            }
            check(isHandleLastPrice) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandleLastPrice"
            }
            val annotation = bean.javaClass.getAnnotation(HandleLastPrice::class.java)
            val tickerValue = annotation.ticker
            val figiValue = annotation.figi
            val instrumentIdValue = annotation.instrumentUid
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

        if (isAllHandleLastPrice) {
            val annotation = bean.javaClass.getAnnotation(HandleAllLastPrices::class.java)
            if (annotation.beforeEachLastPriceHandler && annotation.afterEachLastPriceHandler) {
                logger.warn { "${bean.javaClass.name} any parameters of annotation 'HandleAllLastPrices' should be true, your handler will be ignored" }
            }
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean

    private companion object : KLogging()
}
