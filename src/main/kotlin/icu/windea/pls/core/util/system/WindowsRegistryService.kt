package icu.windea.pls.core.util.system

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.util.SystemInfo
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg
import icu.windea.pls.core.execution.ExecutionTimeouts
import icu.windea.pls.core.execution.ProcessExecutor
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runCatchingCancelable
import java.nio.charset.Charset

object WindowsRegistryService {
    /**
     * 读取 Windows 注册表中的字符串值。
     *
     * 说明：
     * - 优先使用 JNA。参见 [Advapi32Util]。
     * - 如果 JNA 不可用或读取失败，则回退到 `reg query`。参见 [ProcessExecutor]。
     *
     * @param key 注册表路径。使用 `\` 作为分隔符。
     * @param valueName 要读取的值的名字。
     */
    fun getStringValue(key: String, valueName: String): String? {
        if (!SystemInfo.isWindows) return null
        runCatchingCancelable { getStringValueFromJna(key, valueName) }.getOrNull()?.orNull()?.let { return it }
        runCatchingCancelable { getStringValueFromCommandLine(key, valueName) }.getOrNull()?.orNull()?.let { return it }
        return null
    }

    private fun getStringValueFromJna(key: String, valueName: String): String? {
        return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, key, valueName)
    }

    private fun getStringValueFromCommandLine(key: String, valueName: String): String? {
        val commandLine = GeneralCommandLine("reg", "query", "HKLM\\$key", "/v", valueName).withCharset(Charset.defaultCharset())
        val output = ProcessExecutor(commandLine).withTimeout(ExecutionTimeouts.short).execute()
        if (output.isCancelled || output.isTimeout || output.exitCode != 0) return null
        val pattern = Regex("""(?im)^\s*${Regex.escape(valueName)}\s+REG_\w+\s+(.+?)\s*$""")
        return pattern.find(output.stdout)?.groupValues?.get(1)
    }
}
