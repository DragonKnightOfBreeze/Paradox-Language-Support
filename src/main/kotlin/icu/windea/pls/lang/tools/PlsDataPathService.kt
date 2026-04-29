package icu.windea.pls.lang.tools

import com.intellij.openapi.components.serviceOrNull
import java.nio.file.Path

/**
 * 备注：这些路径会在访问时确保创建对应的目录或文件。
 */
interface PlsDataPathService {
    /** `~/.windea/chronicle` */
    val path: Path

    /** `{path}/images` */
    val imagesPath: Path
    /** `{path}/images/_tmp` */
    val imagesTempPath: Path
    /** `{path}/lintResults` */
    val lintResultsPath: Path
    /** `{path}/tools` */
    val toolsPath: Path

    /** `{path}/tools/texconv.exe` */
    val texconvExePath: Path

    companion object {
        @JvmStatic
        fun getInstance(): PlsDataPathService = serviceOrNull() ?: PlsDataPathServiceImpl()
    }
}
