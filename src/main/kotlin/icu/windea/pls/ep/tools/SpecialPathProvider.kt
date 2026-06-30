package icu.windea.pls.ep.tools

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType
import org.jetbrains.annotations.Nls
import java.nio.file.Path
import javax.swing.Icon

/**
 * 用于根据可选的上下文参数，得到特定的特殊路径（可能不存在于本地）。
 *
 * @property icon 用于 UI 展示的图标。
 * @property text 用于 UI 展示的文本。
 */
interface SpecialPathProvider {
    val icon: Icon? get() = null
    val text: @Nls String

    fun getPath(file: VirtualFile? = null, gameType: ParadoxGameType? = null): Path?

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<SpecialPathProvider>("icu.windea.pls.specialPathProvider")
    }
}
