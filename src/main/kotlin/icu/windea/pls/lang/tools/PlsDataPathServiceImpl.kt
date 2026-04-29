package icu.windea.pls.lang.tools

import icu.windea.pls.core.io.PathService
import icu.windea.pls.core.toPath
import java.nio.file.Path

class PlsDataPathServiceImpl : PlsDataPathService {
    // NOTE 2.1.8 改为在需要时才创建目录或文件（不需要经过VFS，不需要在打开应用时异步初始化，应当不会有什么耗时）

    private val _path = System.getProperty("user.home").toPath().resolve(".windea/chronicle")
    private val _imagesPath = _path.resolve("images")
    private val _imagesTempPath = _imagesPath.resolve("_tmp")
    private val _lintResultsPath = _path.resolve("lint-results")
    private val _toolsPath = _path.resolve("tools")

    private val _texconvExePath = _toolsPath.resolve("texconv.exe")

    override val path: Path get() = _path.withDirectoryCreated()
    override val imagesPath: Path get() = _imagesPath.withDirectoryCreated()
    override val imagesTempPath: Path get() = _imagesTempPath.withDirectoryCreated()
    override val lintResultsPath: Path get() = _lintResultsPath.withDirectoryCreated()
    override val toolsPath: Path get() = _toolsPath.withDirectoryCreated()

    override val texconvExePath: Path get() = _texconvExePath.withFileCreatedFromClasspath("/tools/texconv.exe")

    private fun Path.withDirectoryCreated() = apply { PathService.ensureDirectoryCreated(this) }

    private fun Path.withFileCreatedFromClasspath(url: String) = apply { PathService.ensureFileCreatedFromClasspath(this, url) }
}
