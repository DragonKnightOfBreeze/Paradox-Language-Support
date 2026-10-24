@file:Suppress("unused")

package icu.windea.pls.gradle.benchmark

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.options.Option

/**
 * 运行 JMH 基准测试的任务。
 */
abstract class ChronicleBenchmarkTask : JavaExec() {
    @get:Option(option = "benchmarks", description = "Benchmark filters, like --tests.")
    @get:Input
    abstract val benchmarks: ListProperty<String>
}
