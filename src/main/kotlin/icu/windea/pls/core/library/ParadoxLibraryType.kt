package icu.windea.pls.core.library

import com.intellij.ide.highlighter.*
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
	
	//如果存在描述符文件，其中有name属性则取name属性的值，否则取库的文件名/目录
	//如果存在游戏执行文件，则认为是标准库
	//如果根目录名是特定的字符串，需要特殊处理，视为特殊的rootType
	//TODO 兼容文件夹和zip压缩包
	private fun getLibraryName(file: VirtualFile, project: Project): String? {
		//这里file如果是zip文件而不是目录，则file.children返回空
		val name = file.name.substringBeforeLast('.',"")
		//FIXME 实际测试读不到zip文件中的内容
		//if(file.fileType == ArchiveFileType.INSTANCE){
		//	val zipFile = JarFileSystem.getInstance().getRootByLocal(file)!!
		//	val files = zipFile.children
		//	println("111")
		//}
		//处理特殊顶级目录的情况
		when{
			name.equals(ParadoxRootType.PdxLauncher.key, true) -> return ParadoxRootType.PdxLauncher.text
			name.equals(ParadoxRootType.PdxOnlineAssets.key, true) -> return ParadoxRootType.PdxOnlineAssets.text
			name.equals(ParadoxRootType.TweakerGuiAssets.key, true) -> return ParadoxRootType.TweakerGuiAssets.text
		}
		//处理模组目录的情况
		val descriptorFile = file.findChild(descriptorFileName)
		if(descriptorFile != null) {
			//从descriptor.name中获取，或者直接使用目录/压缩包去除后缀名后的名字
			return getLibraryNameFromDescriptorFile(descriptorFile)?:name 
		}
		//处理游戏目录的情况
		val exeFile = file.children.find { it.name.substringAfterLast('.',"").lowercase() == "exe" } //TODO 严格验证
		if(exeFile != null){
			return ParadoxRootType.Stdlib.text
		} 
		//不合法的情况要弹出对话框
		showInvalidLibraryDialog(project)
		return null
	}
	
	private fun getLibraryNameFromDescriptorFile(file:VirtualFile):String?{
		val text = file.inputStream.reader().use { it.readText() }
		for(line in text.lines()) {
			val lineText = line.trim()
			if(lineText.startsWith("name")) {
				return line.substringAfter('=').trim().unquote().trim()
			}
		}
		return null
	}
	
	private fun showInvalidLibraryDialog(project: Project) {
		Messages.showWarningDialog(project, _invalidLibraryPathMessage, _invalidLibraryPathTitle)
	}
	
	override fun createPropertiesEditor(editorComponent: LibraryEditorComponent<ParadoxLibraryProperties>) = null
}