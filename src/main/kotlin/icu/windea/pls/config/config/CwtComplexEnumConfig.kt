package icu.windea.pls.config.config

import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*

/**
 * @property name string
 * @property path (property*) path: string 相对于游戏或模组根路径的路径。
 * @property pathFile (property) path_file: string 路径下的文件名。
 * @property pathStrict (property) path_strict: boolean
 * @property startFromRoot (property) start_from_root: boolean
 * @property searchScopeType (property) search_scope_type: string 查询作用域，认为仅该作用域下的复杂枚举值是等同的。（目前支持：definition）
 * @property nameConfig `name`对应的CWT规则。
 * @property enumNameConfigs [nameConfig]中作为锚点的`enum_name`对应的CWT规则。
 */
interface CwtComplexEnumConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val path: Set<String>
    val pathFile: String?
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
    val key = config.key
    val name = key.removeSurroundingOrNull("complex_enum[", "]")?.orNull()?.intern() ?: return null
    val props = config.properties?.orNull() ?: return null
    val path: MutableSet<String> = mutableSetOf()
    var pathFile: String? = null
    var pathStrict = false
    var startFromRoot = false
    var nameConfig: CwtPropertyConfig? = null
    for(prop in props) {
        when(prop.key) {
            //这里的path一般"game/"开始，这里需要忽略
            "path" -> prop.stringValue?.let { it.removePrefix("game/").normalizePath() }?.let { path += it }
            "path_file" -> pathFile = prop.stringValue?.intern()
            "path_strict" -> pathStrict = prop.booleanValue ?: false
            "start_from_root" -> startFromRoot = prop.booleanValue ?: false
            "name" -> nameConfig = prop
        }
    }
    val searchScopeType = config.findOption("search_scope_type")?.stringValue
    if(path.isEmpty() || nameConfig == null) return null //invalid
    return CwtComplexEnumConfigImpl(config, name, path, pathFile, pathStrict, startFromRoot, searchScopeType, nameConfig)
}

private class CwtComplexEnumConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val path: Set<String>,
    override val pathFile: String?,
    override val pathStrict: Boolean,
    override val startFromRoot: Boolean,
    override val searchScopeType: String?,
    override val nameConfig: CwtPropertyConfig
) : CwtComplexEnumConfig {
    override val enumNameConfigs: List<CwtMemberConfig<*>> by lazy {
        buildList {
            nameConfig.processDescendants {
                when {
                    it is CwtPropertyConfig -> {
                        if(it.key == "enum_name" || it.stringValue == "enum_name") add(it)
                    }
                    it is CwtValueConfig -> {
                        if(it.stringValue == "enum_name") add(it)
                    }
                }
                true
            }
        }
    }
}
