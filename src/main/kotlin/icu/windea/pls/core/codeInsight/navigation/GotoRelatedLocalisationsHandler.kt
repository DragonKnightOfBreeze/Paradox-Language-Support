package icu.windea.pls.core.codeInsight.navigation

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
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.lang.*
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
        if(element.isDefinitionRootKeyOrName()) {
            val definition = element.findParentDefinition() ?: return null
            val definitionInfo = definition.definitionInfo ?: return null
            val localisationInfos = definitionInfo.localisations
            if(localisationInfos.isEmpty()) return GotoData(definition, PsiElement.EMPTY_ARRAY, emptyList())
            val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
            val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
                //need read action here
                runReadAction {
                    for((_, locationExpression) in localisationInfos) {
                        ProgressManager.checkCanceled()
                        val selector = localisationSelector(project, definition).contextSensitive().preferLocale(preferredParadoxLocale())
                        val resolved = locationExpression.resolveAll(definitionInfo.name, definition, selector) ?: continue
                        if(resolved.localisations.isNotEmpty()) {
                            targets.addAll(resolved.localisations)
                        }
                    }
                }
            }, PlsBundle.message("script.goto.relatedLocalisations.search.1", definitionInfo.name), true, project)
            if(!runResult) return null
            return GotoData(definition, targets.toTypedArray(), emptyList())
        }
        val modifierElement = ParadoxModifierHandler.resolveModifier(element)
        if(modifierElement != null) {
            val gameType = modifierElement.gameType
            val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
            val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
                runReadAction {
                    val nameKeys = ParadoxModifierHandler.getModifierNameKeys(modifierElement.name)
                    val localisations = nameKeys.firstNotNullOfOrNull {
                        val selector = localisationSelector(project, element).contextSensitive().preferLocale(preferredParadoxLocale())
                        val result = ParadoxLocalisationSearch.search(it, selector).findAll()
                        result.takeIfNotEmpty()
                    }
                    if(localisations != null) targets.addAll(localisations)
                    val descKeys = ParadoxModifierHandler.getModifierDescKeys(modifierElement.name)
                    val descLocalisations = descKeys.firstNotNullOfOrNull {
                        val selector = localisationSelector(project, element).contextSensitive().preferLocale(preferredParadoxLocale())
                        val result = ParadoxLocalisationSearch.search(it, selector).findAll()
                        result.takeIfNotEmpty()
                    }
                    if(descLocalisations != null) targets.addAll(descLocalisations)
                }
            }, PlsBundle.message("script.goto.relatedLocalisations.search.2", modifierElement.name), true, project)
            if(!runResult) return null
            return GotoData(element, targets.toTypedArray(), emptyList())
        }
        return null
    }
    
    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptStringExpressionElement? {
        //direct parent
        return file.findElementAt(offset) {
            it.parent as? ParadoxScriptStringExpressionElement
        }?.takeIf { it.isExpression() }
    }
    
    override fun shouldSortTargets(): Boolean {
        return false
    }
    
    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val definitionInfo = sourceElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
        if(definitionInfo != null) {
            val definitionName = definitionInfo.name.orAnonymous()
            return PlsBundle.message("script.goto.relatedLocalisations.chooseTitle.1", definitionName.escapeXml())
        }
        val modifierElement = sourceElement.castOrNull<ParadoxScriptStringExpressionElement>()?.let { ParadoxModifierHandler.resolveModifier(it) }
        if(modifierElement != null) {
            val modifierName = modifierElement.name
            return PlsBundle.message("script.goto.relatedLocalisations.chooseTitle.2", modifierName.escapeXml())
        }
        val sourceName = sourceElement.text.unquote()
        return PlsBundle.message("script.goto.relatedLocalisations.chooseTitle.0", sourceName.escapeXml())
    }
    
    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val definitionInfo = sourceElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
        if(definitionInfo != null) {
            val definitionName = definitionInfo.name.orAnonymous()
            return PlsBundle.message("script.goto.relatedLocalisations.findUsagesTitle.1", definitionName.escapeXml())
        }
        val modifierElement = sourceElement.castOrNull<ParadoxScriptStringExpressionElement>()?.let { ParadoxModifierHandler.resolveModifier(it) }
        if(modifierElement != null) {
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
        if(descriptor is PsiElement) {
            NavigationUtil.activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}