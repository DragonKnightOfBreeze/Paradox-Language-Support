package icu.windea.pls.integrations

/**
 * 提供外部工具。
 *
 * 说明：
 * - 其中的操作方法（operation methods）可能抛出 [UnsupportedOperationException]。例如，不支持特定操作。
 * - 其中的操作方法（operation methods）也可能抛出某些其他异常。例如，命令行执行失败。
 */
interface ExternalToolProvider {
    val name: String

    fun isAvailable(): Boolean
}
