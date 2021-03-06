package icu.windea.pls.core.library

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.roots.libraries.*
import com.intellij.openapi.roots.libraries.ui.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.library.ParadoxLibraryKind.*
import icu.windea.pls.model.*
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
		
	private val createActionName = "Paradox/${gameType}"
	private val libraryIcon = gameType.icon
	private val namePrefix = "$createActionName: "
	
	companion object {
		private val _chooserTitle = message("pls.library.chooser.title")
		private val _chooserDescription = message("pls.library.chooser.description")
		private val _invalidLibraryPathTitle = message("pls.library.dialog.invalidLibraryPath.title")
		private val _invalidLibraryPathMessage = message("pls.library.dialog.invalidLibraryPath.message")
	}
	
	override fun getCreateActionName() = createActionName
	
	override fun getIcon(properties: ParadoxLibraryProperties?) = libraryIcon
	
	override fun getExternalRootTypes() = arrayOf(OrderRootType.SOURCES)
	
	//TODO 兼容zip压缩包
	//如果存在描述符文件，其中有name属性则取name属性的值，否则取库的文件名/目录
	//如果存在游戏执行文件，则认为是标准库
	//如果根目录名是特定的字符串，需要特殊处理，视为特殊的rootType
	private fun getLibraryName(file: VirtualFile, project: Project): String? {
		val fileName = file.name
		for(child in file.children) {
			val childName = child.name
			val childExtension = child.extension
			//要求：根目录包含描述符文件descriptor.mod或者对应的.exe文件，或者根目录名是特定的字符串
			when {
				childName.equals(descriptorFileName, true) -> {
					val text = child.inputStream.reader().use { it.readText() }
					for(line in text.lines()) {
						if(line.startsWith("name")) {
							return line.substringAfter('=').trim().unquote().trim()
						}
					}
					return file.nameWithoutExtension
				}
				fileName.equals(ParadoxRootType.PdxLauncher.key, true) -> return ParadoxRootType.PdxLauncher.text
				fileName.equals(ParadoxRootType.PdxOnlineAssets.key, true) -> return ParadoxRootType.PdxOnlineAssets.text
				fileName.equals(ParadoxRootType.TweakerGuiAssets.key, true) -> return ParadoxRootType.TweakerGuiAssets.text
				//TODO 可能并不是这样命名，需要检查
				//childName.equals("${gameType.name}.exe", true) -> return ParadoxRootType.Stdlib.text
				childExtension.equals("exe", true) -> return ParadoxRootType.Stdlib.text
			}
		}
		showInvalidLibraryDialog(project)
		return null
	}
	
	private fun showInvalidLibraryDialog(project: Project) {
		Messages.showWarningDialog(project, _invalidLibraryPathMessage, _invalidLibraryPathTitle)
	}
	
	//必须是一个文件夹，但必须包含descriptor.mod或者.exe文件
	override fun createNewLibrary(parentComponent: JComponent, contextDirectory: VirtualFile?, project: Project): NewLibraryConfiguration? {
		if(contextDirectory == null) return null
		val chooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
		chooserDescriptor.title = _chooserTitle
		chooserDescriptor.description = _chooserDescription
		val file = FileChooser.chooseFile(chooserDescriptor, parentComponent, project, contextDirectory) ?: return null
		val name = getLibraryName(file, project) ?: return null
		val libraryProperties = ParadoxLibraryProperties.instance
		return ParadoxNewLibraryConfiguration(namePrefix + name, this, file, libraryProperties)
	}
	
	override fun createPropertiesEditor(editorComponent: LibraryEditorComponent<ParadoxLibraryProperties>) = null
}