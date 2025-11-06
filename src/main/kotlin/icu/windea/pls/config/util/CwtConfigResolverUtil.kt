package icu.windea.pls.config.util

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.jetbrains.rd.util.ThreadLocal
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.CwtTagType
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.filePathExpressions
import icu.windea.pls.config.configGroup.parameterConfigs
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.optimized
import icu.windea.pls.core.pass
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtBlockElement
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.cwt.psi.CwtOptionComment
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue

object CwtConfigResolverUtil {
    private val currentLocation = ThreadLocal<String>()

    fun setLocation(filePath: String, configGroup: CwtConfigGroup) {
        val gameTypeId = configGroup.gameType.id
        val location = "$gameTypeId@$filePath"
        currentLocation.set(location)
    }

    fun resetLocation() {
        currentLocation.remove()
    }

    fun getLocation(): String? {
        return currentLocation.get()
    }

    fun getConfigs(element: PsiElement, file: CwtFile, configGroup: CwtConfigGroup): List<CwtMemberConfig<*>>? {
        if (element !is CwtBlockElement) return null
        val configs = mutableListOf<CwtMemberConfig<*>>()
        element.forEachChild { e ->
            when (e) {
                is CwtProperty -> CwtPropertyConfig.resolve(e, file, configGroup)?.let { configs += it }
                is CwtValue -> CwtValueConfig.resolve(e, file, configGroup).let { configs += it }
            }
        }
        return configs.optimized() // optimized to optimize memory
    }

    fun getOptionConfigs(element: CwtMember): List<CwtOptionMemberConfig<*>>? {
        var optionConfigs: MutableList<CwtOptionMemberConfig<*>>? = null
        var current: PsiElement = element
        while (true) {
            current = current.prevSibling ?: break
            when (current) {
                is CwtOptionComment -> {
                    val option = current.option
                    if (option != null) {
                        if (optionConfigs == null) optionConfigs = mutableListOf()
                        val resolved = CwtOptionConfig.resolve(option) ?: continue
                        optionConfigs.add(0, resolved)
                    } else {
                        val optionValue = current.optionValue ?: continue
                        if (optionConfigs == null) optionConfigs = mutableListOf()
                        val resolved = CwtOptionValueConfig.resolve(optionValue)
                        optionConfigs.add(0, resolved)
                    }
                }
                is PsiWhiteSpace, is PsiComment -> continue
                else -> break
            }
        }
        return optionConfigs?.optimized() // optimized to optimize memory
    }

    fun getOptionConfigsInOption(element: CwtValue): List<CwtOptionMemberConfig<*>>? {
        if (element !is CwtBlock) return null
        val optionConfigs = mutableListOf<CwtOptionMemberConfig<*>>()
        element.forEachChild { e ->
            when (e) {
                is CwtOption -> CwtOptionConfig.resolve(e)?.let { optionConfigs += it }
                is CwtValue -> CwtOptionValueConfig.resolve(e).let { optionConfigs += it }
            }
        }
        return optionConfigs.optimized() // optimized to optimize memory
    }

    fun isUniform(configs: List<CwtMemberConfig<*>>): Boolean {
        if (configs.isEmpty()) return false
        var flag1 = false
        var flag2 = false
        configs.forEachFast { c ->
            when (c) {
                is CwtPropertyConfig -> flag1 = true
                is CwtValueConfig -> flag2 = true
            }
        }
        return flag1 == flag2
    }

    fun applyOptions(config: CwtMemberConfig<*>) {
        applyInheritOptions(config)
        applyTagOption(config)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun applyInheritOptions(config: CwtMemberConfig<*>) {
        // TODO 1.3.18+
        // val configGroup = config.configGroup
        //
        // var inheritConfigsValue: String? = null
        // var inheritOptionsValue: String? = null
        // var inheritDocValue: String? = null
        //
        // val oldOptions = mutableListOf<CwtOptionMemberConfig<*>>()
        // config.options?.forEach { o ->
        //    when(o){
        //        is CwtOptionConfig -> when(o.key) {
        //            "inherit_configs" -> o.stringValue?.let { inheritConfigsValue = it }
        //            "inherit_options" -> o.stringValue?.let { inheritOptionsValue = it }
        //            "inherit_doc" -> o.stringValue?.let { inheritDocValue = it }
        //            else -> oldOptions += o
        //        }
        //        is CwtOptionValueConfig -> oldOptions += o
        //    }
        // }
        //
        // if(inheritConfigsValue == null && inheritOptionsValue == null && inheritDocValue == null) return config
        //
        // var newConfigs: List<CwtMemberConfig<*>>? = null
        // var newOptions: List<CwtOptionMemberConfig<*>>? = null
        // var newDocumentation: String? = null
        //
        // inheritDocValue?.let { pathExpression ->
        //    CwtConfigManager.getConfigByPathExpression(configGroup, pathExpression)?.let { newConfig ->
        //        newDocumentation = newConfig.documentation
        //    }
        // }
        //
        // if(newConfigs == null && newOptions == null && newDocumentation == null) return config
    }

    private fun applyTagOption(config: CwtMemberConfig<*>) {
        // #123 mark tag type as predefined for config
        if (config is CwtValueConfig && config.optionData { flags }.tag) {
            config.tagType = CwtTagType.Predefined
        }
    }

    fun collectFromConfigExpression(config: CwtConfig<*>, configExpression: CwtDataExpression) {
        val configGroup = config.configGroup
        when (configExpression.type) {
            CwtDataTypes.FilePath -> {
                if (configExpression.value != null) {
                    configGroup.filePathExpressions += configExpression
                }
            }
            CwtDataTypes.Icon -> {
                if (configExpression.value != null) {
                    configGroup.filePathExpressions += configExpression
                }
            }
            CwtDataTypes.Parameter -> {
                if (config is CwtPropertyConfig) {
                    configGroup.parameterConfigs += config
                }
            }
            else -> pass()
        }
    }
}
