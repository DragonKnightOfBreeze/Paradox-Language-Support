package icu.windea.pls.config.util.generators

import com.intellij.openapi.project.Project
import icu.windea.pls.model.ParadoxGameType

/**
 * 从 `on_actions.txt` 生成 `on_actions.cwt`。
 */
class CwtOnActionConfigGenerator(
    override val gameType: ParadoxGameType,
    override val inputPath: String,
    override val outputPath: String,
) : CwtConfigGenerator {
    override fun getDefaultGeneratedFileName() = "on_actions.cwt"

    override suspend fun generate(project: Project): CwtConfigGenerator.Hint {
        TODO("2.0.6-dev")
    }
}
