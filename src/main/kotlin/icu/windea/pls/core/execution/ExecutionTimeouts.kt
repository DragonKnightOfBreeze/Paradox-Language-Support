@file:Suppress("unused")

package icu.windea.pls.core.execution

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * 常用的命令执行超时时间。
 *
 * 备注：默认不设置超时；具体调用点应根据场景显式使用这些预定义值。
 */
object ExecutionTimeouts {
    /** 适用于快速的版本检查等场景。 */
    val short: Duration = 5.seconds

    /** 适用于常规命令执行。 */
    val normal: Duration = 30.seconds

    /** 适用于可能耗时较长的命令执行。 */
    val long: Duration = 2.minutes

    /** 适用于外部检查工具、克隆仓库等长时间运行的任务。 */
    val veryLong: Duration = 10.minutes
}
