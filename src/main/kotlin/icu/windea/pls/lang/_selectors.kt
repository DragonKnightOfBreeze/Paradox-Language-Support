package icu.windea.pls.lang

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.LightVirtualFileBase
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.configGroup.localisationLocalesById
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.psi.mock.CwtConfigMockPsiElement
import icu.windea.pls.lang.psi.mock.ParadoxMockPsiElement
import icu.windea.pls.lang.psi.stubs.ParadoxLocaleAwareStub
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.lang.util.ParadoxCoreManager
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.indexInfo.CwtConfigIndexInfo
import icu.windea.pls.model.indexInfo.ParadoxIndexInfo

tailrec fun selectRootFile(from: Any?): VirtualFile? {
    if (from == null) return null
    return when {
        from is VirtualFileWindow -> selectRootFile(from.delegate) // for injected PSI
        from is LightVirtualFileBase && from.originalFile != null -> selectRootFile(from.originalFile)
        from is VirtualFile -> from.fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.MetadataBased>()?.rootFile
        else -> selectRootFile(selectFile(from))
    }
}

tailrec fun selectFile(from: Any?): VirtualFile? {
    if (from == null) return null
    return when {
        from is ParadoxIndexInfo -> selectFile(from.virtualFile)
        from is VirtualFileWindow -> from.castOrNull() // for injected PSI (result is from, not from.delegate)
        from is LightVirtualFileBase && from.originalFile != null -> selectFile(from.originalFile)
        from is VirtualFile -> from
        from is PsiDirectory -> selectFile(from.virtualFile)
        from is PsiFile -> selectFile(from.originalFile.virtualFile)
        from is PsiElement -> selectFile(runReadAction { from.containingFile })
        else -> null
    }
}

tailrec fun selectGameType(from: Any?): ParadoxGameType? {
    if (from == null) return null
    if (from is ParadoxGameType) return from
    if (from is VirtualFile) ParadoxFileManager.getInjectedGameTypeForTestDataFile(from)
    if (from is UserDataHolder) from.getUserData(PlsKeys.injectedGameType)?.let { return it }
    return when {
        from is ParadoxIndexInfo -> from.gameType
        from is CwtConfigIndexInfo -> from.gameType
        from is VirtualFileWindow -> selectGameType(from.delegate) // for injected PSI
        from is LightVirtualFileBase && from.originalFile != null -> selectGameType(from.originalFile)
        from is VirtualFile -> from.fileInfo?.rootInfo?.gameType
        from is PsiDirectory -> selectGameType(selectFile(from))
        from is PsiFile -> selectGameType(selectFile(from))
        from is ParadoxStub<*> -> from.gameType
        from is CwtConfigMockPsiElement -> from.gameType
        from is ParadoxMockPsiElement -> from.gameType
        from is StubBasedPsiElementBase<*> -> selectGameType(runReadAction { getStubToSelectGameType(from) ?: from.containingFile })
        from is PsiElement -> selectGameType(runReadAction { from.parent })
        else -> null
    }
}

private fun getStubToSelectGameType(from: StubBasedPsiElementBase<*>): ParadoxStub<*>? {
    return from.greenStub?.castOrNull<ParadoxStub<*>>()
}

tailrec fun selectLocale(from: Any?): CwtLocaleConfig? {
    if (from == null) return null
    if (from is CwtLocaleConfig) return from
    if (from is UserDataHolder) from.getUserData(PlsKeys.injectedLocaleConfig)?.let { return it }
    return when {
        from is PsiDirectory -> ParadoxLocaleManager.getPreferredLocaleConfig()
        from is PsiFile -> ParadoxCoreManager.getLocaleConfig(from.virtualFile ?: return null, from.project)
        from is ParadoxLocaleAwareStub<*> -> toLocale(from.locale, from.containingFileStub?.psi)
        from is ParadoxLocalisationLocale -> toLocale(from.name, from)
        from is StubBasedPsiElementBase<*> -> selectLocale(runReadAction { getStubToSelectLocale(from) ?: from.parent })
        from is PsiElement && from.language is ParadoxLocalisationLanguage -> selectLocale(runReadAction { from.parent })
        else -> ParadoxLocaleManager.getPreferredLocaleConfig()
    }
}

private fun getStubToSelectLocale(from: StubBasedPsiElementBase<*>): ParadoxLocaleAwareStub<*>? {
    return from.greenStub?.castOrNull<ParadoxLocaleAwareStub<*>>()
}

private fun toLocale(localeId: String?, from: PsiElement?): CwtLocaleConfig? {
    if (localeId == null || from == null) return null
    return PlsFacade.getConfigGroup(from.project).localisationLocalesById.get(localeId)
}
