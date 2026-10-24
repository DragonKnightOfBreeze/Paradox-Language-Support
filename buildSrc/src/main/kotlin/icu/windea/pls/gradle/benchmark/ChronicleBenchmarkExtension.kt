@file:Suppress("unused")

package icu.windea.pls.gradle.benchmark

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * `chronicle.benchmark` 插件的配置项。
 */
abstract class ChronicleBenchmarkExtension {
    /** JMH 版本。 */
    abstract val jmhVersion: Property<String>

    /** 基准启动器的全限定类名。 */
    abstract val launcherClass: Property<String>

    /** JMH 原始结果目录。 */
    abstract val resultDir: DirectoryProperty

    /** 带时间戳的报告输出目录。 */
    abstract val reportOutputDir: DirectoryProperty
}
