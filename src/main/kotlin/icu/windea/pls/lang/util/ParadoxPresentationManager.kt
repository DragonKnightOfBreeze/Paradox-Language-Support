package icu.windea.pls.lang.util

import com.intellij.diagram.DiagramElementManager.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.presentation.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.dataFlow.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import java.awt.*
import java.util.*
import javax.swing.*

object ParadoxPresentationManager {
    fun getPresentation(definition: ParadoxScriptDefinitionElement): JComponent? {
        val definitionInfo = definition.definitionInfo ?: return null
        return ParadoxDefinitionPresentationProvider.getPresentation(definition, definitionInfo)
    }

    fun getNameLocalisation(definition: ParadoxScriptDefinitionElement): ParadoxLocalisationProperty? {
        return ParadoxDefinitionManager.getPrimaryLocalisation(definition)
    }

    fun getNameLocalisationKey(definition: ParadoxScriptDefinitionElement): String? {
        return ParadoxDefinitionManager.getPrimaryLocalisationKey(definition)
    }

    fun getNameText(definition: ParadoxScriptDefinitionElement): String? {
        val localizedName = getNameLocalisation(definition)
        if (localizedName != null) return ParadoxLocalisationTextHtmlRenderer().render(localizedName)
        return null
    }

    fun getNameTextOrKey(definition: ParadoxScriptDefinitionElement): String? {
        val localizedName = getNameLocalisation(definition)
        if (localizedName != null) return ParadoxLocalisationTextHtmlRenderer().render(localizedName)
        val localizedNameKey = getNameLocalisationKey(definition)
        return localizedNameKey
    }

    fun getProperties(definition: ParadoxScriptDefinitionElement, keys: Collection<String>): TreeSet<ParadoxScriptProperty> {
        val properties = sortedSetOf<ParadoxScriptProperty>(compareBy { keys.indexOf(it.name.lowercase()) })
        definition.block?.properties()?.options(conditional = true, inline = true)?.forEach {
            if (it.name.lowercase() in keys) properties.add(it)
        }
        return properties
    }

    fun getPropertyText(property: ParadoxScriptProperty, detail: Boolean = false): SimpleColoredText {
        val rendered = ParadoxScriptTextRenderer(renderInBlock = detail).render(property)
        val propertyText = SimpleColoredText(rendered, DEFAULT_TEXT_ATTR)
        val propertyValue = property.propertyValue
        if (propertyValue is ParadoxScriptScriptedVariableReference) {
            val sv = propertyValue.text
            propertyText.append(" by $sv", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
        return propertyText
    }

    fun getText(localisation: ParadoxLocalisationProperty): String {
        return ParadoxLocalisationTextHtmlRenderer().render(localisation)
    }

    fun getText(localisationKey: String, project: Project, contextElement: PsiElement? = null): String? {
        val selector = selector(project, contextElement).localisation().contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        val localisation = ParadoxLocalisationSearch.search(localisationKey, selector).find() ?: return null
        return ParadoxLocalisationTextHtmlRenderer().render(localisation)
    }

    fun getLabel(text: String, color: Color? = null): JLabel {
        return ParadoxLocalisationTextUIRenderer(color).render(text)
    }

    fun getIcon(ddsFile: PsiFile): Icon? {
        val iconFile = ddsFile.virtualFile ?: return null
        val iconUrl = ParadoxImageManager.resolveUrlByFile(iconFile, ddsFile.project) ?: return null
        if (!ParadoxImageManager.canResolve(iconUrl)) return null
        return iconUrl.toFileUrl().toIconOrNull()
    }

    fun getIcon(definition: ParadoxScriptDefinitionElement): Icon? {
        val ddsFile = ParadoxDefinitionManager.getPrimaryImage(definition) ?: return null
        val iconFile = ddsFile.virtualFile ?: return null
        val iconUrl = ParadoxImageManager.resolveUrlByFile(iconFile, ddsFile.project) ?: return null
        if (!ParadoxImageManager.canResolve(iconUrl)) return null
        return iconUrl.toFileUrl().toIconOrNull()
    }

    fun getIcon(definition: ParadoxScriptDefinitionElement, ddsFile: PsiFile): Icon? {
        val iconFile = ddsFile.virtualFile ?: return null
        val frameInfo = definition.getUserData(PlsKeys.imageFrameInfo)
        val iconUrl = ParadoxImageManager.resolveUrlByFile(iconFile, ddsFile.project, frameInfo) ?: return null
        if (!ParadoxImageManager.canResolve(iconUrl)) return null
        return iconUrl.toFileUrl().toIconOrNull()
    }
}
