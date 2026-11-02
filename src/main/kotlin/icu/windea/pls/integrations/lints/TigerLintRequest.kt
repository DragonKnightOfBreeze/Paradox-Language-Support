package icu.windea.pls.integrations.lints

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType
import java.nio.file.Path

/**
 * Tiger 检查工具的检查请求。
 *
 * 该请求不依赖项目或 PSI，而 [rootFile] 仅用于追踪更改。
 *
 * 参见：[Tiger](https://github.com/amtep/tiger)
 *
 * @property name 检查工具的名字。
 * @property gameType 游戏类型。
 * @property executablePath 检查工具的可执行文件路径。
 * @property confPath 检查工具的配置文件路径。
 * @property workingDirectory 执行命令时的工作目录。
 * @property rootFile 模组根目录对应的虚拟文件。用于追踪更改。
 * @property modPath 模组路径（`.mod` 文件的路径，或者模组根目录路径）。作为 Tiger 的实际输入。
 * @property gamePath 游戏目录路径。
 * @property outputPath 检查结果的输出路径。
 */
data class TigerLintRequest(
    override val name: String,
    override val gameType: ParadoxGameType,
    val executablePath: Path,
    val confPath: Path? = null,
    val workingDirectory: Path,
    val rootFile: VirtualFile,
    val gamePath: Path? = null,
    val modPath: Path,
    val outputPath: Path,
): LintRequest {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TigerLintRequest) return false
        return name == other.name
            && gameType == other.gameType
            && executablePath == other.executablePath
            && confPath == other.confPath
            && gamePath == other.gamePath
            && modPath == other.modPath
            && outputPath == other.outputPath
    }

    override fun hashCode(): Int {
        var result = gameType.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + executablePath.hashCode()
        result = 31 * result + confPath.hashCode()
        result = 31 * result + gamePath.hashCode()
        result = 31 * result + modPath.hashCode()
        result = 31 * result + outputPath.hashCode()
        return result
    }

    override fun toString(): String {
        return "TigerLintRequest(name=$name, gameType=$gameType, gamePath=$gamePath, modPath=$modPath)"
    }
}
