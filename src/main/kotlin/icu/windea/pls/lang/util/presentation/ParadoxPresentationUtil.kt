package icu.windea.pls.lang.util.presentation

import com.intellij.diagram.DiagramElementManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.ColorUtil
import com.intellij.ui.SimpleColoredText
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.ui.JBUI
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.core.toIconOrNull
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextHtmlRenderer
import icu.windea.pls.lang.util.renderers.ParadoxScriptTextPlainRenderer
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import java.awt.Color
import java.util.*
import javax.swing.Icon
import javax.swing.JLabel

object ParadoxPresentationUtil {
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
        definition.block?.properties(conditional = true, inline = true)?.forEach {
            if (it.name.lowercase() in keys) properties.add(it)
        }
        return properties
    }

    fun getPropertyText(property: ParadoxScriptProperty, detail: Boolean = false): SimpleColoredText {
        val renderer = ParadoxScriptTextPlainRenderer().apply { renderInBlock = detail }
        val rendered = renderer.render(property)
        val propertyText = SimpleColoredText(rendered, DiagramElementManager.DEFAULT_TEXT_ATTR)
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
        val localisation = ParadoxLocalisationSearch.searchNormal(localisationKey, selector).find() ?: return null
        return ParadoxLocalisationTextHtmlRenderer().render(localisation)
    }

    fun getLabel(text: String, color: Color? = null): JLabel {
        val label = JLabel()
        label.text = getLabelHtml(text, color)
        label.border = JBUI.Borders.empty()
        label.size = label.preferredSize
        label.isOpaque = false
        return label
    }

    fun getLabelHtml(text: String, color: Color? = null): String {
        // com.intellij.openapi.actionSystem.impl.ActionToolbarImpl.paintToImage
        return buildString {
            append("<html>")
            append("<span")
            if (color != null) append(" style='color: #").append(ColorUtil.toHex(color, true)).append("'")
            append(">")
            append(text)
            append("</span>")
            append("</html>")
        }
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
        val frameInfo = definition.getUserData(ParadoxDefinitionManager.Keys.imageFrameInfo)
        val iconUrl = ParadoxImageManager.resolveUrlByFile(iconFile, ddsFile.project, frameInfo) ?: return null
        if (!ParadoxImageManager.canResolve(iconUrl)) return null
        return iconUrl.toFileUrl().toIconOrNull()
    }
}
