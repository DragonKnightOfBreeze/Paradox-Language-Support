package icu.windea.pls.core.util.console

/**
 * 命令类型。
 */
enum class CommandType {
    /** Windows `cmd` */
    CMD,
    /** Windows PowerShell */
    POWER_SHELL,
    /** 类 Unix Shell（如 `/bin/sh`） */
    SHELL,
    ;
}
