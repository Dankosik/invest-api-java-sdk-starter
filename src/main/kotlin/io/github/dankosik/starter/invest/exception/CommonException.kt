package io.github.dankosik.starter.invest.exception

data class CommonException(val code: ErrorCode) : RuntimeException(code.getMessage())