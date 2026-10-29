package icu.windea.pls.base.io

import icu.windea.pls.core.toPath
import icu.windea.pls.core.util.io.PathService
import java.nio.file.Path

class ChronicleDataPathServiceImpl : ChronicleDataPathService {
    // NOTE 2.1.8 改为在需要时才创建目录或文件（不需要经过 VFS，不需要在打开应用时异步初始化，应当不会有什么耗时）

    private val _userHome = System.getProperty("user.home").toPath()
    private val _path = _userHome.resolve(".windea/chronicle")
    private val _imagesPath = _path.resolve("images")
    private val _imagesTempPath = _imagesPath.resolve("_tmp")
    private val _lintResultsPath = _path.resolve("lint-results")
    private val _toolsPath = _path.resolve("tools")
    private val _externalToolsPath = _path.resolve("external-tools")
    private val _texconvExePath = _externalToolsPath.resolve("texconv.exe")

    override val path: Path get() = _path.withDirectoryCreated()
    override val imagesPath: Path get() = _imagesPath.withDirectoryCreated()
    override val imagesTempPath: Path get() = _imagesTempPath.withDirectoryCreated()
    override val lintResultsPath: Path get() = _lintResultsPath.withDirectoryCreated()
    override val toolsPath: Path get() = _toolsPath.withDirectoryCreated()
    override val texconvExePath: Path get() = _texconvExePath.withParentDirectoryCreated()

    private fun Path.withDirectoryCreated() = apply { PathService.ensureDirectoryCreated(this) }

    private fun Path.withParentDirectoryCreated() = apply { parent?.let { PathService.ensureDirectoryCreated(it) } }
}
