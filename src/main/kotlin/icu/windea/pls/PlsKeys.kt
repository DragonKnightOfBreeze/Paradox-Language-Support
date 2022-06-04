package icu.windea.pls

import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

object PlsKeys {
	val paradoxFileInfoKey = Key<ParadoxFileInfo>("paradoxFileInfo")
	val contentVirtualFileKey = Key<VirtualFile>("paradoxContentVirtualFile")
	
	val cachedParadoxDescriptorInfoKey = Key<CachedValue<ParadoxDescriptorInfo>>("cachedParadoxDescriptorInfo")
	val cachedParadoxDefinitionInfoKey = Key<CachedValue<ParadoxDefinitionInfo>>("cachedParadoxDefinitionInfo")
	val cachedParadoxDefinitionElementInfoKey = Key<CachedValue<ParadoxDefinitionElementInfo>>("cachedParadoxDefinitionElementInfo")
	val cachedParadoxLocalisationInfoKey = Key<CachedValue<ParadoxLocalisationInfo>>("cachedParadoxLocalisationInfo")
}