package icu.windea.pls.integrations

import icu.windea.pls.*

object PlsIntegrationConstants {
    object Texconv {
        val name get() = PlsBundle.message("integrations.texconv.name")
        const val url = "https://github.com/microsoft/DirectXTex/wiki/Texconv"
    }

    object PaintNet {
        val name get() = PlsBundle.message("integrations.paint.net.name")
        const val url = "https://www.getpaint.net"
    }

    object Magick {
        val name get() = PlsBundle.message("integrations.magick.name")
        const val url = "https://www.imagemagick.org"
    }

    object TranslationPlugin {
        const val id = "cn.yiiguxing.plugin.translate"
        val name get() = PlsBundle.message("integrations.tp.name")
        const val url = "https://github.com/yiiguxing/TranslationPlugin"
    }

    object Tiger {
        val name get() = PlsBundle.message("integrations.tiger.name")
        const val url = "https://github.com/amtep/tiger"
    }
}
