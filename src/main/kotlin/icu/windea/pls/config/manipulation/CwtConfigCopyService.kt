package icu.windea.pls.config.manipulation

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfigService
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.lang.resolve.CwtDeclarationConfigContext
import icu.windea.pls.model.expressions.ParadoxDefinitionSubtypeExpression
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object CwtConfigCopyService {
    @Optimized
    fun createListForDeepCopy(): MutableList<CwtMemberConfig<*>> {
        return FastList()
    }

    @Optimized
    @OptIn(ExperimentalContracts::class)
    fun createListForDeepCopy(configs: List<CwtMemberConfig<*>>?): MutableList<CwtMemberConfig<*>>? {
        contract {
            returnsNotNull() implies (configs != null)
        }
        if (configs == null) return null
        return FastList()
    }

    @Optimized
    fun deepCopyConfigs(containerConfig: CwtMemberConfig<*>, parentConfig: CwtMemberConfig<*> = containerConfig): List<CwtMemberConfig<*>>? {
        return doDeepCopyConfigs(containerConfig, parentConfig)
    }

    @Optimized
    fun deepCopyConfigsInDeclaration(containerConfig: CwtMemberConfig<*>, parentConfig: CwtMemberConfig<*> = containerConfig, context: CwtDeclarationConfigContext): List<CwtMemberConfig<*>>? {
        return doDeepCopyConfigsInDeclaration(containerConfig, context, parentConfig)
    }

    private fun doDeepCopyConfigs(containerConfig: CwtMemberConfig<*>, parentConfig: CwtMemberConfig<*>): List<CwtMemberConfig<*>>? {
        val configs = containerConfig.configs?.optimized() ?: return null // 这里需要兼容并同样处理子规则列表为空的情况
        if (configs.isEmpty()) return configs
        val result = createListForDeepCopy()
        configs.forEachFast { config ->
            val childConfigs = createListForDeepCopy(config.configs)
            val delegatedConfig = config.delegated(childConfigs).also { it.parentConfig = parentConfig }
            if (childConfigs != null) childConfigs += doDeepCopyConfigs(config, delegatedConfig).orEmpty()
            delegatedConfig.postOptimize() // 进行后续优化
            result += delegatedConfig
        }
        result.forEachFast { it.parentConfig = parentConfig } // 确保绑定了父规则
        injectConfigsForDeepCopy(parentConfig, result) ?: return emptyList() // 尝试注入规则，如果失败则返回空列表（即使输入的结果为空也要尝试）
        return result // 这里需要直接返回可变列表
    }

    private fun doDeepCopyConfigsInDeclaration(containerConfig: CwtMemberConfig<*>, context: CwtDeclarationConfigContext, parentConfig: CwtMemberConfig<*>): List<CwtMemberConfig<*>>? {
        val configs = containerConfig.configs?.optimized() ?: return null // 这里需要兼容并同样处理子规则列表为空的情况
        if (configs.isEmpty()) return configs
        val result = createListForDeepCopy()
        configs.forEachFast f@{ config ->
            run r@{
                // 如果匹配子类型表达式，打平其中的子规则并加入结果，否则直接跳过
                val subtypes = context.definitionSubtypes ?: return@r
                val subtypeExpression = getSubtypeExpression(config) ?: return@r
                if (config.configs.isNullOrEmpty()) return@f // skip
                val matched = ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression).matches(subtypes)
                if (!matched) return@f // skip
                result += deepCopyConfigsInDeclaration(config, parentConfig, context).orEmpty()
                return@f
            }

            val childConfigs = createListForDeepCopy(config.configs)
            val delegatedConfig = config.delegated(childConfigs).also { it.parentConfig = parentConfig }
            if (childConfigs != null) childConfigs += deepCopyConfigsInDeclaration(config, delegatedConfig, context).orEmpty()
            delegatedConfig.postOptimize() // 进行后续优化
            result += delegatedConfig
        }
        result.forEachFast { it.parentConfig = parentConfig } // 确保绑定了父规则
        injectConfigsForDeepCopy(parentConfig, result) ?: return emptyList() // 尝试注入规则，如果失败则返回空列表（即使输入的结果为空也要尝试）
        return result // 这里需要直接返回可变列表
    }

    private fun injectConfigsForDeepCopy(parentConfig: CwtMemberConfig<*>, result: MutableList<CwtMemberConfig<*>>): Boolean? {
        // NOTE 2.1.1 对于目前的深拷贝规则的逻辑，仅需在注入规则时使用递归守卫（根据分析结果，无需使用命名递归守卫）
        return withRecursionGuard {
            val key = getKeyForDeepCopy(parentConfig)
            withRecursionCheck(key) {
                CwtConfigService.injectConfigs(parentConfig, result)
            }
        }
    }

    private fun getKeyForDeepCopy(parentConfig: CwtMemberConfig<*>): Any? {
        // NOTE 2.1.1 这里可以直接使用指针作为键，应当不会存在内存泄露或其他问题
        // NOTE 2.1.1 为了优化性能，这里可以直接检查是否引用相等
        return parentConfig.pointer.takeIf { it !== emptyPointer<PsiElement>() }
    }

    fun getSubtypeExpression(config: CwtMemberConfig<*>): String? {
        if (config !is CwtPropertyConfig) return null
        return config.key.removeSurroundingOrNull("subtype[", "]")
    }

    @Optimized
    fun flattenBySubtypeExpression(rootConfig: CwtMemberConfig<*>, action: (config: CwtMemberConfig<*>, expression: String) -> Unit) {
        flattenBySubtypeExpressionRecursively(rootConfig, "", action)
    }

    private fun flattenBySubtypeExpressionRecursively(rootConfig: CwtMemberConfig<*>, currentExpression: String, action: (CwtMemberConfig<*>, String) -> Unit) {
        rootConfig.configs?.forEachFast f@{ config ->
            val nextExpression = getSubtypeExpression(config)
            if (nextExpression != null) {
                if (config.configs.isNullOrEmpty()) return@f // skip
                val mergedExpression = mergeSubtypeExpression(currentExpression, nextExpression)
                flattenBySubtypeExpressionRecursively(config, mergedExpression, action)
            } else {
                action(config, currentExpression)
            }
        }
    }

    private fun mergeSubtypeExpression(expression1: String, expression2: String): String {
        return when {
            expression1.isEmpty() -> expression2
            expression2.isEmpty() -> expression1
            expression1 == expression2 -> expression1
            else -> "$expression1&$expression2"
        }
    }
}
