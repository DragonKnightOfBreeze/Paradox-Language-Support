package icu.windea.pls.model.constants

import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.io.*
import org.apache.commons.io.file.*
import kotlin.io.path.*

object PlsPathConstants {
    val userHome = System.getProperty("user.home").toPath()

    val data by lazy { userHome.resolve(".pls").also { runCatchingCancelable { it.createDirectories() } } }
    val images by lazy { data.resolve("images").also { runCatchingCancelable { it.createDirectory() } } }
    val imagesTemp by lazy { images.resolve("_temp").also { runCatchingCancelable { PathUtils.cleanDirectory(it) } } }

    val texconvExe by lazy { data.resolve("texconv.exe") }
    val texconvExeClasspathUrl by lazy { "/tools/texconv.exe".toClasspathUrl() }
    val texconvExeFile by VirtualFileProvider(texconvExe) { VfsUtil.findFileByURL(texconvExeClasspathUrl)!! }
}
