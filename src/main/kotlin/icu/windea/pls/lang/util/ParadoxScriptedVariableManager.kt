package icu.windea.pls.lang.util

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.orNull
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.resolve.ParadoxScriptedVariableService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object ParadoxScriptedVariableManager {
    fun isGlobalScriptedVariablesFile(file: VirtualFile): Boolean {
        if (file.fileType != ParadoxScriptFileType) return false
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

    fun getPresentableName(element: ParadoxScriptScriptedVariable, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): String? {
        val name = element.name?.orNull() ?: return null
        val nameLocalisation = getNameLocalisation(name, element, locale)
        return nameLocalisation?.let { ParadoxLocalisationManager.getLocalizedText(it) }
    }

    fun getPresentableNames(element: ParadoxScriptScriptedVariable, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): Set<String> {
        val name = element.name?.orNull() ?: return emptySet()
        val nameLocalisation = getNameLocalisations(name, element, locale)
        return nameLocalisation.mapNotNull { ParadoxLocalisationManager.getLocalizedText(it) }.toSet()
    }

    fun getNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): ParadoxLocalisationProperty? {
        return ParadoxScriptedVariableService.resolveNameLocalisation(name, contextElement, locale)
    }

    fun getNameLocalisations(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): List<ParadoxLocalisationProperty> {
        return ParadoxScriptedVariableService.resolveNameLocalisations(name, contextElement, locale)
    }
}
