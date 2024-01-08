package io.github.dankosik.starter.invest.validation.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTrades
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTrade
import io.github.dankosik.starter.invest.contract.marketdata.trade.AsyncTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.BlockingTradeHandler
import io.github.dankosik.starter.invest.contract.marketdata.trade.CoroutineTradeHandler
import org.springframework.beans.factory.config.BeanPostProcessor

internal class TradesHandlerBeanPostProcessor : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandleTrade = bean.javaClass.declaredAnnotations.filterIsInstance<HandleTrade>().isNotEmpty()
        val isAllHandleTrade = bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllTrades>().isNotEmpty()
        val javaClassName = bean.javaClass.name
        check(
            !(isHandleTrade
                    && (bean !is CoroutineTradeHandler && bean !is BlockingTradeHandler && bean !is AsyncTradeHandler))
        ) { "Class: $javaClassName annotated with HandleTrade should implement AsyncTradesHandler or BlockingTradesHandler or CoroutineTradesHandler" }
        check(
            !(isAllHandleTrade
                    && (bean !is CoroutineTradeHandler && bean !is BlockingTradeHandler && bean !is AsyncTradeHandler))
        ) { "Class: $javaClassName annotated with HandleAllTrades should implement AsyncTradesHandler or BlockingTradesHandler or CoroutineTradesHandler" }
        if (isHandleTrade) {
            val classNameInMessage = when (bean) {
                is CoroutineTradeHandler -> "CoroutineTradesHandler"
                is BlockingTradeHandler -> "BlockingTradesHandler"
                else -> "AsyncTradesHandler"
            }
            check(isHandleTrade) {
                "$classNameInMessage: $javaClassName must have an annotated of HandleTrades"
            }
            val annotation = bean.javaClass.getAnnotation(HandleTrade::class.java)
            val tickerValue = annotation.ticker
            val figiValue = annotation.figi
            val instrumentIdValue = annotation.instrumentUid
            check(tickerValue.isNotBlank() || figiValue.isNotBlank() || instrumentIdValue.isNotBlank()) {
                "At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided in $javaClassName"
            }
        }

        if (isAllHandleTrade) {
            val annotation = bean.javaClass.getAnnotation(HandleAllTrades::class.java)
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
