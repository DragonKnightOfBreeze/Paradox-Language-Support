package icu.windea.pls.dds.support

import com.intellij.openapi.extensions.*
import com.intellij.openapi.vfs.*

/**
 * 用于提供对DDS图片的支持，包括获取元信息、渲染图片、转化图片格式等。
 */
interface DdsSupport {
    fun getMetadata(file: VirtualFile): DdsMetadata?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<DdsSupport>("icu.windea.pls.dds.support")
    }
}
