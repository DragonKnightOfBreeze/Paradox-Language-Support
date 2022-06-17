package icu.windea.pls.model

import java.util.*

//see: descriptor.cwt

@Suppress("unused")
class ParadoxDescriptorInfo(
	val name: String,
	val version: String? = null,
	val picture: String? = null,
	val tags: Set<String>? = null,
	val supportedVersion: String? = null,
	val remoteFileId: String? = null,
	val path: String? = null,
	val isModDescriptor: Boolean = true
){
	//如果是模组描述符，返回的游戏版本允许通配符，如："3.3.*"
	val gameVersion = if(isModDescriptor) supportedVersion else version
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDescriptorInfo
			&& name == other.name && version == other.version && supportedVersion == other.supportedVersion
			&& remoteFileId == other.remoteFileId && path == other.path
	}
	
	override fun hashCode(): Int {
		return Objects.hash(name, version, supportedVersion, remoteFileId, path)
	}
}