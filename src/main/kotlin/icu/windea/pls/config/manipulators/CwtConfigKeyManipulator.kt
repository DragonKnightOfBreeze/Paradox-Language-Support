package icu.windea.pls.config.manipulators

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.singleAliasConfig
import icu.windea.pls.config.isSamePointer
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.lang.util.ParadoxInlineScriptManager

@Suppress("unused")
object CwtConfigKeyManipulator {
    object Keys : KeyRegistry() {
        val inBlockKeys by registerKey<Set<String>>(this)
    }

    @Optimized
    fun getIdentifierKey(config: CwtMemberConfig<*>, delimiter: String, maxDepth: Int = -1): String {
        return doGetIdentifierKey(config, delimiter, maxDepth, 0)
    }

    @Optimized
    fun getIdentifierKey(configs: List<CwtMemberConfig<*>>, delimiter: String, maxDepth: Int = -1): String {
        return doGetIdentifierKey(configs, delimiter, maxDepth, 0)
    }

    private fun doGetIdentifierKey(config: CwtMemberConfig<*>, delimiter: String, maxDepth: Int, depth: Int): String {
        if (maxDepth >= 0 && maxDepth < depth) return ""
        return buildString {
            if (config is CwtPropertyConfig) {
                append(config.key)
                append(config.separatorType.text)
            }
            val children = config.configs
            when {
                children == null -> append(config.value)
                children.isEmpty() -> append("{}")
                else -> append('{').append(doGetIdentifierKey(children, delimiter, maxDepth, depth + 1)).append('}')
            }
        }
    }

    private fun doGetIdentifierKey(configs: List<CwtMemberConfig<*>>, delimiter: String, maxDepth: Int, depth: Int): String {
        val size = configs.size
        return when (size) {
            0 -> ""
            1 -> doGetIdentifierKey(configs.get(0), delimiter, maxDepth, depth)
            else -> configs.mapFast { doGetIdentifierKey(it, delimiter, maxDepth, depth) }.sorted().joinToString(delimiter)
        }
    }

    @Optimized
    fun getIdentifierKey(optionConfig: CwtOptionMemberConfig<*>, delimiter: String): String {
        return doGetIdentifierKey(optionConfig, delimiter)
    }

    @Optimized
    fun getIdentifierKey(optionConfigs: List<CwtOptionMemberConfig<*>>, delimiter: String): String {
        return doGetIdentifierKey(optionConfigs, delimiter)
    }

    private fun doGetIdentifierKey(config: CwtOptionMemberConfig<*>, delimiter: String): String {
        return buildString {
            if (config is CwtOptionConfig) {
                append(config.key)
                append(config.separatorType.text)
            }
            val children = config.optionConfigs
            when {
                children == null -> append(config.value)
                children.isEmpty() -> append("{}")
                else -> append('{').append(doGetIdentifierKey(children, delimiter)).append('}')
            }
        }
    }

    private fun doGetIdentifierKey(optionConfigs: List<CwtOptionMemberConfig<*>>, delimiter: String): String {
        val size = optionConfigs.size
        return when (size) {
            0 -> ""
            1 -> doGetIdentifierKey(optionConfigs.get(0), delimiter)
            else -> optionConfigs.mapFast { doGetIdentifierKey(it, delimiter) }.sorted().joinToString(delimiter)
        }
    }

    @Optimized
    fun getDistinctKey(config: CwtMemberConfig<*>): String {
        return doGetDistinctKey(config)
    }

    private fun doGetDistinctKey(config: CwtMemberConfig<*>, guardStack: MutableSet<String>? = null): String {
        run {
            // 处理规则需要内联的情况，并且尝试避免SOF
            if (config !is CwtPropertyConfig) return@run
            val inlinedConfig = CwtConfigManipulator.inlineSingleAlias(config) ?: return@run
            val guardKey = inlinedConfig.singleAliasConfig?.let { "sa:${it.name}" } ?: return@run
            val newGuardStack = guardStack ?: mutableSetOf()
            if (!newGuardStack.add(guardKey)) return "..."
            return doGetDistinctKey(inlinedConfig, newGuardStack)
        }
        return buildString {
            if (config is CwtPropertyConfig) append(config.key).append('=')
            val children = config.configs
            when {
                children == null -> append(config.value)
                children.isEmpty() -> append("{}")
                else -> append('{').append(children.mapFast { doGetDistinctKey(it, guardStack) }.sorted().joinToString("\u0000")).append('}')
            }
        }
    }

    @Optimized
    fun getInBlockKeys(config: CwtMemberConfig<*>): Set<String> {
        return config.getOrPutUserData(Keys.inBlockKeys) { doGetInBlockKeys(config).optimized() }
    }

    private fun doGetInBlockKeys(config: CwtMemberConfig<*>): Set<@CaseInsensitive String> {
        val childConfigs = config.configs
        if (childConfigs.isNullOrEmpty()) return emptySet()
        val keys = caseInsensitiveStringSet()
        childConfigs.forEachFast { if (it is CwtPropertyConfig && isInBlockKey(it)) keys.add(it.key) }
        if (keys.isEmpty()) return emptySet()
        when (config) {
            is CwtPropertyConfig -> {
                val propertyConfig = config
                val configs1 = propertyConfig.parentConfig?.configs
                if (configs1.isNullOrEmpty()) return keys
                configs1.forEachFast f@{ c ->
                    val childConfigs1 = c.configs
                    if (childConfigs1.isNullOrEmpty()) return@f
                    if (c.isSamePointer(propertyConfig) || c !is CwtPropertyConfig || !c.key.equals(propertyConfig.key, true)) return@f
                    childConfigs1.forEachFast { if (it is CwtPropertyConfig && isInBlockKey(it)) keys.remove(it.key) }
                }
            }
            is CwtValueConfig -> {
                val propertyConfig = config.propertyConfig
                val configs1 = propertyConfig?.parentConfig?.configs
                if (configs1.isNullOrEmpty()) return keys
                configs1.forEachFast f@{ c ->
                    val childConfigs1 = c.configs
                    if (childConfigs1.isNullOrEmpty()) return@f
                    if (c.isSamePointer(propertyConfig) || c !is CwtPropertyConfig || !c.key.equals(propertyConfig.key, true)) return@f
                    childConfigs1.forEachFast { if (it is CwtPropertyConfig && isInBlockKey(it)) keys.remove(it.key) }
                }
            }
        }
        return keys
    }

    private fun isInBlockKey(config: CwtPropertyConfig): Boolean {
        val gameType = config.configGroup.gameType
        if (config.keyExpression.type != CwtDataTypes.Constant) return false
        if (config.optionData.cardinality?.isRequired() == false) return false
        if (ParadoxInlineScriptManager.isMatched(config.key, gameType)) return false // 排除是内联脚本用法的情况
        return true
    }
}
