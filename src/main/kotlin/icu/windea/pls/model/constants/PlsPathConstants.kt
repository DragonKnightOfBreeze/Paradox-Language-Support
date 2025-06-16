package icu.windea.pls.model.constants

import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.toClasspathUrl
import kotlinx.coroutines.*

object PlsPathConstants {
    // NOTE 仅在打开IDE后保证相关的目录、文件存在（如果不存在则自动创建），不考虑在IDE使用过程中被删除的情况
    // NOTE 不要在这里清理临时文件，而是在需要时就尽早清理，否则如果临时目录和文件过多，可能导致打开IDE后会卡住一段时间

    private val initializer = SmartInitializer()

    private val _userHome = System.getProperty("user.home").toPath()
    private val _data = _userHome.resolve(".pls")
    private val _images = _data.resolve("images")
    private val _imagesTemp = _images.resolve("_tmp")
    private val _texconvExe = _data.resolve("texconv.exe")

    val data by initializer.awaitDirectory(_data)
    val images by initializer.awaitDirectory(_images)
    val imagesTemp by initializer.await(_imagesTemp)
    val texconvExe by initializer.await(_texconvExe)

    val texconvExeFile by initializer.awaitFileFromVirtualFile(_texconvExe, "/tools/texconv.exe".toClasspathUrl())

    fun init() {
        val coroutineScope = PlsFacade.getCoroutineScope()
        coroutineScope.launch {
            initializer.initialize()
        }
    }
}
