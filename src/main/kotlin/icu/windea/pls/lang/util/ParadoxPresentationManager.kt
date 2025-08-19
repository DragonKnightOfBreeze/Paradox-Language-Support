package icu.windea.pls.lang.util

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import java.awt.*
import javax.swing.*

object ParadoxPresentationManager {
    fun getNameText(definition: ParadoxScriptDefinitionElement): String? {
        val localizedName = ParadoxDefinitionManager.getPrimaryLocalisation(definition)
        if (localizedName != null) return ParadoxLocalisationTextHtmlRenderer().render(localizedName)
        val localizedNameKey = ParadoxDefinitionManager.getPrimaryLocalisationKey(definition)
        return localizedNameKey
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

    fun getLabel(text: Lazy<String>, color: Color? = null): JLabel {
        return ParadoxLocalisationTextUIRenderer(color).render(text::value)
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
