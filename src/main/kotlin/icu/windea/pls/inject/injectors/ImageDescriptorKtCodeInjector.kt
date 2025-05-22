@file:Suppress("UnstableApiUsage", "UNUSED_PARAMETER")

package icu.windea.pls.inject.injectors

import icu.windea.pls.core.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*

/**
 * @see com.intellij.ui.icons.createImageDescriptorList
 */
@InjectTarget("com.intellij.ui.icons.ImageDescriptorKt")
class ImageDescriptorKtCodeInjector : CodeInjectorBase() {
    //用于修复 com.intellij.ui.icons.createImageDescriptorList 中的一处 BUG

    //NOTE 不可行：应用代码注入器时相关类已加载

    private var Any.svg: Boolean by memberProperty("isSvg")

    @InjectMethod(pointer = InjectMethod.Pointer.AFTER, static = true)
    fun createImageDescriptorList(path: String, isDark: Boolean, isStroke: Boolean, pixScale: Float, returnValue: List<Any>): List<Any> {
        if (returnValue.size >= 4) {
            val isSvg = path.endsWith(".svg")
            if (isDark && !isSvg) {
                val last = returnValue.last()
                last.svg = false
            }
        }
        return returnValue
    }
}
