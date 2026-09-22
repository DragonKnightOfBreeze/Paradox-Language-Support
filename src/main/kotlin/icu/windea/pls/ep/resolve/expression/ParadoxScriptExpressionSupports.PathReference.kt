package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.util.ParadoxExpressionSupportFactory
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.script.highlighting.ParadoxScriptHighlighterColors

/**
 * @see CwtDataTypeSets.PathReference
 */
class ParadoxPathReferenceScriptExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType in CwtDataTypeSets.PathReference
    }

    override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
        val attributesKey = ParadoxScriptHighlighterColors.PATH_REFERENCE
        ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
        return true
    }

    override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        if (text.isEmpty()) return null

        val configExpression = config.configExpression ?: return null
        val configGroup = config.configGroup
        val project = configGroup.project

        // absolute file path -> use `VfsUtil.findFile`
        if (configExpression.type == CwtDataTypes.AbsoluteFilePath) return text.toVirtualFile()?.toPsiFile(project)

        val pathReference = text.normalizePath()
        if (pathReference.isEmpty()) return null
        val selector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
        return ParadoxFilePathSearch.search(pathReference, configExpression, selector).find()?.toPsiFile(project)
    }

    override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
        val configExpression = config.configExpression ?: return emptyList()
        val configGroup = config.configGroup
        val project = configGroup.project

        if (configExpression.type == CwtDataTypes.AbsoluteFilePath) {
            return text.toVirtualFile()?.toPsiFile(project).to.singletonListOrEmpty()
        }

        val pathReference = text.normalizePath()
        if (pathReference.isEmpty()) return emptyList()
        val selector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
        return ParadoxFilePathSearch.search(pathReference, configExpression, selector).findAll().mapNotNull { it.toPsiFile(project) }
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况
        ParadoxExpressionCompletionManager.completePathReference(context, result)
    }
}
