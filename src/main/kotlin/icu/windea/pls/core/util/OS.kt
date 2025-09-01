package icu.windea.pls.core.util

/**
 * 运行时操作系统枚举。
 *
 * 当前仅区分 `Windows` 与 `Linux`（非 Windows 皆视为 Linux）。
 */
enum class OS {
    Windows,
    Linux,
    ;

    companion object {
        /** 操作系统名称（`os.name`）。 */
        @JvmField
        val name = System.getProperty("os.name")
        /** 操作系统版本（`os.version`）。 */
        @JvmField
        val version = System.getProperty("os.version")

        /** 推断的操作系统枚举值。 */
        @JvmField
        val value = if (name.isNullOrEmpty() || name.contains("windows", true)) Windows else Linux
    }
}
