package icu.windea.pls.localisation.psi

import com.intellij.psi.stubs.*

object ParadoxLocalisationNameIndex : AbstractParadoxLocalisationNameIndex() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index")
	private const val version = 10 //0.7.4
	private const val cacheSize = 100 * 1024
	
	override fun getKey() = key
	override fun getVersion() = version
	override fun getCacheSize() = cacheSize
}