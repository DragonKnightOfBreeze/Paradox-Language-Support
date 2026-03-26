package icu.windea.pls.core.execution

/**
 * 命令类型。
 */
enum class CommandType {
    /** 基于操作系统（Windows 使用 [POWER_SHELL]），类 Unix 使用 [SHELL]。 */
    AUTO,
    /** Windows cmd */
    CMD,
    /** Windows PowerShell */
    POWER_SHELL,
    /** 类 Unix Shell（如 `/bin/sh`） */
    SHELL,
    /** 直接使用输入的原始命令。 */
    NONE,
    ;
}
