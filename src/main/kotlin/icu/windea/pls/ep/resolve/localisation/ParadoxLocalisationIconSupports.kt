package icu.windea.pls.ep.resolve.localisation

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.addPrefix
import icu.windea.pls.core.collections.mapNotNullFast
import icu.windea.pls.core.processAsync
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionFactory
import icu.windea.pls.lang.codeInsight.completion.addToResult
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

class ParadoxFromIconFileLocalisationIconSupport : ParadoxLocalisationIconSupport {
    private val pathPrefix = "gfx/interface/icons/"
    private val pathExpression = CwtDataExpression.resolve("icon[$pathPrefix]")

    override fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
        val selector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
        val query = ParadoxFilePathSearch.searchImage(name, pathExpression, selector) // 3.0.1 optimize: limit file extensions
        return query.find()?.toPsiFile(project)
    }

    override fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
        val selector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
        val query = ParadoxFilePathSearch.searchImage(name, pathExpression, selector) // 3.0.1 optimize: limit file extensions
        val files = query.findAll()
        return files.mapNotNullFast { it.toPsiFile(project) }
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val hintText = " from icon files"
        val selector = ParadoxFilePathSearch.selector(context.project, context.file).contextSensitive().distinct()
        val query = ParadoxFilePathSearch.searchImage(null, pathExpression, selector) // 3.0.1 optimize: limit file extensions
        query.processAsync p@{ file ->
            val name = file.nameWithoutExtension
            if (name.isEmpty()) return@p true
            val psiFile = file.toPsiFile(context.project) ?: return@p true
            val lookupElement = ParadoxCompletionFactory.forLocalisationIcon(psiFile, name, psiFile, hintText)
            lookupElement.addToResult(context, result)
        }
    }
}

class ParadoxFromModifierIconFileLocalisationIconSupport : ParadoxLocalisationIconSupport {
    private val pathPrefix = "gfx/interface/icons/modifiers/"

    override fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
        val filePath = pathPrefix + name
        val selector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
        val query = ParadoxFilePathSearch.searchModifierIcon(filePath, selector) // 3.0.2 #385 also case-insensitive
        return query.find()?.toPsiFile(project)
    }

    override fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
        val filePath = pathPrefix + name
        val selector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
        val query = ParadoxFilePathSearch.searchModifierIcon(filePath, selector) // 3.0.2 #385 also case-insensitive
        val files = query.findAll()
        return files.mapNotNullFast { it.toPsiFile(project) }
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val hintText = " from modifier icon files"
        val selector = ParadoxFilePathSearch.selector(context.project, context.file).contextSensitive().distinct()
        val query = ParadoxFilePathSearch.searchModifierIcon(null, selector)
        query.processAsync p@{ file ->
            val name = file.nameWithoutExtension
            if (name.isEmpty()) return@p true
            val psiFile = file.toPsiFile(context.project) ?: return@p true
            val lookupElement = ParadoxCompletionFactory.forLocalisationIcon(psiFile, name, psiFile, hintText)
                ?.withCaseSensitivity(false) // 3.0.2 #385 also case-insensitive
            lookupElement.addToResult(context, result)
        }
    }
}

class ParadoxBaseLocalisationIconSupport : ParadoxCompositeLocalisationIconSupport() {
    override fun registerSupports() {
        fromDefinition(ParadoxDefinitionTypes.sprite, { it.addPrefix("GFX_text_") }, { it.removePrefixOrNull("GFX_text_") })
        fromDefinition(ParadoxDefinitionTypes.sprite, { it.addPrefix("GFX_") }, { it.removePrefixOrNull("GFX_") })
    }

    @ForGameType(ParadoxGameType.Stellaris)
    class Stellaris : ParadoxCompositeLocalisationIconSupport() {
        override fun supports(gameType: ParadoxGameType) = gameType == ParadoxGameType.Stellaris

        override fun registerSupports() {
            fromDefinition(ParadoxDefinitionTypes.job, { it.removePrefixOrNull("job_") }, { it.addPrefix("job_") })
            fromDefinition(ParadoxDefinitionTypes.swappedJob, { it.removePrefixOrNull("job_") }, { it.addPrefix("job_") })
            fromDefinition(ParadoxDefinitionTypes.resource)
        }
    }
}
