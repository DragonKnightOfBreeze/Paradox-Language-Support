package icu.windea.pls.localisation.psi

import com.intellij.psi.stubs.*

object ParadoxSyncedLocalisationNameIndex : AbstractParadoxLocalisationNameIndex() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.syncedLocalisation.name.index")
	private const val version = 10 //0.7.4
	private const val cacheSize = 2 * 1024
	
	override fun getKey() = key
	override fun getVersion() = version
	override fun getCacheSize() = cacheSize
}
