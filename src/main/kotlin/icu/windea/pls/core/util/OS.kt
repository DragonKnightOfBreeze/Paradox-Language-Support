package icu.windea.pls.core.util

/**
 * 操作系统类型。
 *
 * 目前仅区分 `Windows` 与 `Linux`（类 Unix 统称）。
 * 通过伴生对象的静态字段便于快速获得当前平台信息。
 */
enum class OS {
    Windows,
    Linux,
    ;

    companion object {
        /** `os.name` 系统属性。*/
        @JvmField
        val name = System.getProperty("os.name")
        /** `os.version` 系统属性。*/
        @JvmField
        val version = System.getProperty("os.version")

        /**
         * 当前平台：如果名字为空或包含 "windows"（忽略大小写），视为 [Windows]，否则为 [Linux]。
         */
        @JvmField
        val value = if (name.isNullOrEmpty() || name.contains("windows", true)) Windows else Linux
    }
}
