package icu.windea.pls.core.library

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.roots.libraries.*
import com.intellij.openapi.roots.libraries.ui.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.library.ParadoxLibraryKind.*
import javax.swing.*

abstract class ParadoxLibraryType(
	libraryKind: ParadoxLibraryKind
) : LibraryType<ParadoxLibraryProperties>(libraryKind) {
	class Ck2LibraryType : ParadoxLibraryType(Ck2LibraryKind)
	class Ck3LibraryType : ParadoxLibraryType(Ck3LibraryKind)
	class Eu4LibraryType : ParadoxLibraryType(Eu4LibraryKind)
	class Hoi4LibraryType : ParadoxLibraryType(Hoi4LibraryKind)
	class IrLibraryType : ParadoxLibraryType(IrLibraryKind)
	class StellarisLibraryType : ParadoxLibraryType(StellarisLibraryKind)
	class Vic2LibraryType : ParadoxLibraryType(Vic2LibraryKind)
	
	val gameType = libraryKind.gameType
	
	private val createActionName = "Paradox/${gameType.description}"
	private val libraryNamePrefix = "Paradox/${gameType.description}"
	private val libraryIcon = gameType.icon
	
	override fun getCreateActionName() = createActionName
	
	override fun getIcon(properties: ParadoxLibraryProperties?) = libraryIcon
	
	override fun getExternalRootTypes() = arrayOf(OrderRootType.SOURCES)
	
	override fun createLibraryRootsComponentDescriptor(): LibraryRootsComponentDescriptor? {
		return super.createLibraryRootsComponentDescriptor()
	}
	
	override fun createNewLibrary(parentComponent: JComponent, contextDirectory: VirtualFile?, project: Project): NewLibraryConfiguration? {
		//必须是一个文件夹，且必须包含descriptor.mod或launcher-settings.json
		//TODO 兼容压缩包
		if(contextDirectory == null) return null
		val chooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
		chooserDescriptor.title = PlsBundle.message("library.chooser.title")
		chooserDescriptor.description = PlsBundle.message("library.chooser.description")
		val root = FileChooser.chooseFile(chooserDescriptor, parentComponent, project, contextDirectory) ?: return null
		setFileInfoAndGetFileType(root, root, project, emptyList(), root.name)
		val fileInfo = root.fileInfo
		val descriptorInfo = fileInfo?.getDescriptorInfo(project)
		if(fileInfo != null && descriptorInfo != null) {
			//基于descriptor.mod或launcher-settings.json得到库的名字，由于此文件可能发生变更，不保存库的属性
			val libraryName = getLibraryName(fileInfo, descriptorInfo)
			val libraryProperties = ParadoxLibraryProperties.instance
			return ParadoxNewLibraryConfiguration(libraryName, this, root, libraryProperties)
		}
		//不合法的情况要弹出对话框
		showInvalidLibraryDialog(project)
		return null
	}
	
	private fun getLibraryName(fileInfo: ParadoxFileInfo, descriptorInfo: ParadoxDescriptorInfo): String {
		return buildString {
			val rootType = fileInfo.rootType
			append(libraryNamePrefix).append(" ").append(rootType.description)
			val version = descriptorInfo.version
			if(rootType == ParadoxRootType.Mod) append(": ").append(descriptorInfo.name)
			if(version != null) append("@").append(version)
		}
	}
	
	private fun showInvalidLibraryDialog(project: Project) {
		Messages.showWarningDialog(project, PlsBundle.message("library.dialog.invalidLibraryPath.message"), PlsBundle.message("library.dialog.invalidLibraryPath.title"))
	}
	
	override fun createPropertiesEditor(editorComponent: LibraryEditorComponent<ParadoxLibraryProperties>) = null
}