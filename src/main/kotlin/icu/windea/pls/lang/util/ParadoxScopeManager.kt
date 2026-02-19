package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.parents
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.resolved
import icu.windea.pls.config.resolvedOrNull
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.match.ParadoxScopeMatchService
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.resolve.ParadoxScopeService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.ParadoxScopeId
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.isBlockMember

@Suppress("UNUSED_PARAMETER")
object ParadoxScopeManager {
    object Keys : KeyRegistry() {
        val cachedScopeContext by registerKey<CachedValue<ParadoxScopeContext>>(Keys)
    }

    const val maxScopeLinkSize = 5

    fun findParentMember(element: PsiElement, withSelf: Boolean): ParadoxScriptMember? {
        return element.parents(withSelf)
            .find { it is ParadoxDefinitionElement || (it is ParadoxScriptBlock && it.isBlockMember()) }
            .castOrNull<ParadoxScriptMember>()
    }

    fun matchesScope(scopeContext: ParadoxScopeContext?, scopeToMatch: String, configGroup: CwtConfigGroup): Boolean {
        return ParadoxScopeMatchService.matchesScope(scopeContext, scopeToMatch, configGroup)
    }

    fun matchesScope(scopeContext: ParadoxScopeContext?, scopesToMatch: Set<String>?, configGroup: CwtConfigGroup): Boolean {
        return ParadoxScopeMatchService.matchesScope(scopeContext, scopesToMatch, configGroup)
    }

    fun matchesScopeGroup(scopeContext: ParadoxScopeContext?, scopeGroupToMatch: String, configGroup: CwtConfigGroup): Boolean {
        return ParadoxScopeMatchService.matchesScopeGroup(scopeContext, scopeGroupToMatch, configGroup)
    }

    fun isScopeContextChanged(element: ParadoxScriptMember, scopeContext: ParadoxScopeContext): Boolean {
        // does not have scope context -> changed always
        val parentMember = findParentMember(element, withSelf = false)
        if (parentMember == null) return true
        val parentScopeContext = getScopeContext(parentMember)
        if (parentScopeContext == null) return true
        if (parentScopeContext != scopeContext) return true
        if (!isScopeContextSupported(parentMember)) return true
        return false
    }

    /**
     * @param indirect 是否包括间接支持作用域上下文的情况（如事件）。
     */
    fun isScopeContextSupported(element: ParadoxScriptMember, indirect: Boolean = false): Boolean {
        return ParadoxScopeService.isScopeContextSupportedForMember(element, indirect)
    }

    fun getScopeContext(element: ParadoxScriptMember): ParadoxScopeContext? {
        // from cache
        return CachedValuesManager.getCachedValue(element, Keys.cachedScopeContext) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val value = ParadoxScopeService.evaluateScopeContextForMember(element)
                value.withDependencyItems(element.containingFile, ParadoxModificationTrackers.Scope)
            }
        }
    }

    fun getScopeContext(element: ParadoxDynamicValueElement): ParadoxScopeContext {
        // from cache
        return CachedValuesManager.getCachedValue(element, Keys.cachedScopeContext) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val value = ParadoxScopeService.evaluateScopeContextForDynamicValue(element)
                value.withDependencyItems(element)
            }
        }
    }

    fun getScopeContext(element: ParadoxDynamicValueElement, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        // only receive push scope (this scope), ignore others (like root scope, etc.)
        val scopeContext = getScopeContext(element)
        return inputScopeContext.resolveNext(scopeContext.scope.id)
    }

    fun getScopeContext(element: PsiElement, expression: ParadoxScopeFieldExpression, configExpression: CwtDataExpression): ParadoxScopeContext? {
        val memberElement = findParentMember(element, withSelf = true) ?: return null
        return ParadoxScopeService.evaluateScopeContextForExpression(memberElement, expression, configExpression)
    }

    fun getScopeContext(element: ParadoxExpressionElement, expression: ParadoxScopeFieldExpression, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        return ParadoxScopeService.evaluateScopeContextForExpression(element, expression, inputScopeContext)
    }

    fun getScopeContext(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        return ParadoxScopeService.evaluateScopeContextForNode(element, node, inputScopeContext)
    }

    fun getScopeContext(config: CwtMemberConfig<*>, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext? {
        // 优先基于内联前的规则，如果没有，再基于内联后的规则
        val replaceScopes = config.optionData.replaceScopes ?: config.resolvedOrNull()?.optionData?.replaceScopes
        val pushScope = config.optionData.pushScope ?: config.resolved().optionData.pushScope
        if (replaceScopes != null) {
            return ParadoxScopeContext.get(replaceScopes)
        } else if (pushScope != null) {
            return inputScopeContext.resolveNext(pushScope)
        }
        return null
    }

    fun getSupportedScopes(modifierCategories: Map<String, CwtModifierCategoryConfig>): Set<String> {
        val categoryConfigs = modifierCategories.values
        return when {
            categoryConfigs.isEmpty() -> ParadoxScopeId.anyScopeIdSet
            categoryConfigs.any { it.supportedScopes == ParadoxScopeId.anyScopeIdSet } -> ParadoxScopeId.anyScopeIdSet
            else -> categoryConfigs.flatMapTo(mutableSetOf()) { it.supportedScopes }
        }
    }

    fun getSupportedScopes(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, inputScopeContext: ParadoxScopeContext): Set<String>? {
        return ParadoxScopeService.evaluateSupportedScopesForNode(element, node, inputScopeContext)
    }
}
