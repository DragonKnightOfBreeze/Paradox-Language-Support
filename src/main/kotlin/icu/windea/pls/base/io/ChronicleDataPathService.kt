package icu.windea.pls.base.io

import com.intellij.openapi.components.serviceOrNull
import java.nio.file.Path

/**
 * 备注：这些路径会在访问时确保创建对应的目录或文件。
 */
interface ChronicleDataPathService {
    /** `~/.windea/chronicle` */
    val path: Path

    /** `{path}/images` */
    val imagesPath: Path
    /** `{path}/images/_tmp` */
    val imagesTempPath: Path
    /** `{path}/lintResults` */
    val lintResultsPath: Path
    /** `{path}/external-tools` */
    val externalToolsPath: Path
    /** `{path}/external-tools/texconv.exe` */
    val texconvExePath: Path

    fun ensureTexconvExePath()

    companion object {
        @JvmStatic
        fun getInstance(): ChronicleDataPathService = serviceOrNull() ?: ChronicleDataPathServiceImpl()
    }
}
