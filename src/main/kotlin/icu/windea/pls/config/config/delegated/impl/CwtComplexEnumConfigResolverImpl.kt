package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.processDescendants
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.optimizedPath
import icu.windea.pls.config.optimizedPathExtension
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.collections.getAll
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

class CwtComplexEnumConfigResolverImpl : CwtComplexEnumConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtComplexEnumConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtComplexEnumConfig? {
        val name = config.key.removeSurroundingOrNull("complex_enum[", "]")?.orNull() ?: return null
        val propElements = config.properties
        if (propElements.isNullOrEmpty()) {
            logger.warn("Skipped invalid complex enum config (name: $name): Missing properties.".withLocationPrefix(config))
            return null
        }

        val propGroup = propElements.groupBy { it.key }
        val paths = propGroup.getAll("path").mapNotNullTo(sortedSetOf()) { it.stringValue?.optimizedPath() }.optimized()
        val pathFile = propGroup.getOne("path_file")?.stringValue
        val pathExtension = propGroup.getOne("path_extension")?.stringValue?.optimizedPathExtension()
        val pathStrict = propGroup.getOne("path_strict")?.booleanValue ?: false
        val pathPatterns = propGroup.getAll("path_pattern").mapNotNullTo(sortedSetOf()) { it.stringValue?.optimizedPath() }.optimized()
        val startFromRoot = propGroup.getOne("start_from_root")?.booleanValue ?: false
        val perDefinition = propGroup.getOne("per_definition")?.booleanValue ?: false
        val nameConfig = propGroup.getOne("name")

        if (nameConfig == null) {
            logger.warn("Skipped invalid complex enum config (name: $name): Missing name config.".withLocationPrefix(config))
            return null
        }
        logger.debug { "Resolved complex enum config (name: $name).".withLocationPrefix(config) }
        return CwtComplexEnumConfigImpl(
            config, name,
            paths, pathFile, pathExtension, pathStrict, pathPatterns,
            startFromRoot, perDefinition, nameConfig
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
    override val perDefinition: Boolean,
    override val nameConfig: CwtPropertyConfig,
) : UserDataHolderBase(), CwtComplexEnumConfig {
    override val searchScopeType: String? = if (perDefinition) "definition" else null
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
