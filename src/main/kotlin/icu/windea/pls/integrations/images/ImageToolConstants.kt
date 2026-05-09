package icu.windea.pls.integrations.images

import icu.windea.pls.integrations.PlsIntegrationsBundle
import org.cef.OS

object ImageToolConstants {
    object Texconv {
        val name get() = PlsIntegrationsBundle.message("integrations.texconv.name")
        const val url = "https://github.com/microsoft/DirectXTex/wiki/Texconv"
    }

    object Magick {
        val name get() = PlsIntegrationsBundle.message("integrations.magick.name")
        const val url = "https://www.imagemagick.org"

        fun pathTip(): String {
            val suffix = if (OS.isWindows()) ".exe" else ""
            return "/path/to/magick$suffix"
        }
    }

    // object PaintNet {
    //     val name get() = PlsBundle.message("integrations.paint.net.name")
    //     const val url = "https://www.getpaint.net"
    //     const val exeFileName = "PaintDotNet.exe"
    // }
}
