package icu.windea.pls

import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.io.*
import org.apache.commons.io.file.*

object PlsConstants {
    const val pluginId = "icu.windea.pls"

    val locationClass = PlsBundle::class.java

    val utf8Bom = byteArrayOf(0xef.toByte(), 0xbb.toByte(), 0xbf.toByte())

    val scriptFileExtensions = arrayOf("txt", "gfx", "gui", "asset", "dlc", "settings")
    val localisationFileExtensions = arrayOf("yml")
    val imageFileExtensions = arrayOf("dds", "png", "tga")

    const val modDescriptorFileName = "descriptor.mod"

    const val anonymousString = "(anonymous)"
    const val unknownString = "(unknown)"
    const val unresolvedString = "(unresolved)"

    const val dummyIdentifier = "windea"

    //val eraseMarker = TextAttributes()
    //val onlyForegroundAttributesFlags = WithAttributesPresentation.AttributesFlags().withSkipBackground(true).withSkipEffects(true)

    object Settings {
        /** 默认的封装变量的名字（执行重构与生成操作时会用到） */
        const val defaultScriptedVariableName = "var"
        /** 定义相对于脚本文件的最大深度（用于优化性能） */
        const val maxDefinitionDepth = 4
        /** 在提示信息中显示的条目的数量限制 */
        const val itemLimit = 5
    }

    object Samples {
        val cwtColorSettings = "/samples/Cwt.colorSettings.txt".toClasspathUrl(locationClass).readText()
        val cwtCodeStyleSettings = "/samples/Cwt.codeStyleSettings.txt".toClasspathUrl(locationClass).readText()

        val paradoxLocalisationColorSettings = "/samples/ParadoxLocalisation.colorSettings.txt".toClasspathUrl(locationClass).readText()
        val paradoxLocalisationCodeStyleSettings = "/samples/ParadoxLocalisation.codeStyleSettings.txt".toClasspathUrl(locationClass).readText()

        val paradoxScriptColorSettings = "/samples/ParadoxScript.colorSettings.txt".toClasspathUrl(locationClass).readText()
        val paradoxScriptCodeStyleSettings = "/samples/ParadoxScript.codeStyleSettings.txt".toClasspathUrl(locationClass).readText()
    }

    object Folders {
        const val block = "{...}"
        val parameterCondition = { expression: String -> "[[$expression]...]" }
        const val inlineMath = "@[...]"
        const val command = "[...]"
        const val concept = "['...']"
        const val conceptWithText = "['...', ...]"
    }

    object Patterns {
        val scriptedVariableName = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()
        val localisationPropertyName = """[a-zA-Z0-9_.\-']+""".toRegex()
        val parameterName = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()
    }

    object Paths {
        val userHome = System.getProperty("user.home").toPath()
        val data = userHome.resolve(".pls")
        val images = data.resolve("images")

        val imagesTemp by PathAdapter(images.resolve("_temp")) { PathUtils.cleanDirectory(it) }

        val unknownPng = data.resolve("unknown.png")
        val unknownPngClasspathUrl = "/tools/unknown.png".toClasspathUrl(locationClass)
        val unknownPngFile by VirtualFileProvider(unknownPng) { VfsUtil.findFileByURL(unknownPngClasspathUrl)!! }

        val texconvExe = data.resolve("texconv.exe")
        val texconvExeClasspathUrl = "/tools/texconv.exe".toClasspathUrl(locationClass)
        val texconvExeFile by VirtualFileProvider(texconvExe) { VfsUtil.findFileByURL(texconvExeClasspathUrl)!! }
    }
}
