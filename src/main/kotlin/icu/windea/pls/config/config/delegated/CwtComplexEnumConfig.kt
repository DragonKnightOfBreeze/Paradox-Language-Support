@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.util.*
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
 * @property startFromRoot (property) start_from_root: boolean
 * @property searchScopeType (property) search_scope_type: string 查询作用域，认为仅该作用域下的复杂枚举值是等同的。（目前支持：definition）
 * @property nameConfig `name`对应的CWT规则。
 * @property enumNameConfigs [nameConfig]中作为锚点的`enum_name`对应的CWT规则。
 */
interface CwtComplexEnumConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtFilePathMatchableConfig {
    val name: String
    override val paths: Set<String>
    override val pathFile: String?
    override val pathExtension: String?
    override val pathStrict: Boolean
    override val pathPatterns: Set<String>
    val startFromRoot: Boolean
    val searchScopeType: String?
    val nameConfig: CwtPropertyConfig
    val enumNameConfigs: List<CwtMemberConfig<*>>

    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtComplexEnumConfig? = doResolve(config)
    }
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtComplexEnumConfig? {
    val name = config.key.removeSurroundingOrNull("complex_enum[", "]")?.orNull() ?: return null
    val paths = sortedSetOf<String>()
    var pathFile: String? = null
    var pathExtension: String? = null
    var pathStrict = false
    val pathPatterns = sortedSetOf<String>()
    var startFromRoot = false
    var nameConfig: CwtPropertyConfig? = null

    val props = config.properties.orEmpty()
    if (props.isEmpty()) return null
    for (prop in props) {
        when (prop.key) {
            "path" -> prop.stringValue?.removePrefix("game/")?.normalizePath()?.let { paths += it.intern() }
            "path_file" -> pathFile = prop.stringValue ?: continue
            "path_extension" -> pathExtension = prop.stringValue?.removePrefix(".")?.intern() ?: continue
            "path_strict" -> pathStrict = prop.booleanValue ?: continue
            "path_pattern" -> prop.stringValue?.removePrefix("game/")?.normalizePath()?.let { pathPatterns += it.intern() }
            "start_from_root" -> startFromRoot = prop.booleanValue ?: false
            "name" -> nameConfig = prop
        }
    }

    val searchScopeType = config.findOption("search_scope_type")?.stringValue

    if (nameConfig == null) return null
    return CwtComplexEnumConfigImpl(
        config, name,
        paths.optimized(), pathFile, pathExtension, pathStrict, pathPatterns.optimized(),
        startFromRoot, searchScopeType, nameConfig
    )
}

private class CwtComplexEnumConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val paths: Set<String>,
    override val pathFile: String?,
    override val pathExtension: String?,
    override val pathStrict: Boolean,
    override val pathPatterns: Set<String>,
    override val startFromRoot: Boolean,
    override val searchScopeType: String?,
    override val nameConfig: CwtPropertyConfig,
) : UserDataHolderBase(), CwtComplexEnumConfig {
    override val filePathPatterns: Set<String> by lazy {
        CwtConfigManager.getFilePathPatterns(this).optimized()
    }
    override val filePathPatternsForPriority: Set<String> by lazy {
        CwtConfigManager.getFilePathPatternsForPriority(this).optimized()
    }

    override val enumNameConfigs: List<CwtMemberConfig<*>> by lazy {
        buildList {
            nameConfig.processDescendants {
                when {
                    it is CwtPropertyConfig -> {
                        if (it.key == "enum_name" || it.stringValue == "enum_name") add(it)
                    }
                    it is CwtValueConfig -> {
                        if (it.stringValue == "enum_name") add(it)
                    }
                }
                true
            }
        }
    }
}
