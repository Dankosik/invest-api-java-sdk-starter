package io.github.dankosik.starter.invest.exception

enum class ErrorCode(
    private val messageCode: String
) {

    HANDLER_NOT_FOUND("error: necessary Handler type not found"),
    STREAM_PROCESSOR_ADAPTER_NOT_FOUND("error: necessary StreamProcessorAdapter type not found");

    fun getMessage(): String = messageCode
}