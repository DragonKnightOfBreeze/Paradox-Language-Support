package icu.windea.pls.integrations.images

import com.intellij.openapi.util.SystemInfo
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.integrations.ChronicleIntegrationsBundle

object ImageToolConstants {
    object Texconv {
        val name get() = ChronicleIntegrationsBundle.message("tool.texconv.name")
        const val url = "https://github.com/microsoft/DirectXTex/wiki/Texconv"

        fun pathTip(): String {
            // return "/path/to/texconv.exe" // hint only
            return ChronicleDataPathService.getInstance().texconvExePath.toString() // actual default value
        }
    }

    object Magick {
        val name get() = ChronicleIntegrationsBundle.message("tool.magick.name")
        const val url = "https://www.imagemagick.org"

        fun pathTip(): String {
            val suffix = if (SystemInfo.isWindows) ".exe" else ""
            return "/path/to/magick$suffix" // hint only
        }
    }

    // object PaintNet {
    //     val name get() = ChronicleBundle.message("integrations.paint.net.name")
    //     const val url = "https://www.getpaint.net"
    //     const val exeFileName = "PaintDotNet.exe"
    // }
}
