package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*
import java.util.*

//com.intellij.testIntegration.GotoTestOrCodeHandler

@Suppress("DialogTitleCapitalization")
class GotoRelatedLocalisationsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedLocalisations"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        if (element.isDefinitionRootKeyOrName()) {
            val definition = element.findParentDefinition() ?: return null
            val definitionInfo = definition.definitionInfo ?: return null
            val localisationInfos = definitionInfo.localisations
            if (localisationInfos.isEmpty()) return GotoData(definition, PsiElement.EMPTY_ARRAY, emptyList())
            val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
            val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
                for ((_, locationExpression) in localisationInfos) {
                    ProgressManager.checkCanceled()
                    //need read action here
                    runReadAction {
                        val selector = localisationSelector(project, definition).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                        val resolved = locationExpression.resolveAll(definition, definitionInfo, selector)
                        if (resolved != null && resolved.elements.isNotEmpty()) {
                            targets.addAll(resolved.elements)
                        }
                    }
                }
            }, PlsBundle.message("script.goto.relatedLocalisations.search.1", definitionInfo.name), true, project)
            if (!runResult) return null
            return GotoData(definition, targets.distinct().toTypedArray(), emptyList())
        }
        val modifierElement = ParadoxModifierManager.resolveModifier(element)
        if (modifierElement != null) {
            val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
            val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
                runReadAction {
                    run {
                        val keys = ParadoxModifierManager.getModifierNameKeys(modifierElement.name, modifierElement)
                        val result = keys.firstNotNullOfOrNull { key ->
                            val selector = localisationSelector(project, element).contextSensitive()
                                .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                                .withConstraint(ParadoxLocalisationConstraint.Modifier)
                            ParadoxLocalisationSearch.search(key, selector).findAll().orNull()
                        }
                        if (result != null) targets.addAll(result)
                    }
                    run {
                        val keys = ParadoxModifierManager.getModifierDescKeys(modifierElement.name, modifierElement)
                        val result = keys.firstNotNullOfOrNull { key ->
                            val selector = localisationSelector(project, element).contextSensitive()
                                .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                                .withConstraint(ParadoxLocalisationConstraint.Modifier)
                            ParadoxLocalisationSearch.search(key, selector).findAll().orNull()
                        }
                        if (result != null) targets.addAll(result)
                    }
                }
            }, PlsBundle.message("script.goto.relatedLocalisations.search.2", modifierElement.name), true, project)
            if (!runResult) return null
            return GotoData(element, targets.distinct().toTypedArray(), emptyList())
        }
        return null
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptStringExpressionElement? {
        return ParadoxPsiManager.findScriptExpression(file, offset).castOrNull()
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val definitionInfo = sourceElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
        if (definitionInfo != null) {
            val definitionName = definitionInfo.name.orAnonymous()
            return PlsBundle.message("script.goto.relatedLocalisations.chooseTitle.1", definitionName.escapeXml())
        }
        val modifierElement = sourceElement.castOrNull<ParadoxScriptStringExpressionElement>()?.let { ParadoxModifierManager.resolveModifier(it) }
        if (modifierElement != null) {
            val modifierName = modifierElement.name
            return PlsBundle.message("script.goto.relatedLocalisations.chooseTitle.2", modifierName.escapeXml())
        }
        val sourceName = sourceElement.text.unquote()
        return PlsBundle.message("script.goto.relatedLocalisations.chooseTitle.0", sourceName.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val definitionInfo = sourceElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
        if (definitionInfo != null) {
            val definitionName = definitionInfo.name.orAnonymous()
            return PlsBundle.message("script.goto.relatedLocalisations.findUsagesTitle.1", definitionName.escapeXml())
        }
        val modifierElement = sourceElement.castOrNull<ParadoxScriptStringExpressionElement>()?.let { ParadoxModifierManager.resolveModifier(it) }
        if (modifierElement != null) {
            val modifierName = modifierElement.name
            return PlsBundle.message("script.goto.relatedLocalisations.findUsagesTitle.2", modifierName.escapeXml())
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
