package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*

/**
 * @property name string
 * @property pathPatterns (property*) path_pattern: string
 * @property paths (property) path: string
 * @property pathFile (property) path_file: string
 * @property pathExtension (property) path_extension: string
 * @property pathStrict (property) path_strict: boolean
 * @property startFromRoot (property) start_from_root: boolean
 * @property searchScopeType (property) search_scope_type: string 查询作用域，认为仅该作用域下的复杂枚举值是等同的。（目前支持：definition）
 * @property nameConfig `name`对应的CWT规则。
 * @property enumNameConfigs [nameConfig]中作为锚点的`enum_name`对应的CWT规则。
 */
interface CwtComplexEnumConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val pathPatterns: Set<String>
    val paths: Set<String>
    val pathFile: String?
    val pathExtension: String?
    val pathStrict: Boolean
    val startFromRoot: Boolean
    val searchScopeType: String?
    val nameConfig: CwtPropertyConfig
    val enumNameConfigs: List<CwtMemberConfig<*>>

    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtComplexEnumConfig? = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtComplexEnumConfig? {
    val name = config.key.removeSurroundingOrNull("complex_enum[", "]")?.orNull()?.intern() ?: return null
    val pathPatterns = sortedSetOf<String>()
    val paths = sortedSetOf<String>()
    var pathFile: String? = null
    var pathExtension: String? = null
    var pathStrict = false
    var startFromRoot = false
    var nameConfig: CwtPropertyConfig? = null

    val props = config.properties.orEmpty()
    if (props.isEmpty()) return null
    for (prop in props) {
        when (prop.key) {
            "path_pattern" -> prop.stringValue?.removePrefix("game/")?.normalizePath()?.let { pathPatterns += it }
            "path" -> prop.stringValue?.removePrefix("game/")?.normalizePath()?.let { paths += it }
            "path_file" -> pathFile = prop.stringValue ?: continue
            "path_extension" -> pathExtension = prop.stringValue?.removePrefix(".") ?: continue
            "path_strict" -> pathStrict = prop.booleanValue ?: continue
            "start_from_root" -> startFromRoot = prop.booleanValue ?: false
            "name" -> nameConfig = prop
        }
    }

    val searchScopeType = config.findOption("search_scope_type")?.stringValue

    if (nameConfig == null) return null
    return CwtComplexEnumConfigImpl(
        config, name, pathPatterns.optimized(), paths.optimized(), pathFile, pathExtension, pathStrict,
        startFromRoot, searchScopeType, nameConfig
    )
}

private class CwtComplexEnumConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val pathPatterns: Set<String>,
    override val paths: Set<String>,
    override val pathFile: String?,
    override val pathExtension: String?,
    override val pathStrict: Boolean,
    override val startFromRoot: Boolean,
    override val searchScopeType: String?,
    override val nameConfig: CwtPropertyConfig,
) : UserDataHolderBase(), CwtComplexEnumConfig {
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
