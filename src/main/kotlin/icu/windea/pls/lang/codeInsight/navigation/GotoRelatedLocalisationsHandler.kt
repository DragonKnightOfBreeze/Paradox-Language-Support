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
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.orNull
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withConstraint
import icu.windea.pls.lang.util.CwtLocationExpressionManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.lang.util.psi.ParadoxPsiMatcher
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.findParentDefinition
import icu.windea.pls.script.psi.isDefinitionRootKeyOrName
import java.util.*

//com.intellij.testIntegration.GotoTestOrCodeHandler

class GotoRelatedLocalisationsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedLocalisations"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        when {
            ParadoxPsiMatcher.isScriptedVariable(element) -> {
                val scriptedVariable = element
                val name = scriptedVariable.name?.orNull() ?: return null
                val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
                val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
                    // need read actions here if necessary
                    runReadAction {
                        val result = ParadoxScriptedVariableManager.getNameLocalisations(name, element, preferredLocale)
                        targets.addAll(result)
                    }
                }, PlsBundle.message("script.goto.relatedLocalisations.search.3", name), true, project)
                if (!runResult) return null
                return GotoData(element, targets.distinct().toTypedArray(), emptyList())
            }
            element !is ParadoxScriptStringExpressionElement -> return null
            element.isDefinitionRootKeyOrName() -> {
                val definition = element.findParentDefinition() ?: return null
                val definitionInfo = definition.definitionInfo ?: return null
                val localisationInfos = definitionInfo.localisations
                if (localisationInfos.isEmpty()) return GotoData(definition, PsiElement.EMPTY_ARRAY, emptyList())
                val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
                val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
                    // need read actions here if necessary
                    for ((_, locationExpression) in localisationInfos) {
                        ProgressManager.checkCanceled()
                        runReadAction {
                            val resolveResult = CwtLocationExpressionManager.resolve(locationExpression, definition, definitionInfo) { preferLocale(preferredLocale) }
                            if (resolveResult != null && resolveResult.elements.isNotEmpty()) {
                                targets.addAll(resolveResult.elements)
                            }
                        }
                    }
                }, PlsBundle.message("script.goto.relatedLocalisations.search.1", definitionInfo.name), true, project)
                if (!runResult) return null
                return GotoData(definition, targets.distinct().toTypedArray(), emptyList())
            }
            else -> {
                val modifierElement = ParadoxModifierManager.resolveModifier(element) ?: return null
                val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
                val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
                    // need read actions here if necessary
                    runReadAction {
                        val keys = ParadoxModifierManager.getModifierNameKeys(modifierElement.name, modifierElement)
                        val result = keys.firstNotNullOfOrNull { key ->
                            val selector = selector(project, element).localisation().contextSensitive()
                                .preferLocale(preferredLocale)
                                .withConstraint(ParadoxIndexConstraint.Localisation.Modifier)
                            ParadoxLocalisationSearch.search(key, selector).findAll().orNull()
                        }
                        if (result != null) targets.addAll(result)
                    }
                    runReadAction {
                        val keys = ParadoxModifierManager.getModifierDescKeys(modifierElement.name, modifierElement)
                        val result = keys.firstNotNullOfOrNull { key ->
                            val selector = selector(project, element).localisation().contextSensitive()
                                .preferLocale(preferredLocale)
                                .withConstraint(ParadoxIndexConstraint.Localisation.Modifier)
                            ParadoxLocalisationSearch.search(key, selector).findAll().orNull()
                        }
                        if (result != null) targets.addAll(result)
                    }
                }, PlsBundle.message("script.goto.relatedLocalisations.search.2", modifierElement.name), true, project)
                if (!runResult) return null
                return GotoData(element, targets.distinct().toTypedArray(), emptyList())
            }
        }
    }

    private fun findElement(file: PsiFile, offset: Int): PsiElement? {
        return ParadoxPsiFinder.findScriptedVariable(file, offset) { BY_NAME }
            ?: ParadoxPsiFinder.findScriptExpression(file, offset).castOrNull()
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        run {
            when {
                ParadoxPsiMatcher.isScriptedVariable(sourceElement) -> {
                    val name = sourceElement.name?.orNull() ?: return@run
                    return PlsBundle.message("script.goto.relatedLocalisations.chooseTitle.3", name.escapeXml())
                }
                sourceElement !is ParadoxScriptStringExpressionElement -> {}
                sourceElement.isDefinitionRootKeyOrName() -> {
                    val definitionInfo = sourceElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo ?: return@run
                    val definitionName = definitionInfo.name.or.anonymous()
                    return PlsBundle.message("script.goto.relatedLocalisations.chooseTitle.1", definitionName.escapeXml())
                }
                else -> {
                    val modifierElement = sourceElement.castOrNull<ParadoxScriptStringExpressionElement>()
                        ?.let { ParadoxModifierManager.resolveModifier(it) } ?: return@run
                    val modifierName = modifierElement.name
                    return PlsBundle.message("script.goto.relatedLocalisations.chooseTitle.2", modifierName.escapeXml())
                }
            }
        }
        val sourceName = sourceElement.text.unquote()
        return PlsBundle.message("script.goto.relatedLocalisations.chooseTitle.0", sourceName.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        run {
            when {
                ParadoxPsiMatcher.isScriptedVariable(sourceElement) -> {
                    val name = sourceElement.name?.orNull() ?: return@run
                    return PlsBundle.message("script.goto.relatedLocalisations.findUsagesTitle.3", name.escapeXml())
                }
                sourceElement !is ParadoxScriptStringExpressionElement -> {}
                sourceElement.isDefinitionRootKeyOrName() -> {
                    val definitionInfo = sourceElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo ?: return@run
                    val definitionName = definitionInfo.name.or.anonymous()
                    return PlsBundle.message("script.goto.relatedLocalisations.findUsagesTitle.1", definitionName.escapeXml())
                }
                else -> {
                    val modifierElement = sourceElement.castOrNull<ParadoxScriptStringExpressionElement>()
                        ?.let { ParadoxModifierManager.resolveModifier(it) } ?: return@run
                    val modifierName = modifierElement.name
                    return PlsBundle.message("script.goto.relatedLocalisations.findUsagesTitle.2", modifierName.escapeXml())
                }
            }
        }
        val sourceName = sourceElement.text.unquote()
        return PlsBundle.message("script.goto.relatedLocalisations.findUsagesTitle.0", sourceName.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return PlsBundle.message("script.goto.relatedLocalisations.notFoundMessage")
    }

    override fun navigateToElement(descriptor: Navigatable) {
        if (descriptor is PsiElement) {
            activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}
