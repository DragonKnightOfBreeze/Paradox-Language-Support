package icu.windea.pls.ep.icon

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*

@Suppress("SameParameterValue")
abstract class CompositeParadoxLocalisationIconSupport : ParadoxLocalisationIconSupport {
    val supports = mutableListOf<ParadoxLocalisationIconSupport>()

    protected fun fromDefinition(definitionType: String) {
        supports += DefinitionBasedParadoxLocalisationIconSupport(definitionType) { it }
    }

    protected fun fromDefinition(definitionType: String, definitionNameProvider: (String) -> String?) {
        supports += DefinitionBasedParadoxLocalisationIconSupport(definitionType, definitionNameProvider)
    }

    protected fun fromImageFile(pathExpressionString: String) {
        supports += ImageFileBasedParadoxLocalisationIconSupport(pathExpressionString)
    }

    final override fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
        return supports.firstNotNullOfOrNull { it.resolve(name, element, project) }
    }

    final override fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
        return supports.firstNotNullOfOrNull { it.resolveAll(name, element, project).orNull() }.orEmpty()
    }
}

class DefinitionBasedParadoxLocalisationIconSupport(
    val definitionType: String,
    val definitionNameProvider: (String) -> String?
) : ParadoxLocalisationIconSupport {
    override fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
        val definitionName = definitionNameProvider(name)
        if (definitionName.isNullOrEmpty()) return null
        val definitionSelector = selector(project, element).definition().contextSensitive()
        val definition = ParadoxDefinitionSearch.search(definitionName, definitionType, definitionSelector).find()
        return definition
    }

    override fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
        val definitionName = definitionNameProvider(name)
        if (definitionName.isNullOrEmpty()) return emptySet()
        val definitionSelector = selector(project, element).definition().contextSensitive()
        val definitions = ParadoxDefinitionSearch.search(definitionName, definitionType, definitionSelector).findAll()
        return definitions
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
}
