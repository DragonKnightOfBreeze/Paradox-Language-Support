package icu.windea.pls.core

//see: descriptor.cwt

class ParadoxDescriptorInfo(
	val name: String,
	val version: String? = null,
	val picture: String? = null,
	val tags: Set<String>? = null,
	val supportedVersion: String? = null,
	val remoteFileId: String? = null,
	val path: String? = null
)