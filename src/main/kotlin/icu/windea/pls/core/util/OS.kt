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
        val version = System.getProperty("os.version")

        @JvmField
        val value = if (name.isNullOrEmpty() || name.contains("windows", true)) Windows else Linux
    }
}
