package io.github.dankosik.starter.invest.processor.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleTradingStatus
import io.github.dankosik.starter.invest.contract.status.AsyncTradingStatusHandler
import io.github.dankosik.starter.invest.contract.status.BlockingTradingStatusHandler
import io.github.dankosik.starter.invest.contract.status.CoroutineTradingStatusHandler
import io.github.dankosik.starter.invest.extension.addAllNotBlank
import org.springframework.beans.factory.config.BeanPostProcessor

internal class TradingStatusBeanPostProcessor : BeanPostProcessor {

    private val uniqueInstruments = mutableSetOf<String>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        check(
            !(bean.javaClass.declaredAnnotations.filterIsInstance<HandleTradingStatus>().isNotEmpty()
                    && (bean !is CoroutineTradingStatusHandler && bean !is BlockingTradingStatusHandler && bean !is AsyncTradingStatusHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleTradingStatus should be implements AsyncTradingStatusHandler or BlockingTradingStatusHandler or CoroutineTradingStatusHandler" }
        if (bean is CoroutineTradingStatusHandler || bean is BlockingTradingStatusHandler || bean is AsyncTradingStatusHandler) {
            val classNameInMessage = when (bean) {
                is CoroutineTradingStatusHandler -> "CoroutineTradingStatusHandler"
                is BlockingTradingStatusHandler -> "BlockingTradingStatusHandler"
                else -> "AsyncTradingStatusHandler"
            }
            check(bean.javaClass.declaredAnnotations.filterIsInstance<HandleTradingStatus>().isNotEmpty()) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandleTradingStatus"
            }
            val tickerValue = bean.javaClass.getAnnotation(HandleTradingStatus::class.java).ticker
            val figiValue = bean.javaClass.getAnnotation(HandleTradingStatus::class.java).figi
            val instrumentIdValue = bean.javaClass.getAnnotation(HandleTradingStatus::class.java).instrumentUid
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
