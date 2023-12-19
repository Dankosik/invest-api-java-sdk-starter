package io.github.dankosik.starter.invest.validation.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTrades
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTrade
import io.github.dankosik.starter.invest.contract.marketdata.trade.AsyncTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.BlockingTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.CoroutineTradeHandler
import mu.KLogging
import org.springframework.beans.factory.config.BeanPostProcessor

internal class TradesHandlerBeanPostProcessor : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandleTrade = bean.javaClass.declaredAnnotations.filterIsInstance<HandleTrade>().isNotEmpty()
        val isAllHandleTrade = bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllTrades>().isNotEmpty()
        check(
            !(isHandleTrade
                    && (bean !is CoroutineTradeHandler && bean !is BlockingTradeHandler && bean !is AsyncTradeHandler))
        ) { "Class: ${bean.javaClass.name} annotated with HandleTrade should implement AsyncTradesHandler or BlockingTradesHandler or CoroutineTradesHandler" }
        check(
            !(isAllHandleTrade
                    && (bean !is CoroutineTradeHandler && bean !is BlockingTradeHandler && bean !is AsyncTradeHandler))
        ) { "Class: ${bean.javaClass.name} annotated with HandleAllTrades should implement AsyncTradesHandler or BlockingTradesHandler or CoroutineTradesHandler" }
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
