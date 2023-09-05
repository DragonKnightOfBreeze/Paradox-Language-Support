package icu.windea.pls.lang.modifier

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.annotations.*

/**
 * 用于为修正提供图标的图片。
 *
 * 注意：修正的图标的图片对应的文件类型可以是DDS、PNG。
 */
@WithGameTypeEP
interface ParadoxModifierIconProvider {
    fun addModifierIconPath(name: String, registry: MutableSet<String>)
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxModifierIconProvider>("icu.windea.pls.modifierIconProvider")
    }
}