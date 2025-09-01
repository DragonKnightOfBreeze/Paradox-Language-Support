package icu.windea.pls.core.util.console

/**
 * 命令执行的类型。
 *
 * - `CMD`：Windows 命令提示符。
 * - `POWER_SHELL`：Windows PowerShell。
 * - `SHELL`：类 Unix 系统的 `/bin/sh`。
 */
enum class CommandType {
    CMD,
    POWER_SHELL,
    SHELL,
    ;
}
