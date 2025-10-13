package icu.windea.pls.config.util.generators

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.model.ParadoxGameType

/**
 * 规则文件的生成器。
 *
 * 用于从日志或脚本文件生成规则文件的维护提示。
 *
 * @property gameType 指定的游戏类型。
 * @property inputPath 输入的日志文件的路径，或者输入的脚本文件所在目录的路径。
 * @property outputPath 输出的规则文件的路径。
 */
interface CwtConfigGenerator {
    val gameType: ParadoxGameType
    val inputPath: String
    val outputPath: String

    /**
     * 生成规则文件的维护提示。
     */
    suspend fun generate(project: Project): Hint

    /**
     * 规则文件的维护提示。
     *
     * 可以通过 userData 存储额外的元数据。
     *
     * @property summary 总结。
     * @property details 详情。
     * @property fileText 要生成的文件的文本（避免直接替换文件文本，优先考虑手动替换，以及显示 DIFF 视图）。
     */
    data class Hint(
        val summary: String,
        val details: String,
        val fileText: String,
    ) : UserDataHolderBase()
}

