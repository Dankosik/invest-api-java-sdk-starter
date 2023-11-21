package io.github.dankosik.starter.invest.processor.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleLastPrice
import io.github.dankosik.starter.invest.contract.lastprice.AsyncLastPriceHandler
import io.github.dankosik.starter.invest.contract.lastprice.BlockingLastPriceHandler
import io.github.dankosik.starter.invest.contract.lastprice.CoroutineLastPriceHandler
import io.github.dankosik.starter.invest.extension.addAllNotBlank
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class LastPriceBeanPostProcessor : BeanPostProcessor {

    private val uniqueInstruments = mutableSetOf<String>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        check(
            !(bean.javaClass.declaredAnnotations.filterIsInstance<HandleLastPrice>().isNotEmpty()
                    && (bean !is CoroutineLastPriceHandler && bean !is BlockingLastPriceHandler && bean !is AsyncLastPriceHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleLastPrice should be implements AsyncLastPriceHandler or BlockingTradesHandler or CoroutineTradesHandler" }
        if (bean is CoroutineLastPriceHandler || bean is BlockingLastPriceHandler || bean is AsyncLastPriceHandler) {
            val classNameInMessage = when (bean) {
                is CoroutineLastPriceHandler -> "CoroutineLastPriceHandler"
                is BlockingLastPriceHandler -> "BlockingLastPriceHandler"
                else -> "AsyncLastPriceHandler"
            }
            check(bean.javaClass.declaredAnnotations.filterIsInstance<HandleLastPrice>().isNotEmpty()) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandleLastPrice"
            }
            val tickerValue = bean.javaClass.getAnnotation(HandleLastPrice::class.java).ticker
            val figiValue = bean.javaClass.getAnnotation(HandleLastPrice::class.java).figi
            val instrumentIdValue = bean.javaClass.getAnnotation(HandleLastPrice::class.java).instrumentUid
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
