package icu.windea.pls.dds.support

import com.intellij.openapi.vfs.*

/**
 * 用于在Windows环境下，通过DirectXTex的Texconv工具，进行相关的图片处理操作，例如将DDS图片转化为PNG图片。
 *
 * 参见：[Texconv · microsoft/DirectXTex Wiki](https://github.com/microsoft/DirectXTex/wiki/Texconv)
 */
class DirectXTexBasedDdsSupport: DdsSupport {
    override fun getMetadata(file: VirtualFile): DdsMetadata? {
        return null
    }
}
