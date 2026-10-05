package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.GotoTargetHandler
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.collections.synced
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName
import icu.windea.pls.lang.resolve.ParadoxLocationExpressionService
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

// com.intellij.testIntegration.GotoTestOrCodeHandler

class GotoRelatedImagesHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedImages"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        when {
            element !is ParadoxScriptStringExpressionElement -> return null
            element.isDefinitionTypeKeyOrName() -> {
                val definition = selectScope { element.parentDefinition() } ?: return null
                val definitionInfo = definition.definitionInfo ?: return null
                if (definitionInfo.name.isEmpty()) return null // 排除匿名定义
                val name = ParadoxPsiPresentationService.getNameForDefinition(definition) ?: return null
                val imageInfos = definitionInfo.images
                if (imageInfos.isEmpty()) return GotoData(definition, PsiElement.EMPTY_ARRAY, emptyList())
                val targets = mutableListOf<PsiElement>().synced()
                runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedImages.search.definition", name)) {
                    // need read actions here if necessary
                    for ((_, locationExpression) in imageInfos) {
                        ProgressManager.checkCanceled()
                        readAction {
                            val resolveResult = ParadoxLocationExpressionService.resolve(locationExpression, definition, definitionInfo)
                            if (resolveResult != null && resolveResult.elements.isNotEmpty()) {
                                targets.addAll(resolveResult.elements)
                            }
                        }
                    }
                }
                return GotoData(definition, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
            }
            else -> {
                val modifierElement = ParadoxModifierManager.resolveModifier(element) ?: return null
                val targets = mutableListOf<PsiElement>().synced()
                runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedImages.search.modifier", modifierElement.name)) {
                    // need read actions here if necessary
                    readAction {
                        val paths = ParadoxModifierManager.getModifierIconPaths(modifierElement.name, modifierElement)
                        val iconFiles = paths.firstNotNullOfOrNull { path ->
                            val iconSelector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
                            ParadoxFilePathSearch.searchModifierIcon(path, iconSelector).findAll().orNull()
                        }
                        if (iconFiles != null) targets.addAll(targets)
                    }
                }
                return GotoData(element, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
            }
        }
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptExpressionElement? {
        return ParadoxPsiFileService.findScriptExpression(file, offset)
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        run {
            when {
                sourceElement !is ParadoxScriptStringExpressionElement -> {}
                sourceElement.isDefinitionTypeKeyOrName() -> {
                    val definitionName = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: return@run
                    return ChronicleBundle.message("script.goto.relatedImages.chooseTitle.d", definitionName.escapeXml())
                }
                else -> {
                    val modifierElement = sourceElement.castOrNull<ParadoxScriptStringExpressionElement>()
                        ?.let { ParadoxModifierManager.resolveModifier(it) } ?: return@run
                    val modifierName = modifierElement.name
                    return ChronicleBundle.message("script.goto.relatedImages.chooseTitle.m", modifierName.escapeXml())
                }
            }
        }
        val sourceName = sourceElement.text.unquote()
        return ChronicleBundle.message("script.goto.relatedImages.chooseTitle", sourceName.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        run {
            when {
                sourceElement !is ParadoxScriptStringExpressionElement -> {}
                sourceElement.isDefinitionTypeKeyOrName() -> {
                    val definitionName = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: return@run
                    return ChronicleBundle.message("script.goto.relatedImages.findUsagesTitle.d", definitionName.escapeXml())
                }
                else -> {
                    val modifierElement = sourceElement.castOrNull<ParadoxScriptStringExpressionElement>()
                        ?.let { ParadoxModifierManager.resolveModifier(it) } ?: return@run
                    val modifierName = modifierElement.name
                    return ChronicleBundle.message("script.goto.relatedImages.findUsagesTitle.m", modifierName.escapeXml())
                }
            }
        }
        val sourceName = sourceElement.text.unquote()
        return ChronicleBundle.message("script.goto.relatedImages.findUsagesTitle", sourceName.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedImages.notFoundMessage")
    }
}
