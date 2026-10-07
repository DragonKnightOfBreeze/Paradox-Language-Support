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
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName
import icu.windea.pls.lang.psi.light.ParadoxLocalisationSnippetLightElement
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.lang.util.ParadoxSnippetManager

class GotoRelatedLocalisationsHandler : GotoHandlerBase() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedLocalisations"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        run {
            // 本地化引用片段（相关本地化）
            val sourceElement = file.findReferenceAt(offset)?.resolve() ?: return@run
            if (sourceElement !is ParadoxLocalisationSnippetLightElement) return@run
            val name = ParadoxPsiPresentationService.getNameForLocalisationSnippet(sourceElement) ?: return@run
            if (name.isEmpty()) return null // 3.0.4 排除匿名或者无法解析的情况
            val targets = mutableListOf<PsiElement>()
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedLocalisations.search.s", name.escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxSnippetManager.getRelatedLocalisations(sourceElement, preferredLocale))
                }
            }
            return getGotoData(sourceElement, targets)
        }
        run {
            // 封装变量（相关本地化）
            val sourceElement = ParadoxPsiFileService.findScriptedVariable(file, offset) { BY_NAME } ?: return@run
            if (!ParadoxPsiMatchService.isScriptedVariable(sourceElement)) return@run
            val name = ParadoxPsiPresentationService.getNameForScriptedVariable(sourceElement) ?: return@run
            if (name.isEmpty()) return null // 3.0.4 排除匿名或者无法解析的情况
            val targets = mutableListOf<PsiElement>()
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedLocalisations.search.sv", name.escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxScriptedVariableManager.getRelatedLocalisations(sourceElement, preferredLocale))
                }
            }
            return getGotoData(sourceElement, targets)
        }
        run {
            // 定义（相关本地化）
            val expressionElement = ParadoxPsiFileService.findScriptExpression(file, offset) ?: return@run
            if (!expressionElement.isDefinitionTypeKeyOrName()) return@run
            val sourceElement = selectScope { expressionElement.parentDefinition() } ?: return@run
            val name = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: return@run
            if (name.isEmpty()) return null // 3.0.4 排除匿名或者无法解析的情况
            val targets = mutableListOf<PsiElement>()
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedLocalisations.search.d", name.escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxDefinitionManager.getRelatedLocalisations(sourceElement, preferredLocale))
                }
            }
            return getGotoData(expressionElement, targets)
        }
        run {
            // 修正（相关本地化）
            val sourceElement = file.findReferenceAt(offset)?.resolve() ?: return@run
            if (sourceElement !is ParadoxModifierLightElement) return@run
            val name = ParadoxPsiPresentationService.getNameForModifier(sourceElement) ?: return@run
            if (name.isEmpty()) return null // 3.0.4 排除匿名或者无法解析的情况
            val targets = mutableListOf<PsiElement>()
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedLocalisations.search.m", name.escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxModifierManager.getRelatedLocalisations(sourceElement, preferredLocale))
                }
            }
            return getGotoData(sourceElement, targets)
        }
        return null
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForLocalisationSnippet(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.chooseTitle.s", name.orUnresolved().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForScriptedVariable(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.chooseTitle.sv", name.orUnresolved().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.chooseTitle.d", name.orUnresolved().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForModifier(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.chooseTitle.m", name.orUnresolved().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedLocalisations.chooseTitle", name.orUnresolved().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForLocalisationSnippet(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.findUsagesTitle.s", name.orUnresolved().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForScriptedVariable(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.findUsagesTitle.sv", name.orUnresolved().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.findUsagesTitle.d", name.orUnresolved().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForModifier(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.findUsagesTitle.m", name.orUnresolved().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedLocalisations.findUsagesTitle", name.orUnresolved().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedLocalisations.notFoundMessage")
    }
}
