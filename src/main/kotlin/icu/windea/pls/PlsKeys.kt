package icu.windea.pls

import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.util.*
import icu.windea.pls.config.definition.config.*
import icu.windea.pls.model.*

object PlsKeys {
	val rootInfoKey = Key<ParadoxRootInfo>("paradoxRootInfo")
	val descriptorInfoKey = Key<ParadoxDescriptorInfo>("paradoxDescriptorInfo")
	val fileInfoKey = Key<ParadoxFileInfo>("paradoxFileInfo")
	val contentFileKey = Key<VirtualFile>("paradoxContentFile")
	
	val cachedDefinitionInfoKey = Key<CachedValue<ParadoxDefinitionInfo>>("cachedParadoxDefinitionInfo")
	val cachedLocalisationInfoKey = Key<CachedValue<ParadoxLocalisationInfo>>("cachedParadoxLocalisationInfo")
	
	val definitionElementInfoKey = Key<ParadoxDefinitionElementInfo>("paradoxDefinitionElementInfo")
	
	val textColorConfigKey = Key<ParadoxTextColorConfig>("paradoxTextColorConfig")
	
	val definitionConfigKeys = setOf<Key<out ParadoxDefinitionConfig>>(
		textColorConfigKey
	)
}

