package io.github.dankosik.starter.invest.processor.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleTrades
import io.github.dankosik.starter.invest.contract.trade.AsyncTradesHandler
import io.github.dankosik.starter.invest.contract.trade.BlockingTradesHandler
import io.github.dankosik.starter.invest.contract.trade.CoroutineTradesHandler
import io.github.dankosik.starter.invest.extension.addAllNotBlank
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class TradesHandlerBeanPostProcessor : BeanPostProcessor {

    private val uniqueInstruments = mutableSetOf<String>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        check(
            !(bean.javaClass.declaredAnnotations.filterIsInstance<HandleTrades>().isNotEmpty()
                    && (bean !is CoroutineTradesHandler && bean !is BlockingTradesHandler && bean !is AsyncTradesHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleTrades should be implements AsyncTradesHandler or BlockingTradesHandler or CoroutineTradesHandler" }
        if (bean is CoroutineTradesHandler || bean is BlockingTradesHandler || bean is AsyncTradesHandler) {
            val classNameInMessage = when (bean) {
                is CoroutineTradesHandler -> "CoroutineTradesHandler"
                is BlockingTradesHandler -> "BlockingTradesHandler"
                else -> "AsyncTradesHandler"
            }
            check(bean.javaClass.declaredAnnotations.filterIsInstance<HandleTrades>().isNotEmpty()) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandleTrades"
            }
            val tickerValue = bean.javaClass.getAnnotation(HandleTrades::class.java).ticker
            val figiValue = bean.javaClass.getAnnotation(HandleTrades::class.java).figi
            val instrumentIdValue = bean.javaClass.getAnnotation(HandleTrades::class.java).instrumentUid
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
