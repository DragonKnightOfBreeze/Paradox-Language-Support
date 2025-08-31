package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtComplexEnumConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.findOption
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.processDescendants
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import kotlin.collections.orEmpty

class CwtComplexEnumConfigResolverImpl:CwtComplexEnumConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtComplexEnumConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtComplexEnumConfigImpl? {
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

    override fun toString() = "CwtComplexEnumConfigImpl(name='$name')"
}
