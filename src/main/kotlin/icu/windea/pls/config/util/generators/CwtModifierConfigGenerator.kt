package icu.windea.pls.config.util.generators

import com.intellij.openapi.project.Project
import icu.windea.pls.model.ParadoxGameType

/**
 * 从 `modifiers.log` 生成 `modifiers.cwt`。
 */
class CwtModifierConfigGenerator(
    override val gameType: ParadoxGameType,
    override val inputPath: String,
    override val outputPath: String,
) : CwtConfigGenerator {
    override fun getDefaultGeneratedFileName() = "modifiers.cwt"

    override suspend fun generate(project: Project): CwtConfigGenerator.Hint {
        TODO("2.0.6-dev")
    }
}
