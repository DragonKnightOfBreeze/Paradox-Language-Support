package icu.windea.pls

import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*

object PlsKeys {
	val cachedParadoxDescriptorInfoKey = Key<CachedValue<ParadoxDescriptorInfo>>("cachedParadoxDescriptorInfo")
	val paradoxFileInfoKey = Key<ParadoxFileInfo>("paradoxFileInfo")
	val cachedParadoxDefinitionInfoKey = Key<CachedValue<ParadoxDefinitionInfo>>("cachedParadoxDefinitionInfo")
	val isDefinitionKey = Key<Boolean>("isDefinition")
	val cachedParadoxDefinitionElementInfoKey = Key<CachedValue<ParadoxDefinitionElementInfo>>("cachedParadoxDefinitionElementInfo")
	val cachedParadoxLocalisationInfoKey = Key<CachedValue<ParadoxLocalisationInfo>>("cachedParadoxLocalisationInfo")
}