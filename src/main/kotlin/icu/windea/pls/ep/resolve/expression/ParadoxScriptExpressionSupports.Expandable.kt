package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.manipulation.CwtConfigExpansionService
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.util.ProcessorFactory
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.manipulation.ParadoxConfigExpansionService
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.model.type.ParadoxExpressionRole

abstract class ParadoxExpandableScriptExpressionSupport : ParadoxScriptExpressionSupport {
    /**
     * @see CwtDataTypes.UnionValue
     */
    class ForUnionValue : ParadoxExpandableScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.UnionValue

        // NOTE 3.0.1 recursion guard is required here for various operations
        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return false
            val unionName = configExpression.metadata.value ?: return false
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first actually matched config atm, event if the result from this config is null or empty
            val processor = ProcessorFactory.find<CwtValueConfig>()
            runWithRecursionGuard("scriptExpression.annotate.union", unionName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedUnion(element, expression, unionName, configGroup) {
                    processor.process(it)
                }
            }
            val unionValueConfig = processor.result ?: return false
            return ParadoxExpressionService.annotateScriptExpression(element, text, rangeInExpression, unionValueConfig, holder)
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return null
            val unionName = configExpression.metadata.value ?: return null
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first actually matched config atm, event if the result from this config is null or empty
            val processor = ProcessorFactory.find<CwtValueConfig>()
            runWithRecursionGuard("scriptExpression.resolve.union", unionName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedUnion(element, expression, unionName, configGroup) {
                    processor.process(it)
                }
            }
            val unionValueConfig = processor.result ?: return null
            return ParadoxExpressionService.resolveScriptExpression(element, text, rangeInExpression, unionValueConfig, role)
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return emptyList()
            val unionName = configExpression.metadata.value ?: return emptyList()
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first actually matched config atm, event if the result from this config is null or empty
            val processor = ProcessorFactory.find<CwtValueConfig>()
            runWithRecursionGuard("scriptExpression.resolveAll.union", unionName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedUnion(element, expression, unionName, configGroup) {
                    processor.process(it)
                }
            }
            val unionValueConfig = processor.result ?: return emptyList()
            return ParadoxExpressionService.resolveAllScriptExpression(element, text, rangeInExpression, unionValueConfig, role)
        }

        override fun getReferences(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiReference> {
            // #374 `union[x]` 同样需要兼容这里，目前来说，这是和 `alias_keys_field[x]` 不同的地方（例如，对于 `union[test_union] = { value[test_flag] }`，其中的 `value[test_flag]` 可以匹配多个节点）
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return emptyList()
            val unionName = configExpression.metadata.value ?: return emptyList()
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first actually matched config atm, event if the result from this config is null or empty
            val processor = ProcessorFactory.find<CwtValueConfig>()
            runWithRecursionGuard("scriptExpression.getReferences.union", unionName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedUnion(element, expression, unionName, configGroup) {
                    processor.process(it)
                }
            }
            val unionValueConfig = processor.result ?: return emptyList()
            return ParadoxExpressionService.getScriptExpressionReferences(element, text, rangeInExpression, unionValueConfig, role)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            val configGroup = context.configGroup
            val configExpression = context.config?.configExpression ?: return
            ProgressManager.checkCanceled()
            // NOTE 3.0.3 recursion guard is required here
            CwtConfigExpansionService.expandUnion(configExpression, configGroup, "scriptExpression.complete") { _, unionValueConfig ->
                val context = context.copy(config = unionValueConfig, configs = setOf(unionValueConfig))
                ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
                true
            }
        }
    }

    /**
     * @see CwtDataTypes.AliasKeysField
     * @see CwtDataTypes.AliasName
     */
    class ForAliasName : ParadoxExpandableScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.AliasKeysField || dataType == CwtDataTypes.AliasName
        }

        // NOTE 3.0.1 recursion guard is required here for various operations

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return false
            val aliasName = configExpression.metadata.value ?: return false
            val aliasGroup = configGroup.aliasGroups.get(aliasName) ?: return false
            // NOTE 3.0.1 recursion guard is required here
            val processor = ProcessorFactory.any<Unit>()
            runWithRecursionGuard("scriptExpression.annotate.alias", aliasName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedAliasKeys(element, expression, aliasName, configGroup) p@{ key ->
                    val aliasConfig = aliasGroup[key]?.firstOrNull() ?: return@p true
                    val r = ParadoxExpressionService.annotateScriptExpression(element, text, rangeInExpression, aliasConfig, holder)
                    if (!r) return@p true
                    processor.process(Unit)
                }
            }
            return processor.result
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return null
            val aliasName = configExpression.metadata.value ?: return null
            val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
            // NOTE 3.0.1 recursion guard is required here
            val processor = ProcessorFactory.find<PsiElement>()
            runWithRecursionGuard("scriptExpression.resolve.alias", aliasName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedAliasKeys(element, expression, aliasName, configGroup) p@{ key ->
                    val aliasConfig = aliasGroup[key]?.firstOrNull() ?: return@p true
                    val r = ParadoxExpressionService.resolveScriptExpression(element, text, rangeInExpression, aliasConfig, role)
                    if (r == null) return@p true
                    processor.process(r)
                }
            }
            return processor.result
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return emptyList()
            val aliasName = configExpression.metadata.value ?: return emptyList()
            val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptyList()
            // NOTE 3.0.1 recursion guard is required here
            val processor = ProcessorFactory.find<List<PsiElement>>()
            runWithRecursionGuard("scriptExpression.resolveAll.alias", aliasName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedAliasKeys(element, expression, aliasName, configGroup) p@{ key ->
                    val aliasConfig = aliasGroup[key]?.firstOrNull() ?: return@p true
                    val r = ParadoxExpressionService.resolveAllScriptExpression(element, text, rangeInExpression, aliasConfig, role).orNull()
                    if (r == null) return@p true
                    processor.process(r)
                }
            }
            return processor.result.orEmpty()
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            val configGroup = context.configGroup
            val configExpression = context.config?.configExpression ?: return
            ProgressManager.checkCanceled()
            // NOTE 3.0.3 recursion guard is required here
            CwtConfigExpansionService.expandAlias(configExpression, configGroup, "scriptExpression.complete") { _, aliasConfigs ->
                val context = context.copy(config = aliasConfigs.first(), configs = aliasConfigs)
                ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
                true
            }
        }
    }
}
