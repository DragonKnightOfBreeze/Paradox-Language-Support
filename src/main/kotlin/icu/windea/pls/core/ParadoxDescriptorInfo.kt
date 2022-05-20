package icu.windea.pls.core

//see: descriptor.cwt

class ParadoxDescriptorInfo(
	val name: String,
	val version: String? = null,
	val picture: String? = null,
	val tags: Set<String>? = null,
	val supportedVersion: String? = null,
	val remoteFileId: String? = null,
	val path: String? = null,
	val isModeDescriptor: Boolean = true
){
	//如果是模组描述符，返回的游戏版本允许通配符，如："3.3.*"
	val gameVersion = if(isModeDescriptor) supportedVersion else version
}