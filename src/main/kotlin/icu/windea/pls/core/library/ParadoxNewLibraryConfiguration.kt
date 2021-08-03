package icu.windea.pls.core.library

import com.intellij.openapi.roots.*
import com.intellij.openapi.roots.libraries.*
import com.intellij.openapi.roots.ui.configuration.libraryEditor.*
import com.intellij.openapi.vfs.*

class ParadoxNewLibraryConfiguration(
	name:String,
	libraryType: ParadoxLibraryType,
	private val libraryFile: VirtualFile,
	libraryProperties : ParadoxLibraryProperties
): NewLibraryConfiguration(name,libraryType,libraryProperties){
	//TODO 兼容文件夹和zip压缩包
	override fun addRoots(editor: LibraryEditor) {
		editor.addRoot(libraryFile, OrderRootType.SOURCES)
	}
}