package io.github.dankosik.starter.invest.validation.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTradingStatuses
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTradingStatus
import io.github.dankosik.starter.invest.contract.marketdata.status.AsyncTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.BlockingTradingStatusHandler
import io.github.dankosik.starter.invest.contract.marketdata.status.CoroutineTradingStatusHandler
import org.springframework.beans.factory.config.BeanPostProcessor

internal class TradingStatusBeanPostProcessor : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandleTradingStatus =
            bean.javaClass.declaredAnnotations.filterIsInstance<HandleTradingStatus>().isNotEmpty()
        val isAllHandleTradingStatus =
            bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllTradingStatuses>().isNotEmpty()
        val javaClassName = bean.javaClass.name
        check(
            !(isHandleTradingStatus
                    && (bean !is CoroutineTradingStatusHandler && bean !is BlockingTradingStatusHandler && bean !is AsyncTradingStatusHandler))
        ) { "Class: $javaClassName annotated with HandleTradingStatus should implement AsyncTradingStatusHandler or BlockingTradingStatusHandler or CoroutineTradingStatusHandler" }
        check(
            !(isAllHandleTradingStatus
                    && (bean !is CoroutineTradingStatusHandler && bean !is BlockingTradingStatusHandler && bean !is AsyncTradingStatusHandler))
        ) { "Class: $javaClassName annotated with of HandleAllTradingStatuses should implement AsyncTradingStatusHandler or BlockingTradingStatusHandler or CoroutineTradingStatusHandler" }
        if (isHandleTradingStatus) {
            val classNameInMessage = when (bean) {
                is CoroutineTradingStatusHandler -> "CoroutineTradingStatusHandler"
                is BlockingTradingStatusHandler -> "BlockingTradingStatusHandler"
                else -> "AsyncTradingStatusHandler"
            }
            check(isHandleTradingStatus) {
                "$classNameInMessage: $javaClassName must have an annotated of HandleTradingStatus"
            }
            val annotation = bean.javaClass.getAnnotation(HandleTradingStatus::class.java)
            val tickerValue = annotation.ticker
            val figiValue = annotation.figi
            val instrumentIdValue = annotation.instrumentUid
            check(tickerValue.isNotBlank() || figiValue.isNotBlank() || instrumentIdValue.isNotBlank()) {
                "At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided in $javaClassName"
            }
        }

        if (isAllHandleTradingStatus) {
            val annotation = bean.javaClass.getAnnotation(HandleAllTradingStatuses::class.java)
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
