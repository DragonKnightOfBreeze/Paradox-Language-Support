package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.orUnresolved
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxModifierManager

class GotoRelatedImagesHandler : GotoHandlerBase() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedImages"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        run {
            // 定义（相关图片）
            val expressionElement = ParadoxPsiFileService.findScriptExpression(file, offset) ?: return@run
            if (!expressionElement.isDefinitionTypeKeyOrName()) return@run
            val sourceElement = selectScope { expressionElement.parentDefinition() } ?: return@run
            val name = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: return@run
            if (name.isEmpty()) return null // 3.0.4 排除匿名或者无法解析的情况
            val targets = mutableListOf<PsiElement>()
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedImages.search.definition", name.escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxDefinitionManager.getRelatedImages(sourceElement))
                }
            }
            return getGotoData(sourceElement, targets)
        }
        run {
            // 修正（相关图片）
            val sourceElement = file.findReferenceAt(offset)?.resolve() ?: return@run
            if (sourceElement !is ParadoxModifierLightElement) return@run
            val name = ParadoxPsiPresentationService.getNameForModifier(sourceElement) ?: return@run
            if (name.isEmpty()) return null // 3.0.4 排除匿名或者无法解析的情况
            val targets = mutableListOf<PsiElement>()
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedImages.search.modifier", name.escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxModifierManager.getRelatedImages(name, sourceElement))
                }
            }
            return getGotoData(sourceElement, targets)
        }
        return null
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedImages.chooseTitle.d", name.orUnresolved().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForModifier(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedImages.chooseTitle.m", name.orUnresolved().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedImages.chooseTitle", name.orUnresolved().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedImages.findUsagesTitle.d", name.orUnresolved().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForModifier(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedImages.findUsagesTitle.m", name.orUnresolved().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedImages.findUsagesTitle", name.orUnresolved().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedImages.notFoundMessage")
    }
}
