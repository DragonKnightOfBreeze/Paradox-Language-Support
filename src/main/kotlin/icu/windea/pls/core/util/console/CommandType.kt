package icu.windea.pls.core.util.console

/**
 * 命令类型。
 *
 * - [CMD]：Windows `cmd`；
 * - [POWER_SHELL]：Windows PowerShell；
 * - [SHELL]：类 Unix Shell（如 `/bin/sh`）。
 */
enum class CommandType {
    CMD,
    POWER_SHELL,
    SHELL,
    ;
}
