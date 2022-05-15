package icu.windea.pls.dds

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.tool.*
import org.intellij.images.editor.ImageDocument.*
import org.intellij.images.vfs.IfsUtil

internal fun VirtualFile.isDdsFileType(): Boolean {
	return FileTypeRegistry.getInstance().isFileOfType(this, DdsFileType) //参考的代码中如此
}

internal fun VirtualFile.getImageProvider(): ScaledImageProvider? {
	val pngFile = ParadoxDdsUrlResolver.getPngFile(this) ?: return null
	return IfsUtil.getImageProvider(pngFile)
}