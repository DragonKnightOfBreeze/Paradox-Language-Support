package icu.windea.pls.ep.analysis

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.model.ParadoxGameType
import java.nio.file.Path

/**
 * 提供推断的游戏类型。
 *
 * @see ParadoxGameType
 */
interface ParadoxInferredGameTypeProvider {
    fun get(rootPath: Path): ParadoxGameType?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxInferredGameTypeProvider>("icu.windea.pls.inferredGameTypeProvider")
    }
}
