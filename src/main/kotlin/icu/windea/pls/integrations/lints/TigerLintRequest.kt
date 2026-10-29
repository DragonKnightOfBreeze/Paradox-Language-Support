package icu.windea.pls.integrations.lints

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType
import java.nio.file.Path

/**
 * Tiger 检查工具的检查请求。
 *
 * 该请求不依赖项目或 PSI；[rootFile] 仅用于追踪根目录的变化。
 *
 * @property gameType 游戏类型。
 * @property name 检查工具的名字。
 * @property exePath 检查工具的可执行文件路径。
 * @property workingDirectory 执行命令时的工作目录。
 * @property rootPath 模组根目录路径。
 * @property rootFile 模组根目录对应的虚拟文件。
 * @property gamePath 游戏目录路径。
 * @property configPath 配置文件路径。
 * @property outputPath 检查结果的输出路径。
 */
class TigerLintRequest(
    val gameType: ParadoxGameType,
    val name: String,
    val exePath: Path,
    val workingDirectory: Path,
    val rootPath: String,
    val rootFile: VirtualFile,
    val gamePath: String? = null,
    val configPath: String? = null,
    val outputPath: Path,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TigerLintRequest) return false
        return gameType == other.gameType &&
            name == other.name &&
            exePath == other.exePath &&
            rootPath == other.rootPath &&
            gamePath == other.gamePath &&
            configPath == other.configPath &&
            outputPath == other.outputPath
    }

    override fun hashCode(): Int {
        var result = gameType.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + exePath.hashCode()
        result = 31 * result + rootPath.hashCode()
        result = 31 * result + (gamePath?.hashCode() ?: 0)
        result = 31 * result + (configPath?.hashCode() ?: 0)
        result = 31 * result + outputPath.hashCode()
        return result
    }

    override fun toString(): String {
        return "TigerLintRequest(gameType=$gameType, name=$name, rootPath=$rootPath)"
    }
}
