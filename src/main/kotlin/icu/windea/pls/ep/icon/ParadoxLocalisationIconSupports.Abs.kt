package icu.windea.pls.ep.icon

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.icon
import icu.windea.pls.core.processQueryAsync
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.parameters
import icu.windea.pls.lang.codeInsight.completion.withCompletionId
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.distinctByFilePath
import icu.windea.pls.lang.search.selector.distinctByName
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

@Suppress("SameParameterValue")
abstract class CompositeParadoxLocalisationIconSupport : ParadoxLocalisationIconSupport {
    val supports = mutableListOf<ParadoxLocalisationIconSupport>()

    protected fun fromDefinition(definitionType: String) {
        supports += DefinitionBasedParadoxLocalisationIconSupport(definitionType, { it }, { it })
    }

    protected fun fromDefinition(definitionType: String, definitionNameGetter: (name: String) -> String?, nameGetter: (definitionName: String) -> String?) {
        supports += DefinitionBasedParadoxLocalisationIconSupport(definitionType, definitionNameGetter, nameGetter)
    }

    protected fun fromImageFile(pathExpressionString: String) {
        supports += ImageFileBasedParadoxLocalisationIconSupport(pathExpressionString)
    }

    final override fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
        return supports.firstNotNullOfOrNull {
            ProgressManager.checkCanceled()
            it.resolve(name, element, project)
        }
    }

    final override fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
        return supports.firstNotNullOfOrNull {
            ProgressManager.checkCanceled()
            it.resolveAll(name, element, project).orNull()
        }.orEmpty()
    }

    final override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        supports.forEach {
            ProgressManager.checkCanceled()
            it.complete(context, result)
        }
    }
}

class DefinitionBasedParadoxLocalisationIconSupport(
    val definitionType: String,
    val definitionNameGetter: (name: String) -> String?,
    val nameGetter: (definitionName: String) -> String?,
) : ParadoxLocalisationIconSupport {
    override fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
        val definitionName = definitionNameGetter(name)
        if (definitionName.isNullOrEmpty()) return null
        val definitionSelector = selector(project, element).definition().contextSensitive()
        val definition = ParadoxDefinitionSearch.search(definitionName, definitionType, definitionSelector).find()
        return definition
    }

    override fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
        val definitionName = definitionNameGetter(name)
        if (definitionName.isNullOrEmpty()) return emptySet()
        val definitionSelector = selector(project, element).definition().contextSensitive()
        val definitions = ParadoxDefinitionSearch.search(definitionName, definitionType, definitionSelector).findAll()
        return definitions
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val icon = PlsIcons.Nodes.LocalisationIcon //使用特定图标
        val originalFile = context.parameters?.originalFile ?: return
        val project = originalFile.project
        val definitionSelector = selector(project, originalFile).definition().contextSensitive().distinctByName()
        ParadoxDefinitionSearch.search(definitionType, definitionSelector).processQueryAsync p@{ definition ->
            ProgressManager.checkCanceled()
            val definitionInfo = definition.definitionInfo ?: return@p true
            val name = nameGetter(definitionInfo.name)
            if (name.isNullOrEmpty()) return@p true

            val tailText = " from ${definitionInfo.type} ${definitionInfo.name}"
            val typeFile = definition.containingFile
            val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
                .withTailText(tailText, true)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withCompletionId()
            result.addElement(lookupElement, context)
            true
        }
    }
}

class ImageFileBasedParadoxLocalisationIconSupport(
    pathExpressionString: String
) : ParadoxLocalisationIconSupport {
    val pathExpression = CwtDataExpression.resolve(pathExpressionString, false)

    override fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
        val fileSelector = selector(project, element).file().contextSensitive()
        val file = ParadoxFilePathSearch.search(name, pathExpression, fileSelector).find()
        return file?.toPsiFile(project)
    }

    override fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
        val fileSelector = selector(project, element).file().contextSensitive()
        val files = ParadoxFilePathSearch.search(name, pathExpression, fileSelector).findAll()
        return files.mapNotNull { it.toPsiFile(project) }
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val icon = PlsIcons.Nodes.LocalisationIcon //使用特定图标
        val tailText = " from image file"
        val originalFile = context.parameters?.originalFile ?: return
        val project = originalFile.project
        val fileSelector = selector(project, originalFile).file().contextSensitive().distinctByFilePath()
        ParadoxFilePathSearch.search(pathExpression, fileSelector).processQueryAsync p@{ file ->
            ProgressManager.checkCanceled()
            val name = file.nameWithoutExtension
            val psiFile = file.toPsiFile(project) ?: return@p true

            val lookupElement = LookupElementBuilder.create(psiFile, name).withIcon(icon)
                .withTailText(tailText, true)
                .withTypeText(psiFile.name, psiFile.icon, true)
                .withCompletionId()
            result.addElement(lookupElement, context)
            true
        }
    }
}
