package icu.windea.pls.integrations

import icu.windea.pls.*
import icu.windea.pls.integrations.lints.*
import icu.windea.pls.model.*
import org.cef.*

object PlsIntegrationConstants {
    object Texconv {
        val name get() = PlsBundle.message("integrations.texconv.name")
        const val url = "https://github.com/microsoft/DirectXTex/wiki/Texconv"
    }

    object Magick {
        val name get() = PlsBundle.message("integrations.magick.name")
        const val url = "https://www.imagemagick.org"

        fun pathTip(): String {
            val suffix = if(OS.isWindows()) ".exe" else ""
            return "path/to/magick$suffix"
        }
    }

    // object PaintNet {
    //     val name get() = PlsBundle.message("integrations.paint.net.name")
    //     const val url = "https://www.getpaint.net"
    //     const val exeFileName = "PaintDotNet.exe"
    // }

    object TranslationPlugin {
        const val id = "cn.yiiguxing.plugin.translate"
        val name get() = PlsBundle.message("integrations.tp.name")
        const val url = "https://github.com/yiiguxing/TranslationPlugin"
    }

    object Tiger {
        val name get() = PlsBundle.message("integrations.tiger.name")
        const val url = "https://github.com/amtep/tiger"

        fun pathTip(gameType: ParadoxGameType): String {
            val name = PlsTigerLintManager.findTigerTool(gameType)?.exePath ?: "tiger"
            val suffix = if(OS.isWindows()) ".exe" else ""
            return "path/to/$name$suffix"
        }

        fun confPathTip(gameType: ParadoxGameType): String {
            val name = PlsTigerLintManager.findTigerTool(gameType)?.exePath ?: "tiger"
            val suffix = ".conf"
            return "path/to/$name$suffix"
        }
    }
}
