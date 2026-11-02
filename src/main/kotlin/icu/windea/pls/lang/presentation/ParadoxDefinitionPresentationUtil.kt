package icu.windea.pls.lang.presentation

import com.intellij.diagram.DiagramElementManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.ColorUtil
import com.intellij.ui.SimpleColoredText
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.ui.JBUI
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.resolve.ParadoxImageResolveResult
import icu.windea.pls.lang.resolve.ParadoxImageService
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextHtmlRenderer
import icu.windea.pls.lang.util.renderers.ParadoxScriptTextPlainRenderer
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import java.awt.Color
import java.util.*
import javax.swing.JLabel

// TODO 3.0.x refactor

object ParadoxDefinitionPresentationUtil {
    fun getNameLocalisation(definition: ParadoxDefinitionElement): ParadoxLocalisationProperty? {
        return ParadoxDefinitionManager.getRelatedLocalisations(definition, onlyOne = true, onlyPrimary = true).firstOrNull()
    }

    fun getNameLocalisationKey(definition: ParadoxDefinitionElement): String? {
        return ParadoxDefinitionManager.getPrimaryLocalisationKey(definition)
    }

    fun getNameText(definition: ParadoxDefinitionElement): String? {
        val presentableName = getNameLocalisation(definition)
        if (presentableName != null) return ParadoxLocalisationTextHtmlRenderer().render(presentableName)
        return null
    }

    fun getNameTextOrKey(definition: ParadoxDefinitionElement): String? {
        val presentableName = getNameLocalisation(definition)
        if (presentableName != null) return ParadoxLocalisationTextHtmlRenderer().render(presentableName)
        val presentableNameKey = getNameLocalisationKey(definition)
        return presentableNameKey
    }

    fun getProperties(definition: ParadoxDefinitionElement, keys: Collection<String>): TreeSet<ParadoxScriptProperty> {
        val properties = sortedSetOf<ParadoxScriptProperty>(compareBy { keys.indexOf(it.name.lowercase()) })
        definition.block?.properties(conditional = true, inline = true)?.forEach {
            if (it.name.lowercase() in keys) properties.add(it)
        }
        return properties
    }

    fun getPropertyText(property: ParadoxScriptProperty, detail: Boolean = false): SimpleColoredText {
        val renderer = ParadoxScriptTextPlainRenderer().apply { settings.renderInBlock = detail }
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
        val selector = ParadoxLocalisationSearch.selector(project, contextElement).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
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

    @Suppress("unused")
    fun resolveIcon(ddsFile: PsiFile): ParadoxImageResolveResult? {
        val iconFile = ddsFile.virtualFile ?: return null
        val imageResolveResult = ParadoxImageService.resolveByFile(iconFile) ?: return null
        return imageResolveResult
    }

    fun resolveIcon(definition: ParadoxDefinitionElement): ParadoxImageResolveResult? {
        val ddsFile = ParadoxDefinitionManager.getRelatedImages(definition, onlyOne = true, onlyPrimary = true).firstOrNull() ?: return null
        val iconFile = ddsFile.virtualFile ?: return null
        val imageResolveResult = ParadoxImageService.resolveByFile(iconFile) ?: return null
        return imageResolveResult
    }

    @Suppress("unused")
    fun resolveIcon(definition: ParadoxDefinitionElement, ddsFile: PsiFile): ParadoxImageResolveResult? {
        val iconFile = ddsFile.virtualFile ?: return null
        val frameInfo = definition.getUserData(ParadoxDefinitionManager.Keys.imageFrameInfo)
        val imageResolveResult = ParadoxImageService.resolveByFile(iconFile, frameInfo) ?: return null
        return imageResolveResult
    }
}
