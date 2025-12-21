package icu.windea.pls.config.util

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.jetbrains.rd.util.ThreadLocal
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.match.PathMatcher
import icu.windea.pls.core.optimized
import icu.windea.pls.core.pass
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtBlockElement
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.cwt.psi.CwtOptionComment
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.model.CwtMemberType

object CwtConfigResolverManager {
    object Keys : KeyRegistry() {
        val fileConfigs by createKey<MutableMap<String, CwtFileConfig>>(Keys)
        val postProcessActions by createKey<MutableList<Runnable>>(Keys)
    }

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

    @Optimized
    fun getConfigs(element: PsiElement?, file: CwtFile, configGroup: CwtConfigGroup): List<CwtMemberConfig<*>>? {
        if (element !is CwtBlockElement) return null
        val configs: MutableList<CwtMemberConfig<*>> = FastList()
        element.forEachChild f@{ e ->
            when (e) {
                is CwtProperty -> {
                    val resolved = CwtPropertyConfig.resolve(e, file, configGroup) ?: return@f
                    CwtPropertyConfig.postProcess(resolved)
                    configs += resolved
                }
                is CwtValue -> {
                    val resolved = CwtValueConfig.resolve(e, file, configGroup)
                    CwtValueConfig.postProcess(resolved)
                    configs += resolved
                }
            }
        }
        return configs // delay optimization
    }

    @Optimized
    fun getOptionConfigs(element: CwtMember): List<CwtOptionMemberConfig<*>> {
        val optionConfigs: MutableList<CwtOptionMemberConfig<*>> = FastList()
        var current: PsiElement = element
        while (true) {
            current = current.prevSibling ?: break
            when (current) {
                is CwtOptionComment -> {
                    current.forEachChild f@{ e ->
                        when (e) {
                            is CwtOption -> {
                                val resolved = CwtOptionConfig.resolve(e) ?: return@f
                                optionConfigs.add(0, resolved)
                            }
                            is CwtValue -> {
                                val resolved = CwtOptionValueConfig.resolve(e)
                                optionConfigs.add(0, resolved)
                            }
                        }
                    }
                }
                is PsiWhiteSpace, is PsiComment -> continue
                else -> break
            }
        }
        return optionConfigs // delay optimization
    }

    @Optimized
    fun getOptionConfigsInOption(element: CwtValue): List<CwtOptionMemberConfig<*>>? {
        if (element !is CwtBlock) return null
        val optionConfigs: MutableList<CwtOptionMemberConfig<*>> = FastList()
        element.forEachChild f@{ e ->
            when (e) {
                is CwtOption -> {
                    val resolved = CwtOptionConfig.resolve(e) ?: return@f
                    optionConfigs += resolved
                }
                is CwtValue -> {
                    val resolved = CwtOptionValueConfig.resolve(e)
                    optionConfigs += resolved
                }
            }
        }
        return optionConfigs.optimized() // optimized to optimize memory
    }

    @Optimized
    fun checkMemberType(configs: List<CwtMemberConfig<*>>?, noConfigs: Boolean = configs.isNullOrEmpty()): CwtMemberType {
        if (noConfigs) return CwtMemberType.NONE
        var result = CwtMemberType.NONE
        configs?.forEachFast { c ->
            val r = when (c) {
                is CwtPropertyConfig -> CwtMemberType.PROPERTY
                is CwtValueConfig -> CwtMemberType.VALUE
            }
            when {
                result == CwtMemberType.NONE -> result = r
                result != r -> return CwtMemberType.MIXED
            }
        }
        return result
    }

    fun collectFromConfigExpression(config: CwtConfig<*>, configExpression: CwtDataExpression) {
        val configGroup = config.configGroup
        val initializer = configGroup.initializer
        when (configExpression.type) {
            CwtDataTypes.FilePath -> {
                if (configExpression.value != null) {
                    initializer.filePathExpressions += configExpression
                }
            }
            CwtDataTypes.Icon -> {
                if (configExpression.value != null) {
                    initializer.filePathExpressions += configExpression
                }
            }
            CwtDataTypes.Parameter -> {
                if (config is CwtPropertyConfig) {
                    initializer.parameterConfigs += config
                }
            }
            else -> pass()
        }
    }

    fun getFileConfigs(configGroup: CwtConfigGroup): MutableMap<String, CwtFileConfig> {
        return configGroup.initializer.getOrPutUserData(Keys.fileConfigs) { mutableMapOf() }
    }

    fun getPostProcessActions(configGroup: CwtConfigGroup): MutableList<Runnable> {
        return configGroup.initializer.getOrPutUserData(Keys.postProcessActions) { mutableListOf() }
    }

    fun findFileConfigByPathExpression(configGroup: CwtConfigGroup, pathExpression: String): CwtFileConfig? {
        if (pathExpression.isEmpty()) return null
        val fileConfigs = getFileConfigs(configGroup)
        val fileConfig = fileConfigs[pathExpression]
        return fileConfig
    }

    fun findConfigsByPathExpression(fileConfig: CwtFileConfig, pathExpression: String): List<CwtMemberConfig<*>> {
        if (pathExpression.isEmpty()) return emptyList()
        val pathList = pathExpression.split('/')
        var r: List<CwtMemberConfig<*>> = emptyList()
        pathList.forEach { p ->
            if (p == "-") {
                if (r.isEmpty()) {
                    r = fileConfig.values
                } else {
                    r = r.flatMap { it.values.orEmpty() }
                }
            } else {
                if (r.isEmpty()) {
                    r = fileConfig.properties
                        .filter { PathMatcher.matches(it.key, p, ignoreCase = true, useAny = true, usePattern = true) }
                } else {
                    r = r.flatMap { it.properties.orEmpty() }
                        .filter { PathMatcher.matches(it.key, p, ignoreCase = true, useAny = true, usePattern = true) }
                }
            }
            if (r.isEmpty()) return emptyList()
        }
        return r
    }

    fun findConfigsByPathExpression(configGroup: CwtConfigGroup, pathExpression: String): List<CwtMemberConfig<*>>? {
        val pathList = pathExpression.split('@', limit = 2)
        if (pathList.size != 2) return null
        val fileConfig = findFileConfigByPathExpression(configGroup, pathList[0]) ?: return null
        val configs = findConfigsByPathExpression(fileConfig, pathList[1])
        return configs
    }
}
