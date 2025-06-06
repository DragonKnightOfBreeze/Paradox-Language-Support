@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.core

object OS {
    val name by lazy { System.getProperty("os.name") }

    val isWindows by lazy { name.isNullOrEmpty() || name.contains("windows", true) }
    val isLinux by lazy { name.isNotNullOrEmpty() && name.contains("linux", true) }
}
