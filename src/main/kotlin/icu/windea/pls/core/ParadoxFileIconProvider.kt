package icu.windea.pls.core

import com.intellij.ide.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import javax.swing.*

class ParadoxFileIconProvider: FileIconProvider {
	override fun getIcon(file: VirtualFile, flags: Int, project: Project?): Icon? {
		//对模组描述符文件使用特定的图标
		if(file.fileInfo?.fileType == ParadoxFileType.ParadoxScript && file.name.equals(descriptorFileName, true)){
			return PlsIcons.DescriptorFile
		}
		return null
	}
}