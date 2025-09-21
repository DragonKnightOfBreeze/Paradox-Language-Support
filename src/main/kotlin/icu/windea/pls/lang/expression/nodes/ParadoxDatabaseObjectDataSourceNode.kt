package icu.windea.pls.lang.expression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.mapToArray
import icu.windea.pls.core.orNull
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.ep.resolve.ParadoxDefinitionInheritSupport
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.expression.ParadoxComplexExpressionError
import icu.windea.pls.lang.expression.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.expression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.psi.ParadoxPsiManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class ParadoxDatabaseObjectDataSourceNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val expression: ParadoxDatabaseObjectExpression,
    val isBase: Boolean,
) : ParadoxComplexExpressionNode.Base() {
    val config = expression.typeNode?.config

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey? {
        if (config == null) return null
        if (config.type != null) return ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
        if (config.localisation != null) return ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        return null
    }

    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if (config == null) return null
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        val reference = getReference(element)
        if (reference == null || reference.resolveFirst() != null) return null
        return ParadoxComplexExpressionErrorBuilder.unresolvedDatabaseObject(rangeInExpression, text, config.name)
    }

    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if (config == null) return null
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        return Reference(element, rangeInElement, this)
    }

    fun isForcedBase(): Boolean {
        //referencing base database objects repeatedly is supported, to force show non-swapped form in the game
        //(pre-condition: database object type can be swapped & database object names should be the same)
        if (config == null) return false
        if (config.localisation != null) return false
        if (isBase || config.type == null || config.swapType == null) return false
        val baseName = expression.valueNode?.text?.orNull()
        return baseName != null && baseName == text
    }

    fun isPossibleForcedBase(): Boolean {
        if (config == null) return false
        if (config.localisation != null) return false
        if (isBase || config.type == null || config.swapType == null) return false
        val baseName = expression.valueNode?.text?.orNull()
        return baseName != null
    }

    fun isValidDatabaseObject(element: PsiElement, typeToSearch: String): Boolean {
        if (config == null) return false

        if (config.localisation != null) {
            if (!isBase) return false
            if (element !is ParadoxLocalisationProperty) return false
            if (!element.name.startsWith(typeToSearch)) return false
            return true
        }

        if (config.type == null) return false
        if (element !is ParadoxScriptDefinitionElement) return false
        if (isForcedBase()) return true
        val definitionInfo = element.definitionInfo ?: return false
        if (definitionInfo.name.isEmpty()) return false
        if (!ParadoxDefinitionTypeExpression.resolve(typeToSearch).matches(definitionInfo)) return false
        if (isBase) return true

        //filter out mismatched swap definition vs base definition
        val expectedSuperDefinitionName = expression.valueNode?.text?.orNull() ?: return false
        val expectedSuperDefinitionType = config.type ?: return false
        val superDefinition = ParadoxDefinitionInheritSupport.getSuperDefinition(element, definitionInfo) ?: return false
        val superDefinitionInfo = superDefinition.definitionInfo ?: return false
        if (superDefinitionInfo.name.isEmpty()) return false
        return superDefinitionInfo.name == expectedSuperDefinitionName && ParadoxDefinitionTypeExpression.resolve(expectedSuperDefinitionType).matches(superDefinitionInfo)
    }

    fun getTypeToSearch(isForcedBase: Boolean = false): String? {
        if (config == null) return null
        if (config.localisation != null) {
            if (!isBase) return null
            return config.localisation
        }
        if (config.type == null) return null
        return if (isBase || isForcedBase) config.type else config.swapType
    }

    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        val node: ParadoxDatabaseObjectDataSourceNode
    ) : PsiPolyVariantReferenceBase<ParadoxExpressionElement>(element, rangeInElement) {
        private val name = node.text
        private val project = node.configGroup.project

        override fun handleElementRename(newElementName: String): PsiElement {
            return ParadoxPsiManager.handleElementRename(element, rangeInElement, newElementName)
        }

        //缓存解析结果以优化性能

        private object Resolver : ResolveCache.AbstractResolver<Reference, PsiElement> {
            override fun resolve(ref: Reference, incompleteCode: Boolean) = ref.doResolve()
        }

        private object MultiResolver : ResolveCache.PolyVariantResolver<Reference> {
            override fun resolve(ref: Reference, incompleteCode: Boolean) = ref.doMultiResolve()
        }

        override fun resolve(): PsiElement? {
            return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
        }

        override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
            return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
        }

        private fun doResolve(): PsiElement? {
            val typeToSearch = node.getTypeToSearch(node.isForcedBase())
            if (typeToSearch == null) return null

            if (node.config == null) return null
            val element = element

            if (node.config.localisation != null) {
                if (!node.isBase) return null
                val preferredLocale = selectLocale(element) ?: ParadoxLocaleManager.getPreferredLocaleConfig()
                val selector = selector(project, element).localisation().contextSensitive().preferLocale(preferredLocale)
                return ParadoxLocalisationSearch.search(name, selector).find()
                    ?.takeIf { node.isValidDatabaseObject(it, typeToSearch) }
            }

            val selector = selector(project, element).definition().contextSensitive()
            return ParadoxDefinitionSearch.search(name, typeToSearch, selector).find()
                ?.takeIf { node.isValidDatabaseObject(it, typeToSearch) }
        }

        private fun doMultiResolve(): Array<out ResolveResult> {
            val typeToSearch = node.getTypeToSearch(node.isForcedBase())
            if (typeToSearch == null) return ResolveResult.EMPTY_ARRAY

            if (node.config == null) return ResolveResult.EMPTY_ARRAY
            val element = element

            if (node.config.localisation != null) {
                if (!node.isBase) return ResolveResult.EMPTY_ARRAY
                val preferredLocale = selectLocale(element) ?: ParadoxLocaleManager.getPreferredLocaleConfig()
                val selector = selector(project, element).localisation().contextSensitive().preferLocale(preferredLocale)
                return ParadoxLocalisationSearch.search(name, selector).findAll()
                    .filter { node.isValidDatabaseObject(it, typeToSearch) }
                    .mapToArray { PsiElementResolveResult(it) }
            }

            val selector = selector(project, element).definition().contextSensitive()
            return ParadoxDefinitionSearch.search(name, typeToSearch, selector).findAll()
                .filter { node.isValidDatabaseObject(it, typeToSearch) }
                .mapToArray { PsiElementResolveResult(it) }
        }
    }

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, expression: ParadoxDatabaseObjectExpression, isBase: Boolean): ParadoxDatabaseObjectDataSourceNode {
            return ParadoxDatabaseObjectDataSourceNode(text, textRange, configGroup, expression, isBase)
        }
    }
}
