package icu.windea.pls.config.util.generators

import com.intellij.openapi.project.Project
import icu.windea.pls.model.ParadoxGameType

/**
 * 从 `game_rules.txt` 生成 `game_rules.cwt`。
 */
class CwtGameRuleConfigGenerator(
    override val gameType: ParadoxGameType,
    override val inputPath: String,
    override val outputPath: String,
) : CwtConfigGenerator {
    override suspend fun generate(project: Project): CwtConfigGenerator.Hint {
        TODO("2.0.6-dev")
    }
}
