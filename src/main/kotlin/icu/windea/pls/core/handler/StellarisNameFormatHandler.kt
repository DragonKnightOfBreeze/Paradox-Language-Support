package icu.windea.pls.core.handler

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.model.*

@WithGameType(ParadoxGameType.Stellaris)
object StellarisNameFormatHandler {
	fun getAllKeys(project: Project): Collection<String> {
		if(DumbService.isDumb(project)) return emptySet()
		
		ProgressManager.checkCanceled()
		val indexId = ParadoxStellarisNameFormatKeyIndex.name
		return FileBasedIndex.getInstance().getAllKeys(indexId, project)
	}
	
	@JvmStatic
	fun getValueSetName(localisationKey: String, project: Project): String? {
		if(DumbService.isDumb(project)) return null
		
		var result: String? = null
		val indexId = ParadoxStellarisNameFormatKeyIndex.name
		FileBasedIndex.getInstance().processValues(indexId, localisationKey, null, { _, value ->
			result = value
			false
		}, GlobalSearchScope.allScope(project))
		return result
	}
}