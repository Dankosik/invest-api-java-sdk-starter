package io.github.dankosik.starter.invest.validation.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTradingStatuses
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTradingStatus
import io.github.dankosik.starter.invest.contract.marketdata.status.AsyncTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.BlockingTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.CoroutineTradingStatusHandler
import io.github.dankosik.starter.invest.extension.addAllNotBlank
import mu.KLogging
import org.springframework.beans.factory.config.BeanPostProcessor

internal class TradingStatusBeanPostProcessor : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandleTradingStatus =
            bean.javaClass.declaredAnnotations.filterIsInstance<HandleTradingStatus>().isNotEmpty()
        val isAllHandleTradingStatus =
            bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllTradingStatuses>().isNotEmpty()
        check(
            !(isHandleTradingStatus
                    && (bean !is CoroutineTradingStatusHandler && bean !is BlockingTradingStatusHandler && bean !is AsyncTradingStatusHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleTradingStatus should be implements AsyncTradingStatusHandler or BlockingTradingStatusHandler or CoroutineTradingStatusHandler" }
        check(
            !(isAllHandleTradingStatus
                    && (bean !is CoroutineTradingStatusHandler && bean !is BlockingTradingStatusHandler && bean !is AsyncTradingStatusHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleAllTradingStatuses should be implements AsyncTradingStatusHandler or BlockingTradingStatusHandler or CoroutineTradingStatusHandler" }
        if (isHandleTradingStatus) {
            val classNameInMessage = when (bean) {
                is CoroutineTradingStatusHandler -> "CoroutineTradingStatusHandler"
                is BlockingTradingStatusHandler -> "BlockingTradingStatusHandler"
                else -> "AsyncTradingStatusHandler"
            }
            check(isHandleTradingStatus) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandleTradingStatus"
            }
            val annotation = bean.javaClass.getAnnotation(HandleTradingStatus::class.java)
            val tickerValue = annotation.ticker
            val figiValue = annotation.figi
            val instrumentIdValue = annotation.instrumentUid
            check(tickerValue.isNotBlank() || figiValue.isNotBlank() || instrumentIdValue.isNotBlank()) {
                "At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided in ${bean.javaClass.name}"
            }
        }

        if (isAllHandleTradingStatus) {
            val annotation = bean.javaClass.getAnnotation(HandleAllTradingStatuses::class.java)
            if (annotation.beforeEachTradingStatusHandler && annotation.afterEachTradingStatusHandler) {
                logger.warn { "${bean.javaClass.name} any parameters of annotation 'HandleAllTradingStatuses' should be true, your handler will be ignored" }
            }
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean

    private companion object : KLogging()
}
