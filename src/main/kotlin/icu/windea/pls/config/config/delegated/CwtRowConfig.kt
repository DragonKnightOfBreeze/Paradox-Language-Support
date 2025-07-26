@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*

/**
 * @property name string
 * @property paths (property) path: string
 * @property pathFile (property) path_file: string
 * @property pathExtension (property) path_extension: string
 * @property pathStrict (property) path_strict: boolean
 * @property pathPatterns (property*) path_pattern: string
 * @property columnConfigs 各个列对应的规则。
 */
interface CwtRowConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtFilePathMatchableConfig {
    val name: String
    override val paths: Set<String>
    override val pathFile: String?
    override val pathExtension: String?
    override val pathStrict: Boolean
    override val pathPatterns: Set<String>
    val columnConfigs: Map<String, CwtMemberConfig<*>>

    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtRowConfig? = doResolve(config)
    }
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtRowConfig? {
    val name = config.key.removeSurroundingOrNull("row[", "]")?.orNull() ?: return null
    val paths = sortedSetOf<String>()
    var pathFile: String? = null
    var pathExtension: String? = null
    var pathStrict = false
    val pathPatterns = sortedSetOf<String>()
    val columnConfigs = mutableMapOf<String, CwtPropertyConfig>()

    val props = config.properties.orEmpty()
    if (props.isEmpty()) return null
    for (prop in props) {
        when (prop.key) {
            "path" -> prop.stringValue?.removePrefix("game/")?.normalizePath()?.let { paths += it.intern() }
            "path_file" -> pathFile = prop.stringValue ?: continue
            "path_extension" -> pathExtension = prop.stringValue?.removePrefix(".")?.intern() ?: continue
            "path_strict" -> pathStrict = prop.booleanValue ?: continue
            "path_pattern" -> prop.stringValue?.removePrefix("game/")?.normalizePath()?.let { pathPatterns += it.intern() }
            "columns" -> prop.properties.orEmpty().forEach { c -> columnConfigs[c.key] = c }
        }
    }

    return CwtRowConfigImpl(
        config, name,
        paths.optimized(), pathFile, pathExtension, pathStrict, pathPatterns.optimized(),
        columnConfigs
    )
}

private class CwtRowConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val paths: Set<String>,
    override val pathFile: String?,
    override val pathExtension: String?,
    override val pathStrict: Boolean,
    override val pathPatterns: Set<String>,
    override val columnConfigs: Map<String, CwtMemberConfig<*>>
) : UserDataHolderBase(), CwtRowConfig {
    override fun toString(): String {
        return "CwtRowConfigImpl(name='$name')"
    }
}
