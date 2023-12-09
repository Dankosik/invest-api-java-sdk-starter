package io.github.dankosik.starter.invest.validation.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTrades
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTrade
import io.github.dankosik.starter.invest.contract.marketdata.trade.AsyncTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.BlockingTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.CoroutineTradeHandler
import io.github.dankosik.starter.invest.extension.addAllNotBlank
import mu.KLogging
import org.springframework.beans.factory.config.BeanPostProcessor

internal class TradesHandlerBeanPostProcessor : BeanPostProcessor {

    private val uniqueInstruments = mutableSetOf<String>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandleTrade = bean.javaClass.declaredAnnotations.filterIsInstance<HandleTrade>().isNotEmpty()
        val isAllHandleTrade = bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllTrades>().isNotEmpty()
        check(
            !(isHandleTrade
                    && (bean !is CoroutineTradeHandler && bean !is BlockingTradeHandler && bean !is AsyncTradeHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleTrade should be implements AsyncTradesHandler or BlockingTradesHandler or CoroutineTradesHandler" }
        check(
            !(isAllHandleTrade
                    && (bean !is CoroutineTradeHandler && bean !is BlockingTradeHandler && bean !is AsyncTradeHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleAllTrades should be implements AsyncTradesHandler or BlockingTradesHandler or CoroutineTradesHandler" }
        if (isHandleTrade) {
            val classNameInMessage = when (bean) {
                is CoroutineTradeHandler -> "CoroutineTradesHandler"
                is BlockingTradeHandler -> "BlockingTradesHandler"
                else -> "AsyncTradesHandler"
            }
            check(isHandleTrade) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandleTrades"
            }
            val annotation = bean.javaClass.getAnnotation(HandleTrade::class.java)
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

        if (isAllHandleTrade) {
            val annotation = bean.javaClass.getAnnotation(HandleAllTrades::class.java)
            if (annotation.beforeEachTradesHandler && annotation.afterEachTradesHandler) {
                logger.warn { "${bean.javaClass.name} any parameters of annotation 'HandleAllTrades' should be true, your handler will be ignored" }
            }
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean

    private companion object: KLogging()
}
