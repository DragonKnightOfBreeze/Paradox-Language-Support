package icu.windea.pls.core.library

import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.roots.libraries.*
import com.intellij.openapi.roots.libraries.ui.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import javax.swing.*

class ParadoxLibraryType : LibraryType<ParadoxLibraryProperties>(ParadoxLibraryKind) {
	override fun getCreateActionName() = "Paradox"
	
	override fun getIcon(properties: ParadoxLibraryProperties?) = PlsIcons.libraryIcon
	
	override fun getExternalRootTypes() = arrayOf(OrderRootType.SOURCES)
	
	override fun createPropertiesEditor(editorComponent: LibraryEditorComponent<ParadoxLibraryProperties>) = null
	
	override fun createNewLibrary(parentComponent: JComponent, contextDirectory: VirtualFile?, project: Project): NewLibraryConfiguration? {
		//必须是一个文件夹，且必须包含descriptor.mod或launcher-settings.json
		//TODO 兼容压缩包
		val dialog = ParadoxCreateNewLibraryDialog(project, contextDirectory)
		if(dialog.showAndGet()) {
			val rootFile = dialog.rootFile
			val rootInfo = dialog.rootInfo
			val descriptorInfo = rootInfo?.descriptorInfo
			if(rootFile != null && rootInfo != null && descriptorInfo != null) {
				//基于descriptor.mod或launcher-settings.json得到库的名字，由于此文件可能发生变更，不保存库的属性
				val libraryName = getLibraryName(rootInfo, descriptorInfo)
				val libraryProperties = ParadoxLibraryProperties(rootInfo)
				return ParadoxNewLibraryConfiguration(libraryName, this, rootFile, libraryProperties)
			}
		}
		return null
	}
	
	private fun getLibraryName(rootInfo: ParadoxRootInfo, descriptorInfo: ParadoxDescriptorInfo): String {
		//FIXME 这里的名字和版本基于描述符文件，可能会过时
		return buildString {
			val rootType = rootInfo.rootType
			append("Paradox/").append(rootInfo.gameType).append(" ").append(rootType.description)
			val version = descriptorInfo.version
			if(rootType == ParadoxRootType.Mod) append(": ").append(descriptorInfo.name)
			if(version != null) append("@").append(version)
		}
	}
}