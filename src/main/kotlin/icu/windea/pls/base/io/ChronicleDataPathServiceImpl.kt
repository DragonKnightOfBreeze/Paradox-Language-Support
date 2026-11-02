package icu.windea.pls.base.io

import icu.windea.pls.core.util.io.PathService
import icu.windea.pls.core.util.io.toPath
import java.nio.file.Path

class ChronicleDataPathServiceImpl : ChronicleDataPathService {
    // NOTE 2.1.8 改为在需要时才创建目录或文件（不需要经过 VFS，不需要在打开应用时异步初始化，应当不会有什么耗时）

    private val _userHome = System.getProperty("user.home").toPath()
    private val _path = _userHome.resolve(".windea/chronicle")
    private val _imagesPath = _path.resolve("images")
    private val _imagesTempPath = _imagesPath.resolve("_tmp")
    private val _lintResultsPath = _path.resolve("lint-results")
    private val _externalToolsPath = _path.resolve("external-tools")
    private val _texconvExePath = _externalToolsPath.resolve("texconv.exe")

    override val path: Path get() = _path.also { PathService.ensureDirectoryCreated(it) }
    override val imagesPath: Path get() = _imagesPath.also { PathService.ensureDirectoryCreated(it) }
    override val imagesTempPath: Path get() = _imagesTempPath.also { PathService.ensureDirectoryCreated(it) }
    override val lintResultsPath: Path get() = _lintResultsPath.also { PathService.ensureDirectoryCreated(it) }
    override val externalToolsPath: Path get() = _externalToolsPath.also { PathService.ensureDirectoryCreated(it) }
    override val texconvExePath: Path get() = _texconvExePath.also { PathService.ensureParentDirectoryCreated(it) }

    override fun ensureTexconvExePath() {
        PathService.ensureFileCreatedFromClasspath(_texconvExePath, "/external-tools/texconv.exe")
    }
}
