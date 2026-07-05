package icu.windea.pls.ep.analysis

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxGameTypeInfo
import java.nio.file.Path

/**
 * 提供推断的游戏类型。
 *
 * @see ParadoxGameType
 * @see ParadoxGameTypeInfo
 */
interface ParadoxInferredGameTypeProvider {
    fun getInferredGameTypeInfo(rootPath: Path): ParadoxGameTypeInfo?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxInferredGameTypeProvider>("icu.windea.pls.inferredGameTypeProvider")
    }
}
