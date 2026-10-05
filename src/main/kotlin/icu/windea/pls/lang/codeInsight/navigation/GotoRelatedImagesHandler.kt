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
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.collections.synced
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.resolve.ParadoxLocationExpressionService
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxModifierManager

// com.intellij.testIntegration.GotoTestOrCodeHandler

class GotoRelatedImagesHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedImages"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val targets = mutableListOf<PsiElement>().synced()
        var sourceElement: PsiElement? = null
        run {
            // 定义（相关图片）
            if (sourceElement != null) return@run
            val element = ParadoxPsiFileService.findScriptExpression(file, offset) ?: return@run
            if (!element.isDefinitionTypeKeyOrName()) return@run
            val definition = selectScope { element.parentDefinition() } ?: return@run
            val definitionInfo = definition.definitionInfo ?: return@run
            if (definitionInfo.name.isEmpty()) return@run // 排除匿名定义
            sourceElement = definition
            val name = ParadoxPsiPresentationService.getNameForDefinition(definition) ?: return@run
            val imageInfos = definitionInfo.images
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedImages.search.definition", name.orAnonymous().escapeXml())) {
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
        }
        run {
            // 修正（相关图片）
            if (sourceElement != null) return@run
            val element = file.findReferenceAt(offset)?.resolve() ?: return@run
            if (element !is ParadoxModifierLightElement) return@run
            sourceElement = element
            val name = ParadoxPsiPresentationService.getNameForModifier(element) ?: return@run
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedImages.search.modifier", name.orAnonymous().escapeXml())) {
                // need read actions here if necessary
                readAction {
                    val paths = ParadoxModifierManager.getModifierIconPaths(name, element)
                    val iconFiles = paths.firstNotNullOfOrNull { path ->
                        val iconSelector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
                        ParadoxFilePathSearch.searchModifierIcon(path, iconSelector).findAll().orNull()
                    }
                    if (iconFiles != null) targets.addAll(iconFiles.mapNotNull { it.toPsiFile(project) })
                }
            }
        }
        if (targets.isEmpty() || sourceElement == null) return null // unavailable
        targets.removeIf { it == sourceElement } // remove current target from targets
        return GotoData(sourceElement, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedImages.chooseTitle.d", name.orAnonymous().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForModifier(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedImages.chooseTitle.m", name.orAnonymous().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedImages.chooseTitle", name.orAnonymous().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedImages.findUsagesTitle.d", name.orAnonymous().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForModifier(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedImages.findUsagesTitle.m", name.orAnonymous().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedImages.findUsagesTitle", name.orAnonymous().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedImages.notFoundMessage")
    }
}
