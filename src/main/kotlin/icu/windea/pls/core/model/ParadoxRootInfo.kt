package icu.windea.pls.core.model

import com.fasterxml.jackson.module.kotlin.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*
import java.nio.file.*

/**
 * @property rootFile 游戏或模组根目录
 * @property descriptorFile 描述符文件（`descriptor.mod`或`launcher-settings.json`）
 * @property markerFile 游戏类型标记文件（如`.stellaris`）
 */
class ParadoxRootInfo(
	val rootFile: VirtualFile,
	val descriptorFile: VirtualFile,
	val markerFile: VirtualFile?,
	val rootType: ParadoxRootType
) {
	val rootPath: Path = rootFile.toNioPath()
	val gameTypeFromMarkerFile: ParadoxGameType? = markerFile?.let { file -> ParadoxGameType.resolve(file) }
	val gameType: ParadoxGameType get() = gameTypeFromMarkerFile ?: getSettings().defaultGameType
	val descriptorInfo: ParadoxDescriptorInfo? get() = doGetDescriptorInfo()
	
	val isValid get() = rootFile.isValid
	val isAvailable get() = descriptorFile.isValid && (markerFile?.isValid != false)
	
	private fun doGetDescriptorInfo(): ParadoxDescriptorInfo? {
		val result = descriptorFile.getUserData(PlsKeys.descriptorInfoKey)
		if(result != null) return result
		val resolved = runCatching { resolveDescriptorInfo(descriptorFile) }.getOrNull()
		descriptorFile.putUserData(PlsKeys.descriptorInfoKey, resolved)
		return resolved
	}
	
	private fun resolveDescriptorInfo(descriptorFile: VirtualFile): ParadoxDescriptorInfo? {
		val fileName = descriptorFile.name
		return when {
			fileName == descriptorFileName -> {
				//TODO 直接解析VirtualFile
				val psiFile = descriptorFile.toPsiFile<PsiFile>(getDefaultProject())
				if(psiFile !is ParadoxScriptFile) return null
				val rootBlock = psiFile.findOptionalChild<ParadoxScriptRootBlock>() ?: return null
				var name: String? = null
				var version: String? = null
				var picture: String? = null
				var tags: Set<String>? = null
				var supportedVersion: String? = null
				var remoteFileId: String? = null
				var path: String? = null
				rootBlock.processProperty { property ->
					when(property.name) {
						"name" -> name = property.findValue<ParadoxScriptString>()?.stringValue
						"version" -> version = property.findValue<ParadoxScriptString>()?.stringValue
						"picture" -> picture = property.findValue<ParadoxScriptString>()?.stringValue
						"tags" -> tags = property.findValue<ParadoxScriptBlock>()?.findValues<ParadoxScriptString>()?.mapTo(mutableSetOf()) { it.stringValue }
						"supported_version" -> supportedVersion = property.findValue<ParadoxScriptString>()?.stringValue
						"remote_file_id" -> remoteFileId = property.findValue<ParadoxScriptString>()?.stringValue
						"path" -> path = property.findValue<ParadoxScriptString>()?.stringValue
					}
					true
				}
				val nameToUse = name ?: descriptorFile.parent?.name ?: anonymousString //如果没有name属性，则使用根目录名
				ParadoxDescriptorInfo(nameToUse, version, picture, tags, supportedVersion, remoteFileId, path, isModDescriptor = true)
			}
			
			fileName == launcherSettingsFileName -> {
				val json = jsonMapper.readValue<Map<String, Any?>>(descriptorFile.inputStream)
				val name = gameType.description
				val version = json.get("rawVersion")?.toString()
				ParadoxDescriptorInfo(name, version, isModDescriptor = false)
			}
			
			else -> null
		}
	}
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxRootInfo && rootFile == other.rootFile
	}
	
	override fun hashCode(): Int {
		return rootFile.hashCode()
	}
	
	companion object {
		val values = mutableSetOf<ParadoxRootInfo>()
	}
}