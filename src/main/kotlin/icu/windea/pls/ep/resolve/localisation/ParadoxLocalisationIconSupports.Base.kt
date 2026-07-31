package icu.windea.pls.ep.resolve.localisation

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapNotNullFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.processAsync
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionLookupProvider
import icu.windea.pls.lang.codeInsight.completion.addToResult
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

@Suppress("SameParameterValue")
abstract class ParadoxCompositeLocalisationIconSupport : ParadoxLocalisationIconSupport {
    private val _supports = mutableListOf<ParadoxLocalisationIconSupport>()

    val supports: List<ParadoxLocalisationIconSupport> get() = _supports

    init {
        registerSupports()
    }

    protected abstract fun registerSupports()

    protected fun fromDefinition(definitionType: String) {
        _supports += ParadoxDefinitionBasedLocalisationIconSupport(definitionType, { it }, { it })
    }

    protected fun fromDefinition(definitionType: String, definitionNameGetter: (name: String) -> String?, nameGetter: (definitionName: String) -> String?) {
        _supports += ParadoxDefinitionBasedLocalisationIconSupport(definitionType, definitionNameGetter, nameGetter)
    }

    protected fun fromImageFile(pathExpressionString: String) {
        _supports += ParadoxImageFileBasedLocalisationIconSupport(pathExpressionString)
    }

    final override fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
        _supports.forEachFast { support ->
            ProgressManager.checkCanceled()
            support.resolve(name, element, project)?.let { return it }
        }
        return null
    }

    final override fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
        _supports.forEachFast { support ->
            ProgressManager.checkCanceled()
            support.resolveAll(name, element, project).orNull()?.let { return it }
        }
        return emptyList()
    }

    final override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        _supports.forEachFast { support ->
            ProgressManager.checkCanceled()
            support.complete(context, result)
        }
    }
}

class ParadoxDefinitionBasedLocalisationIconSupport(
    val definitionType: String,
    val definitionNameGetter: (name: String) -> String?,
    val nameGetter: (definitionName: String) -> String?,
) : ParadoxLocalisationIconSupport {
    override fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
        val definitionName = definitionNameGetter(name)
        if (definitionName.isNullOrEmpty()) return null
        val definitionSelector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
        val definition = ParadoxDefinitionSearch.searchElement(definitionName, definitionType, definitionSelector).find()
        return definition
    }

    override fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
        val definitionName = definitionNameGetter(name)
        if (definitionName.isNullOrEmpty()) return emptySet()
        val definitionSelector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
        val definitions = ParadoxDefinitionSearch.searchElement(definitionName, definitionType, definitionSelector).findAll()
        return definitions
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val definitionSelector = ParadoxDefinitionSearch.selector(context.project, context.file).contextSensitive().distinct()
        ParadoxDefinitionSearch.searchElement(null, definitionType, definitionSelector).processAsync p@{ definition ->
            ProgressManager.checkCanceled()
            val definitionInfo = definition.definitionInfo ?: return@p true
            val name = nameGetter(definitionInfo.name)
            if (name.isNullOrEmpty()) return@p true
            val typeFile = definition.containingFile
            val hintText = " from ${definitionInfo.type} ${definitionInfo.name}"
            val lookupElement = ParadoxCompletionLookupProvider.forLocalisationIcon(definition, name, typeFile, hintText)
            lookupElement.addToResult(context, result)
            true
        }
    }
}

class ParadoxImageFileBasedLocalisationIconSupport(
    pathExpressionString: String
) : ParadoxLocalisationIconSupport {
    val pathExpression = CwtDataExpression.resolve(pathExpressionString, CwtDataExpressionRole.Value)

    override fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
        val fileSelector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
        val file = ParadoxFilePathSearch.search(name, pathExpression, fileSelector).find()
        return file?.toPsiFile(project)
    }

    override fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
        val fileSelector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
        val files = ParadoxFilePathSearch.search(name, pathExpression, fileSelector).findAll()
        return files.mapNotNullFast { it.toPsiFile(project) }
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val hintText = " from image file"
        val fileSelector = ParadoxFilePathSearch.selector(context.project, context.file).contextSensitive().distinct()
        ParadoxFilePathSearch.search(null, pathExpression, fileSelector).processAsync p@{ file ->
            val name = file.nameWithoutExtension
            if (name.isEmpty()) return@p true
            val psiFile = file.toPsiFile(context.project) ?: return@p true
            val lookupElement = ParadoxCompletionLookupProvider.forLocalisationIcon(psiFile, name, psiFile, hintText)
            lookupElement.addToResult(context, result)
        }
    }
}
