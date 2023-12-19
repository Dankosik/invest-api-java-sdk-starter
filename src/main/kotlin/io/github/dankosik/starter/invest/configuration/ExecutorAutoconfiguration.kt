package io.github.dankosik.starter.invest.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnJava
import org.springframework.boot.system.JavaVersion
import org.springframework.context.annotation.Bean
import org.springframework.core.task.SimpleAsyncTaskExecutor

@AutoConfiguration
class ExecutorAutoconfiguration {

    @Bean
    @ConditionalOnJava(JavaVersion.TWENTY_ONE, range = ConditionalOnJava.Range.EQUAL_OR_NEWER)
    fun executor() = SimpleAsyncTaskExecutor("virtualThreadExecutor-")
        .also { it.setVirtualThreads(true) }
}