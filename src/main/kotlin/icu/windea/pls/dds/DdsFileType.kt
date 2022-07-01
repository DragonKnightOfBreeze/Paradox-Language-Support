package icu.windea.pls.dds

import com.intellij.openapi.fileTypes.*
import icu.windea.pls.*
import javax.swing.*

//org.intellij.images.fileTypes.impl.ImageFileType

object DdsFileType : UserBinaryFileType() {
	override fun getName(): String {
		return ddsName
	}
	
	override fun getDescription(): String {
		return ddsDescription
	}
	
	override fun getIcon(): Icon {
		return PlsIcons.DdsFile
	}
}