package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.GotoTargetHandler
import com.intellij.codeInsight.navigation.activateFileWithPsiElement
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.collections.synced
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.CwtLocationExpressionManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.findParentDefinition
import icu.windea.pls.script.psi.isDefinitionRootKeyOrName

//com.intellij.testIntegration.GotoTestOrCodeHandler

class GotoRelatedImagesHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedImages"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        // need read actions here if necessary
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        when {
            element !is ParadoxScriptStringExpressionElement -> return null
            element.isDefinitionRootKeyOrName() -> {
                val definition = element.findParentDefinition() ?: return null
                val definitionInfo = definition.definitionInfo ?: return null
                val imageInfos = definitionInfo.images
                if (imageInfos.isEmpty()) return GotoData(definition, PsiElement.EMPTY_ARRAY, emptyList())
                val targets = mutableListOf<PsiElement>().synced()
                val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
                    for ((_, locationExpression) in imageInfos) {
                        ProgressManager.checkCanceled()
                        runReadAction {
                            val resolveResult = CwtLocationExpressionManager.resolve(locationExpression, definition, definitionInfo)
                            if (resolveResult != null && resolveResult.elements.isNotEmpty()) {
                                targets.addAll(resolveResult.elements)
                            }
                        }
                    }
                }, PlsBundle.message("script.goto.relatedImages.search.1", definitionInfo.name), true, project)
                if (!runResult) return null
                return GotoData(definition, targets.distinct().toTypedArray(), emptyList())
            }
            else -> {
                val modifierElement = ParadoxModifierManager.resolveModifier(element) ?: return null
                val targets = mutableListOf<PsiElement>().synced()
                val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
                    runReadAction {
                        val paths = ParadoxModifierManager.getModifierIconPaths(modifierElement.name, modifierElement)
                        val iconFiles = paths.firstNotNullOfOrNull { path ->
                            val iconSelector = selector(project, element).file().contextSensitive()
                            ParadoxFilePathSearch.searchIcon(path, iconSelector).findAll().orNull()
                        }
                        if (iconFiles != null) targets.addAll(targets)
                    }
                }, PlsBundle.message("script.goto.relatedImages.search.2", modifierElement.name), true, project)
                if (!runResult) return null
                return GotoData(element, targets.distinct().toTypedArray(), emptyList())
            }
        }
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptExpressionElement? {
        return ParadoxPsiFinder.findScriptExpression(file, offset)
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        run {
            when {
                sourceElement !is ParadoxScriptStringExpressionElement -> {}
                sourceElement.isDefinitionRootKeyOrName() -> {
                    val definitionInfo = sourceElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo ?: return@run
                    val definitionName = definitionInfo.name.or.anonymous()
                    return PlsBundle.message("script.goto.relatedImages.chooseTitle.1", definitionName.escapeXml())
                }
                else -> {
                    val modifierElement = sourceElement.castOrNull<ParadoxScriptStringExpressionElement>()
                        ?.let { ParadoxModifierManager.resolveModifier(it) } ?: return@run
                    val modifierName = modifierElement.name
                    return PlsBundle.message("script.goto.relatedImages.chooseTitle.2", modifierName.escapeXml())
                }
            }
        }
        val sourceName = sourceElement.text.unquote()
        return PlsBundle.message("script.goto.relatedImages.chooseTitle.0", sourceName.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        run {
            when {
                sourceElement !is ParadoxScriptStringExpressionElement -> {}
                sourceElement.isDefinitionRootKeyOrName() -> {
                    val definitionInfo = sourceElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo ?: return@run
                    val definitionName = definitionInfo.name.or.anonymous()
                    return PlsBundle.message("script.goto.relatedImages.findUsagesTitle.1", definitionName.escapeXml())
                }
                else -> {
                    val modifierElement = sourceElement.castOrNull<ParadoxScriptStringExpressionElement>()
                        ?.let { ParadoxModifierManager.resolveModifier(it) } ?: return@run
                    val modifierName = modifierElement.name
                    return PlsBundle.message("script.goto.relatedImages.findUsagesTitle.2", modifierName.escapeXml())
                }
            }
        }
        val sourceName = sourceElement.text.unquote()
        return PlsBundle.message("script.goto.relatedImages.findUsagesTitle.0", sourceName.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return PlsBundle.message("script.goto.relatedImages.notFoundMessage")
    }

    override fun navigateToElement(descriptor: Navigatable) {
        if (descriptor is PsiElement) {
            activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}
