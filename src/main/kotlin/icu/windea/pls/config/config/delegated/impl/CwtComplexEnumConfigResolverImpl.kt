package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.processDescendants
import icu.windea.pls.config.util.CwtConfigResolverUtil.withLocationPrefix
import icu.windea.pls.core.collections.getAll
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

class CwtComplexEnumConfigResolverImpl : CwtComplexEnumConfig.Resolver {
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
        val paths = propGroup.getAll("path").mapNotNullTo(sortedSetOf()) { it.stringValue?.removePrefix("game/")?.normalizePath()?.intern() }.optimized()
        val pathFile = propGroup.getOne("path_file")?.stringValue
        val pathExtension = propGroup.getOne("path_extension")?.stringValue?.removePrefix(".")?.intern()
        val pathStrict = propGroup.getOne("path_strict")?.booleanValue ?: false
        val pathPatterns = propGroup.getAll("path_pattern").mapNotNullTo(sortedSetOf()) { it.stringValue?.removePrefix("game/")?.normalizePath()?.intern() }.optimized()
        val startFromRoot = propGroup.getOne("start_from_root")?.booleanValue ?: false
        val nameConfig = propGroup.getOne("name")
        val searchScopeType = config.optionData { searchScopeType }

        if (nameConfig == null) {
            logger.warn("Skipped invalid complex enum config (name: $name): Missing name config.".withLocationPrefix(config))
            return null
        }
        logger.debug { "Resolved complex enum config (name: $name).".withLocationPrefix(config) }
        return CwtComplexEnumConfigImpl(
            config, name,
            paths, pathFile, pathExtension, pathStrict, pathPatterns,
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
