package icu.windea.pls.core.ui

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.vfs.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*
import javax.swing.*

/**
 * @see icu.windea.pls.core.ParadoxProjectViewDecorator
 */
class ParadoxRootDirectoryDescriptor: FileChooserDescriptor(false, true, false, false, false, false) {
	override fun getIcon(file: VirtualFile): Icon {
		val fileInfo = file.fileInfo ?: return super.getIcon(file)
		val rootInfo = fileInfo.rootInfo
		if(rootInfo is ParadoxModRootInfo && file == rootInfo.rootFile) {
			val icon = when(rootInfo.rootType) {
				ParadoxRootType.Game -> PlsIcons.GameDirectory
				ParadoxRootType.Mod -> PlsIcons.ModDirectory
			}
			return icon
		}
		return super.getIcon(file)
	}
	
	override fun getComment(file: VirtualFile): String? {
		val fileInfo = file.fileInfo ?: return super.getComment(file)
		val rootInfo = fileInfo.rootInfo
		if(rootInfo is ParadoxModRootInfo && file == rootInfo.rootFile) {
			return rootInfo.descriptorInfo.qualifiedName
		}
		return super.getComment(file)
	}
}