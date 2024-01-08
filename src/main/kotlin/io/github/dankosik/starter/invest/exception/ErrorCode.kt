package io.github.dankosik.starter.invest.exception

enum class ErrorCode(
    private val messageCode: String
) {

    HANDLER_NOT_FOUND("Starter error: necessary Handler type not found"),
    STREAM_PROCESSOR_ADAPTER_NOT_FOUND("Starter error: necessary StreamProcessorAdapter type not found"),
    INSTRUMENT_NOT_FOUND("Starter error: instrument not found");

    fun getMessage(): String = messageCode
}