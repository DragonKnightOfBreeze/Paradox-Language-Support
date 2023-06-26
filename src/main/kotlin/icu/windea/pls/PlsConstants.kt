package icu.windea.pls

import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icons.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*
import java.awt.*

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
    
    //目前认为定义相对于脚本文件的最大深度是4（最多跳过3个rootKey），在索引之外的某些场合需要加上针挑
    const val maxDefinitionDepth = 4
    //在提示信息中最多显示的键的个数
    const val keysTruncateLimit = 5
    
    //val eraseMarker = TextAttributes()
    
    val onlyForegroundAttributesFlags = WithAttributesPresentation.AttributesFlags().withSkipBackground(true).withSkipEffects(true)
}

object PlsPaths {
    val userHome = System.getProperty("user.home")
    
    const val dataDirectoryName = ".pls"
    
    const val imagesDirectoryName = "images"
    const val unknownPngName = "unknown.png"
    
    val userHomePath by lazy { userHome.toPath() }
    val dataDirectoryPath by lazy { userHomePath.resolve(dataDirectoryName) }
    val imagesDirectoryPath by lazy { dataDirectoryPath.resolve(imagesDirectoryName) }
    val unknownPngPath by lazy { imagesDirectoryPath.resolve(unknownPngName) }
    val unknownPngUrl by lazy { unknownPngPath.toUri().toURL() }
    
    val unknownPngClasspathUrl = "/$unknownPngName".toClasspathUrl()
    
    const val tmpDirectoryName = "tmp"
    val tmpDirectoryPath by lazy { dataDirectoryPath.resolve(tmpDirectoryName) }
}

object PlsPatterns {
    val scriptParameterNameRegex = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()
    val scriptedVariableNameRegex = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()
    
    val localisationPropertyNameRegex = """[a-zA-Z0-9_.\-']+""".toRegex()
}
