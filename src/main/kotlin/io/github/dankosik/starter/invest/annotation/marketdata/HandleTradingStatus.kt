package io.github.dankosik.starter.invest.annotation.marketdata

import io.github.dankosik.starter.invest.contract.marketdata.status.BaseTradingStatusHandler
import org.springframework.stereotype.Service
import ru.tinkoff.piapi.contract.v1.InstrumentType

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleTradingStatus(
    val ticker: String = "",
    val figi: String = "",
    val instrumentUid: String = "",
    val sandboxOnly: Boolean = false,
    val instrumentType: InstrumentType = InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED
)

fun List<BaseTradingStatusHandler>.extractTickersWithoutInstrumentType() =
    this.map { it.javaClass.getAnnotation(HandleTradingStatus::class.java) }
        .filter { it.instrumentType == InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED }
        .map { it.ticker }

fun List<BaseTradingStatusHandler>.extractTickerToInstrumentTypeMap() =
    this.map { it.javaClass.getAnnotation(HandleTradingStatus::class.java) }
        .filter { it.ticker.isNotBlank() }
        .filter { it.instrumentType != InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED }
        .associateBy(keySelector = { it.ticker }, valueTransform = { it.instrumentType })

fun List<BaseTradingStatusHandler>.extractFigiToInstrumentTypeMap() =
    this.map { it.javaClass.getAnnotation(HandleTradingStatus::class.java) }
        .filter { it.figi.isNotBlank() }
        .filter { it.instrumentType != InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED }
        .associateBy(keySelector = { it.figi }, valueTransform = { it.instrumentType })

fun List<BaseTradingStatusHandler>.extractUidToInstrumentTypeMap() =
    this.map { it.javaClass.getAnnotation(HandleTradingStatus::class.java) }
        .filter { it.instrumentUid.isNotBlank() }
        .filter { it.instrumentType != InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED }
        .associateBy(keySelector = { it.instrumentUid }, valueTransform = { it.instrumentType })