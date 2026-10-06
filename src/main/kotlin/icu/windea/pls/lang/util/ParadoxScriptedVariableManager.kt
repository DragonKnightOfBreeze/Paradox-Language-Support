package icu.windea.pls.lang.util

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

@Optimized
object ParadoxScriptedVariableManager {
    fun isGlobalScriptedVariablesFile(file: VirtualFile): Boolean {
        if (file.fileType !== ParadoxScriptFileType) return false
        val filePath = file.fileInfo?.path ?: return false
        return isGlobalScriptedVariablesFilePath(filePath)
    }

    fun isGlobalScriptedVariablesFile(file: PsiFile): Boolean {
        if (file !is ParadoxScriptFile) return false
        val filePath = file.fileInfo?.path ?: return false
        return isGlobalScriptedVariablesFilePath(filePath)
    }

    fun isGlobalScriptedVariablesFilePath(filePath: ParadoxPath): Boolean {
        return ParadoxPathConstraint.ForScriptedVariable.test(filePath)
    }

    fun getGlobalScriptedVariablesDirectory(contextFile: VirtualFile): VirtualFile? {
        val fileInfo = contextFile.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val entryPath = fileInfo.entryPath ?: return null
        val path = entryPath.resolve("common/scripted_variables")
        return VirtualFileService.findDirectory(path)
    }

    // region Related Items

    fun getRelatedLocalisations(
        element: ParadoxScriptScriptedVariable,
        preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig(),
        onlyOne: Boolean = false,
    ): List<ParadoxLocalisationProperty> {
        return getRelatedLocalisations(element.name, element, preferredLocale, onlyOne)
    }

    fun getRelatedLocalisations(
        name: String?,
        contextElement: PsiElement,
        preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig(),
        onlyOne: Boolean = false,
    ): List<ParadoxLocalisationProperty> {
        return ParadoxLocalisationManager.getRelatedLocalisationsFrom(name, contextElement, preferredLocale, onlyOne)
    }

    // endregion

    // region Presentable Items

    fun getPresentableNames(
        element: ParadoxScriptScriptedVariable,
        preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig(),
        onlyOne: Boolean = false,
    ): List<String> {
        val localisations = getRelatedLocalisations(element, preferredLocale, onlyOne)
        return ParadoxLocalisationManager.getPresentableText(localisations)
    }

    @Suppress("unused")
    fun getPresentableNames(
        name: String,
        contextElement: PsiElement,
        preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig(),
        onlyOne: Boolean = false,
    ): List<String> {
        val localisation = ParadoxComplexEnumValueManager.getRelatedLocalisations(name, contextElement, preferredLocale, onlyOne)
        return ParadoxLocalisationManager.getPresentableText(localisation)
    }

    // endregion
}
