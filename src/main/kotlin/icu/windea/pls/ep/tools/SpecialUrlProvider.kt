package icu.windea.pls.ep.tools

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType
import org.jetbrains.annotations.Nls
import javax.swing.Icon

/**
 * 用于根据可选的上下文参数，得到特定的特殊链接。
 *
 * @property icon 用于 UI 展示的图标。
 * @property text 用于 UI 展示的文本。
 */
interface SpecialUrlProvider {
    val icon: Icon? get() = null
    val text: @Nls String

    fun getUrl(file: VirtualFile? = null, gameType: ParadoxGameType? = null): String?

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<SpecialUrlProvider>("icu.windea.pls.specialUrlProvider")
    }
}
