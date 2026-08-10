package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.cache.cancelable
import icu.windea.pls.core.cache.createNestedCache
import icu.windea.pls.core.createCachedValue
import icu.windea.pls.core.util.*
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxModificationTrackers

object ParadoxMatchResultContext {
    object Keys : KeyRegistry() {
        val cacheForDefinitions by registerKeyForCache(ParadoxModificationTrackers.ScriptFile)
        val cacheForLocalisations by registerKeyForCache(ParadoxModificationTrackers.LocalisationFile, ParadoxModificationTrackers.PreferredLocale)
        val cacheForSyncedLocalisations by registerKeyForCache(ParadoxModificationTrackers.LocalisationFile, ParadoxModificationTrackers.PreferredLocale)
        val cacheForPathReferences by registerKeyForCache(ParadoxModificationTrackers.FilePath)
        val cacheForComplexEnumValues by registerKeyForCache(ParadoxModificationTrackers.ScriptFile)
        val cacheForModifiers by registerKeyForCache(ParadoxModificationTrackers.ScriptFile)
        val cacheForTemplates by registerKeyForCache(ParadoxModificationTrackers.ScriptFile, ParadoxModificationTrackers.LocalisationFile, ParadoxModificationTrackers.PreferredLocale)
    }

    fun registerKeyForCache(vararg dependencies: Any): ParadoxMatchResultNestedCacheKeyProvider {
        return registerKeyWithThis(Keys) {
            // rootFile -> cacheKey -> configMatchResult
            createCachedValue(project) {
                createNestedCache<VirtualFile, _, _> {
                    CacheBuilder().build<String, ParadoxMatchResult>().cancelable()
                }.withDependencyItems(*dependencies)
            }
        }
    }

    fun getFromCache(element: PsiElement, project: Project, key: ParadoxMatchResultNestedCacheKey, cacheKey: String, matchResultProvider: (String) -> ParadoxMatchResult): ParadoxMatchResult {
        ProgressManager.checkCanceled() // check cancellation before access root-file-level cache
        val rootFile = selectRootFile(element) ?: return ParadoxMatchResult.NotMatch
        val configGroup = ChronicleFacade.getConfigGroup(project, selectGameType(rootFile))
        val cache = configGroup.getOrPutUserData(key).value.get(rootFile)
        return cache.get(cacheKey, matchResultProvider)
    }
}
