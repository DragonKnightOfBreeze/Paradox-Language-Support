package icu.windea.pls

import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.util.*
import icu.windea.pls.model.*

object PlsKeys {
	val paradoxRootInfoKey = Key<ParadoxRootInfo>("paradoxRootInfo")
	val paradoxDescriptorInfoKey = Key<ParadoxDescriptorInfo>("paradoxDescriptorInfo")
	val paradoxFileInfoKey = Key<ParadoxFileInfo>("paradoxFileInfo")
	val contentFileKey = Key<VirtualFile>("paradoxContentFile")
	
	val cachedParadoxDefinitionInfoKey = Key<CachedValue<ParadoxDefinitionInfo>>("cachedParadoxDefinitionInfo")
	val cachedParadoxDefinitionElementInfoKey = Key<CachedValue<ParadoxDefinitionElementInfo>>("cachedParadoxDefinitionElementInfo")
	val cachedParadoxLocalisationInfoKey = Key<CachedValue<ParadoxLocalisationInfo>>("cachedParadoxLocalisationInfo")
}

