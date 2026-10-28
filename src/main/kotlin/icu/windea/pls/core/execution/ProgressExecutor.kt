@file:Suppress("unused")

package icu.windea.pls.core.execution

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.util.ExceptionUtil
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 外部命令执行器。
 *
 * 基于 [GeneralCommandLine] 和 [CapturingProcessHandler] 实现，从而取得更好的兼容性和性能。
 * 例如命令解析、取消检查和超时限制。
 *
 * 说明：
 * - 默认绑定构造时的当前 [ProgressIndicator]，从而尽可能支持取消。
 * - 默认不设置超时，调用点应按需设置超时。
 * - 为了与 [GeneralCommandLine] 对齐，配置方法仍然返回当前实例；
 *   因此它不是线程安全对象，也不应在多个线程间共享。
 * - 不会硬性禁止在 EDT 或 readAction 中执行，因为这可能影响不同运行环境下的可用性；
 *   如果检测到错误的执行上下文，会输出一次带调用栈的警告。
 *
 * @see ExecutionTimeouts
 */
class ProgressExecutor(
    val commandLine: GeneralCommandLine
) {
    companion object {
        private val logger = logger<ProgressExecutor>()
        private val warnedStacks = ConcurrentHashMap.newKeySet<String>()
    }

    private var indicator: ProgressIndicator? = ProgressIndicatorProvider.getGlobalProgressIndicator()
    private var timeout: Duration = Duration.INFINITE
    private var destroyOnTimeout: Boolean = true

    /**
     * 使用指定的 indicator 进行执行，从而支持取消。
     */
    fun withIndicator(indicator: ProgressIndicator): ProgressExecutor = apply { this.indicator = indicator }

    /**
     * 不使用 indicator 进行执行，此时仅能通过超时限制执行时间。
     */
    fun withoutIndicator(): ProgressExecutor = apply { this.indicator = null }

    /**
     * 设置执行超时。未设置或设置为无限时，表示不设置超时。
     */
    fun withTimeout(timeout: Duration): ProgressExecutor = apply { this.timeout = timeout }

    /**
     * 设置执行超时（毫秒）。`0` 表示不设置超时。
     */
    fun withTimeoutMillis(timeoutMillis: Int): ProgressExecutor = apply { this.timeout = timeoutMillis.milliseconds }

    /**
     * 设置超时后是否销毁进程。为了与 [CapturingProcessHandler] 对齐，默认销毁。
     */
    fun withDestroyOnTimeout(destroyOnTimeout: Boolean): ProgressExecutor = apply { this.destroyOnTimeout = destroyOnTimeout }

    /**
     * 阻塞执行外部命令，并返回进程输出。
     *
     * 说明：
     * - 这是一个阻塞方法，通常应在后台线程中调用；这不属于硬性限制，如果检测到 EDT/readAction，会输出警告。
     *
     * @see CapturingProcessHandler.runProcessWithProgressIndicator
     */
    @RequiresBackgroundThread(generateAssertion = false)
    fun execute(): ProcessOutput {
        warnIfIncorrectContext()
        val commandLineString = commandLine.commandLineString
        logger.debug("Executing command line: $commandLineString")
        val timeoutMillis = timeout.toTimeoutMillis()
        val processHandler = CapturingProcessHandler(commandLine)
        val output = indicator?.let {
            processHandler.runProcessWithProgressIndicator(it, timeoutMillis, destroyOnTimeout)
        } ?: processHandler.runProcess(timeoutMillis, destroyOnTimeout)
        logger.debug("Command line finished: exitCode=${output.exitCode}, timeout=${output.isTimeout}, cancelled=${output.isCancelled}")
        return output
    }

    private fun Duration.toTimeoutMillis(): Int {
        if (isInfinite()) return 0
        return inWholeMilliseconds.coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
    }

    private fun warnIfIncorrectContext() {
        val application = ApplicationManager.getApplication() ?: return
        val message = when {
            application.isDispatchThread -> "on EDT"
            application.holdsReadLock() -> "under readAction"
            else -> return
        }
        val stack = ExceptionUtil.currentStackTrace()
        if (!warnedStacks.add(stack)) return
        logger.warn(
            "Command line is executed $message: ${commandLine.commandLineString}. " +
                "See `OSProcessHandler.checkEdtAndReadAction()` for details."
        )
    }
}
