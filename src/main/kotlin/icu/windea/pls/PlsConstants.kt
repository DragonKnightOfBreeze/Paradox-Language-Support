package icu.windea.pls

import icons.*
import icu.windea.pls.core.*

object PlsConstants {
    val locationClass = PlsIcons::class.java
    
    val cwtColorSettingsDemoText = "/demoText/Cwt.colorSettings.txt".toClasspathUrl().readText()
    val cwtCodeStyleSettingsDemoText = "/demoText/Cwt.codeStyleSettings.txt".toClasspathUrl().readText()
    
    val paradoxLocalisationColorSettingsDemoText = "/demoText/ParadoxLocalisation.colorSettings.txt".toClasspathUrl().readText()
    val paradoxLocalisationCodeStyleSettingsDemoText = "/demoText/ParadoxLocalisation.codeStyleSettings.txt".toClasspathUrl().readText()
    
    val paradoxScriptColorSettingsDemoText = "/demoText/ParadoxScript.colorSettings.txt".toClasspathUrl().readText()
    val paradoxScriptCodeStyleSettingsDemoText = "/demoText/ParadoxScript.codeStyleSettings.txt".toClasspathUrl().readText()
    
    const val dummyIdentifier = "windea"
    
    val utf8Bom = byteArrayOf(0xef.toByte(), 0xbb.toByte(), 0xbf.toByte())
    
    val scriptFileExtensions = arrayOf("txt", "gfx", "gui", "asset", "dlc", "settings")
    val localisationFileExtensions = arrayOf("yml")
    val ddsFileExtensions = arrayOf("dds")
    
    const val launcherSettingsFileName = "launcher-settings.json"
    const val descriptorFileName = "descriptor.mod"
    
    const val ellipsis = "..."
    //const val commentFolder = "#..."
    const val blockFolder = "{...}"
    fun parameterConditionFolder(expression: String) = "[[$expression]...]"
    const val inlineMathFolder = "@[...]"
    
    const val anonymousString = "(anonymous)"
    const val unknownString = "(unknown)"
    const val unresolvedString = "(unresolved)"
    
    const val defaultScriptedVariableName = "var"
    
    //定义相对于脚本文件的最大深度（目前指定为4，即最多跳过3个rootKey） - 用于优化性能
    const val maxDefinitionDepth = 4
    //在提示信息中最多显示的键的个数
    const val keysTruncateLimit = 5
    
    //val eraseMarker = TextAttributes()
    //val onlyForegroundAttributesFlags = WithAttributesPresentation.AttributesFlags().withSkipBackground(true).withSkipEffects(true)
    
    object Patterns {
        val scriptParameterNameRegex = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()
        val scriptedVariableNameRegex = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()
        val localisationPropertyNameRegex = """[a-zA-Z0-9_.\-']+""".toRegex()
    }
    
    object Paths {
        val userHome = System.getProperty("user.home")
        val userHomePath by lazy { userHome.toPath() }
        
        const val dataDirectoryName = ".pls"
        val dataDirectoryPath by lazy { userHomePath.resolve(dataDirectoryName) }
        
        const val imagesDirectoryName = "images"
        val imagesDirectoryPath by lazy { dataDirectoryPath.resolve(imagesDirectoryName) }
        
        const val unknownPngName = "unknown.png"
        val unknownPngPath by lazy { imagesDirectoryPath.resolve(unknownPngName) }
        val unknownPngUrl by lazy { unknownPngPath.toUri().toURL() }
        val unknownPngClasspathUrl = "/$unknownPngName".toClasspathUrl()
        
        const val diffDirectoryName = "diff"
        val diffDirectoryPath by lazy { dataDirectoryPath.resolve(diffDirectoryName) }
    }
}
