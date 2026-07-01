package icu.windea.pls.integrations.images

import icu.windea.pls.integrations.ChronicleIntegrationsBundle
import org.cef.OS

object ImageToolConstants {
    object Texconv {
        val name get() = ChronicleIntegrationsBundle.message("tool.texconv.name")
        const val url = "https://github.com/microsoft/DirectXTex/wiki/Texconv"
    }

    object Magick {
        val name get() = ChronicleIntegrationsBundle.message("tool.magick.name")
        const val url = "https://www.imagemagick.org"

        fun pathTip(): String {
            val suffix = if (OS.isWindows()) ".exe" else ""
            return "/path/to/magick$suffix"
        }
    }

    // object PaintNet {
    //     val name get() = ChronicleBundle.message("integrations.paint.net.name")
    //     const val url = "https://www.getpaint.net"
    //     const val exeFileName = "PaintDotNet.exe"
    // }
}
