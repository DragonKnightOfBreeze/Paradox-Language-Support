package icu.windea.pls.dds

import com.intellij.openapi.vfs.*
import icu.windea.pls.tool.*
import org.intellij.images.editor.ImageDocument.*
import org.intellij.images.vfs.*

internal fun VirtualFile.getImageProvider(): ScaledImageProvider? {
	val pngFile = ParadoxDdsUrlResolver.getPngFile(this) ?: return null
	return IfsUtil.getImageProvider(pngFile)
}