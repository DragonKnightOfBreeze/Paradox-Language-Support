package icu.windea.pls.model.constants

import icu.windea.pls.*
import icu.windea.pls.core.*
import kotlinx.coroutines.*

object PlsPathConstants {
    // NOTE 仅在打开IDE后保证相关的目录、文件存在（如果不存在则自动创建），不考虑在IDE使用过程中被删除的情况
    // NOTE 不要在这里清理临时文件，而是在需要时就尽早清理，否则如果临时目录和文件过多，可能导致打开IDE后会卡住一段时间

    private val initializer = SmartInitializer()

    private val _userHome = System.getProperty("user.home").toPath()
    private val _data = _userHome.resolve(".pls")

    val data by initializer.awaitDirectory(_data)
    val images by initializer.await(_data.resolve("images"))
    val imagesTemp by initializer.await(_data.resolve("images").resolve("_tmp"))
    val lintResults by initializer.await(_data.resolve("lint-results"))
    val texconvExe by initializer.await(_data.resolve("texconv.exe"))
    val texconvExeFile by initializer.awaitFileFromVirtualFile(_data.resolve("texconv.exe"), "/tools/texconv.exe".toClasspathUrl())

    fun init() {
        val coroutineScope = PlsFacade.getCoroutineScope()
        coroutineScope.launch {
            initializer.initialize()
        }
    }
}
