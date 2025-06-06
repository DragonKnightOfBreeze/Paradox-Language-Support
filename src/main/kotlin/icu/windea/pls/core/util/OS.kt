@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.core

enum class OS {
    Windows,
    Linux,
    ;

    companion object {
        @JvmField
        val name = System.getProperty("os.name")

        @JvmField
        val isWindows = name.isNullOrEmpty() || name.contains("windows", true)
        @JvmField
        val isLinux = name.isNotNullOrEmpty() && name.contains("linux", true)
    }
}
