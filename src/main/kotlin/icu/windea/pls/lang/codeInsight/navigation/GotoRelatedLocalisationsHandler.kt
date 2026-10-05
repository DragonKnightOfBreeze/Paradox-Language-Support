package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.GotoTargetHandler
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.lang.definitionInfo
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

// com.intellij.testIntegration.GotoTestOrCodeHandler

class GotoRelatedLocalisationsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedLocalisations"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        val targets = mutableListOf<PsiElement>()
        var sourceElement: PsiElement? = null
        run {
            // 本地化引用片段（相关本地化）
            if (sourceElement != null) return@run
            val element = file.findReferenceAt(offset)?.resolve() ?: return@run
            if (element !is ParadoxLocalisationSnippetLightElement) return@run
            sourceElement = element
            val name = ParadoxPsiPresentationService.getNameForLocalisationSnippet(element) ?: return@run
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedLocalisations.search.s", name.orAnonymous().escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxSnippetManager.getRelatedLocalisations(element))
                }
            }
        }
        run {
            // 封装变量（相关本地化）
            if (sourceElement != null) return@run
            val element = ParadoxPsiFileService.findScriptedVariable(file, offset) { BY_NAME } ?: return@run
            if (!ParadoxPsiMatchService.isScriptedVariable(element)) return@run
            sourceElement = element
            val name = ParadoxPsiPresentationService.getNameForScriptedVariable(element) ?: return@run
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedLocalisations.search.sv", name.orAnonymous().escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxScriptedVariableManager.getNameLocalisations(name, element, preferredLocale))
                }
            }
        }
        run {
            // 定义（相关本地化）
            if (sourceElement != null) return@run
            val element = ParadoxPsiFileService.findScriptExpression(file, offset) ?: return@run
            if (!element.isDefinitionTypeKeyOrName()) return@run
            val definition = selectScope { element.parentDefinition() } ?: return@run
            val definitionInfo = definition.definitionInfo ?: return@run
            if (definitionInfo.name.isEmpty()) return@run // 排除匿名定义
            sourceElement = definition
            val name = ParadoxPsiPresentationService.getNameForDefinition(definition) ?: return@run
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedLocalisations.search.d", name.orAnonymous().escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxDefinitionManager.getRelatedLocalisations(definition))
                }
            }
        }
        run {
            // 修正（相关本地化）
            if (sourceElement != null) return@run
            val element = file.findReferenceAt(offset)?.resolve() ?: return@run
            if (element !is ParadoxModifierLightElement) return@run
            sourceElement = element
            val name = ParadoxPsiPresentationService.getNameForModifier(element) ?: return@run
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedLocalisations.search.m", name.orAnonymous().escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxModifierManager.getRelatedLocalisations(element))
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
            val name = ParadoxPsiPresentationService.getNameForLocalisationSnippet(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.chooseTitle.s", name.orAnonymous().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForScriptedVariable(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.chooseTitle.sv", name.orAnonymous().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.chooseTitle.d", name.orAnonymous().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForModifier(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.chooseTitle.m", name.orAnonymous().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedLocalisations.chooseTitle", name.orAnonymous().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForLocalisationSnippet(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.findUsagesTitle.s", name.orAnonymous().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForScriptedVariable(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.findUsagesTitle.sv", name.orAnonymous().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.findUsagesTitle.d", name.orAnonymous().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForModifier(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedLocalisations.findUsagesTitle.m", name.orAnonymous().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedLocalisations.findUsagesTitle", name.orAnonymous().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedLocalisations.notFoundMessage")
    }
}
