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
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.index.constraints.ParadoxLocalisationIndexConstraint
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName
import icu.windea.pls.lang.psi.light.ParadoxLocalisationSnippetLightElement
import icu.windea.pls.lang.resolve.ParadoxLocationExpressionService
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.search.util.withConstraint
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.lang.util.ParadoxSnippetManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

// com.intellij.testIntegration.GotoTestOrCodeHandler

class GotoRelatedLocalisationsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedLocalisations"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        // 本地化引用片段（相关本地化）
        run {
            val resolved = file.findReferenceAt(offset)?.resolve() ?: return@run
            if (resolved !is ParadoxLocalisationSnippetLightElement) return@run
            val targets = mutableListOf<PsiElement>()
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedLocalisations.search.s", resolved.name.escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxSnippetManager.getRelatedLocalisations(resolved))
                }
            }
            return GotoData(resolved.parent, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
        }
        val element = findElement(file, offset) ?: return null
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        when {
            ParadoxPsiMatchService.isScriptedVariable(element) -> {
                val scriptedVariable = element
                val name = ParadoxPsiPresentationService.getNameForScriptedVariable(scriptedVariable) ?: return null
                val targets = mutableListOf<PsiElement>()
                runWithModalProgressBlocking<Unit>(project, ChronicleBundle.message("script.goto.relatedLocalisations.search.sv", name)) {
                    // need read actions here if necessary
                    readAction {
                        val result = ParadoxScriptedVariableManager.getNameLocalisations(name, element, preferredLocale)
                        targets.addAll(result)
                    }
                }
                return GotoData(element, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
            }
            element !is ParadoxScriptStringExpressionElement -> return null
            element.isDefinitionTypeKeyOrName() -> {
                val definition = selectScope { element.parentDefinition() } ?: return null
                val definitionInfo = definition.definitionInfo ?: return null
                if (definitionInfo.name.isEmpty()) return null // 排除匿名定义
                val name = ParadoxPsiPresentationService.getNameForDefinition(definition) ?: return null
                val localisationInfos = definitionInfo.localisations
                if (localisationInfos.isEmpty()) return GotoData(definition, PsiElement.EMPTY_ARRAY, emptyList())
                val targets = mutableListOf<PsiElement>()
                runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedLocalisations.search.d", name)) {
                    // need read actions here if necessary
                    for ((_, locationExpression) in localisationInfos) {
                        ProgressManager.checkCanceled()
                        readAction {
                            val resolveResult = ParadoxLocationExpressionService.resolve(locationExpression, definition, definitionInfo) { preferLocale(preferredLocale) }
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
                val targets = mutableListOf<PsiElement>()
                runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedLocalisations.search.m", modifierElement.name)) {
                    // need read actions here if necessary
                    readAction {
                        val keys = ParadoxModifierManager.getModifierNameKeys(modifierElement.name, modifierElement)
                        val result = keys.firstNotNullOfOrNull { key ->
                            val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive()
                                .preferLocale(preferredLocale)
                                .withConstraint(ParadoxLocalisationIndexConstraint.Modifier) // so ignore case
                            ParadoxLocalisationSearch.searchNormal(key, selector).findAll().orNull()
                        }
                        if (result != null) targets.addAll(result)
                    }
                    readAction {
                        val keys = ParadoxModifierManager.getModifierDescKeys(modifierElement.name, modifierElement)
                        val result = keys.firstNotNullOfOrNull { key ->
                            val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive()
                                .preferLocale(preferredLocale)
                                .withConstraint(ParadoxLocalisationIndexConstraint.Modifier) // so ignore case
                            ParadoxLocalisationSearch.searchNormal(key, selector).findAll().orNull()
                        }
                        if (result != null) targets.addAll(result)
                    }
                }
                return GotoData(element, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
            }
        }
    }

    private fun findElement(file: PsiFile, offset: Int): PsiElement? {
        return ParadoxPsiFileService.findScriptedVariable(file, offset) { BY_NAME }
            ?: ParadoxPsiFileService.findScriptExpression(file, offset).castOrNull()
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        run {
            when {
                ParadoxPsiMatchService.isScriptedVariable(sourceElement) -> {
                    val name = ParadoxPsiPresentationService.getNameForScriptedVariable(sourceElement) ?: return@run
                    return ChronicleBundle.message("script.goto.relatedLocalisations.chooseTitle.sv", name.escapeXml())
                }
                sourceElement !is ParadoxScriptStringExpressionElement -> {}
                sourceElement.isDefinitionTypeKeyOrName() -> {
                    val definitionName = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: return@run
                    return ChronicleBundle.message("script.goto.relatedLocalisations.chooseTitle.d", definitionName.escapeXml())
                }
                else -> {
                    val modifierElement = sourceElement.castOrNull<ParadoxScriptStringExpressionElement>()
                        ?.let { ParadoxModifierManager.resolveModifier(it) } ?: return@run
                    val modifierName = modifierElement.name
                    return ChronicleBundle.message("script.goto.relatedLocalisations.chooseTitle.m", modifierName.escapeXml())
                }
            }
        }
        val sourceName = sourceElement.text.unquote()
        return ChronicleBundle.message("script.goto.relatedLocalisations.chooseTitle", sourceName.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        run {
            when {
                ParadoxPsiMatchService.isScriptedVariable(sourceElement) -> {
                    val name = ParadoxPsiPresentationService.getNameForScriptedVariable(sourceElement) ?: return@run
                    return ChronicleBundle.message("script.goto.relatedLocalisations.findUsagesTitle.sv", name.escapeXml())
                }
                sourceElement !is ParadoxScriptStringExpressionElement -> {}
                sourceElement.isDefinitionTypeKeyOrName() -> {
                    val definitionName = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: return@run
                    return ChronicleBundle.message("script.goto.relatedLocalisations.findUsagesTitle.d", definitionName.escapeXml())
                }
                else -> {
                    val modifierElement = sourceElement.castOrNull<ParadoxScriptStringExpressionElement>()
                        ?.let { ParadoxModifierManager.resolveModifier(it) } ?: return@run
                    val modifierName = modifierElement.name
                    return ChronicleBundle.message("script.goto.relatedLocalisations.findUsagesTitle.m", modifierName.escapeXml())
                }
            }
        }
        val sourceName = sourceElement.text.unquote()
        return ChronicleBundle.message("script.goto.relatedLocalisations.findUsagesTitle", sourceName.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedLocalisations.notFoundMessage")
    }
}
