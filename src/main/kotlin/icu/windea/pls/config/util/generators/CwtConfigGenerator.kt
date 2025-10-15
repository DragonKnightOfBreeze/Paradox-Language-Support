package icu.windea.pls.config.util.generators

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.model.ParadoxGameType

/**
 * 规则文件的生成器。
 *
 * 用于从日志或脚本文件生成规则文件的维护提示。
 *
 * NOTE 2.0.6+ 仅会检查已存在项的名字，不会检查文档注释与选项注释（目前保持设计如此）。
 *
 * @property project 指定的项目。
 * @property fromScripts 是否从脚本文件生成。
 */
interface CwtConfigGenerator {
    val project: Project
    val fromScripts: Boolean get() = false

    /** 得到生成器的名字。 */
    fun getName(): String

    /** 得到输入的日志文件的默认文件名，或者输入的脚本文件所在目录的相对于游戏目录的默认路径。 */
    fun getDefaultInputName(): String

    /** 得到输出的规则文件的默认文件名。 */
    fun getDefaultOutputName(): String

    /**
     * 生成规则文件的维护提示。
     *
     * @param gameType 指定的游戏类型。
     * @param inputPath 输入的日志文件的路径，或者输入的脚本文件所在目录的路径。
     * @param outputPath 输出的规则文件的路径。
     */
    suspend fun generate(gameType: ParadoxGameType, inputPath: String, outputPath: String): Hint

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
