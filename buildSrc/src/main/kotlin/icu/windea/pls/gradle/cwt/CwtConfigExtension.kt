@file:Suppress("unused")

package icu.windea.pls.gradle.cwt

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * `chronicle.cwt-config` 插件的配置项。
 */
abstract class CwtConfigExtension {
    /** 本地仓库缺失时，是否尝试下载对应的 ZIP。 */
    abstract val downloadIfMissing: Property<Boolean>

    /** 下载时是否接受任意证书。仅用于临时的 SSL 握手问题排查。 */
    abstract val acceptAnyCertificate: Property<Boolean>

    /** CWT 配置仓库列表。 */
    abstract val repositories: ListProperty<CwtRepository>

    /** 是否将 CWT 配置包含到插件 JAR 中。 */
    abstract val includeInJar: Property<Boolean>
}
