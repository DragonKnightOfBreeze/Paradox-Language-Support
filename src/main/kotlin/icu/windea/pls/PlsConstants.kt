package icu.windea.pls

import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.io.*
import org.apache.commons.io.file.*
import kotlin.io.path.*

object PlsConstants {
    const val pluginId = "icu.windea.pls"

    const val pluginSettingsFileName = "paradox-language-support.xml"

    val locationClass = PlsConstants::class.java

    val utf8Bom = byteArrayOf(0xef.toByte(), 0xbb.toByte(), 0xbf.toByte())

    val scriptFileExtensions = arrayOf("txt", "gfx", "gui", "asset", "dlc", "settings")
    val localisationFileExtensions = arrayOf("yml")
    val imageFileExtensions = arrayOf("png", "dds", "tga")

    const val dummyIdentifier = "windea"

    //val eraseMarker = TextAttributes()
    //val onlyForegroundAttributesFlags = WithAttributesPresentation.AttributesFlags().withSkipBackground(true).withSkipEffects(true)

    object Strings {
        const val anonymous = "(anonymous)"
        const val unknown = "(unknown)"
        const val unresolved = "(unresolved)"

        const val typePrefix = "(type)"
        const val subtypePrefix = "(subtype)"
        const val gameRulePrefix = "(game rule)"
        const val onActionPrefix = "(on action)"
        const val enumPrefix = "(enum)"
        const val enumValuePrefix = "(enum value)"
        const val complexEnumPrefix = "(complex enum)"
        const val complexEnumValuePrefix = "(complex enum value)"
        const val dynamicValueTypePrefix = "(dynamic value type)"
        const val dynamicValuePrefix = "(dynamic value)"
        const val inlinePrefix = "(inline)"
        const val singleAliasPrefix = "(single alias)"
        const val aliasPrefix = "(alias)"
        const val linkPrefix = "(link)"
        const val localisationLinkPrefix = "(localisation link)"
        const val localisationPromotionPrefix = "(localisation promotion)"
        const val localisationCommandPrefix = "(localisation command)"
        const val modifierCategoryPrefix = "(modifier category)"
        const val modifierPrefix = "(modifier)"
        const val triggerPrefix = "(trigger)"
        const val effectPrefix = "(effect)"
        const val scopePrefix = "(scope)"
        const val scopeGroupPrefix = "(scope group)"
        const val databaseObjectTypePrefix = "(database object type)"
        const val systemScopePrefix = "(system scope)"
        const val scriptedVariablePrefix = "(scripted variable)"
        const val parameterPrefix = "(parameter)"
        const val definitionPrefix = "(definition)"
        const val relatedDefinitionPrefix = "(related definition)"
        const val relatedLocalisationPrefix = "(related localisation)"
        const val relatedImagePrefix = "(related image)"
        const val generatedModifierPrefix = "(generated modifier)"
        const val eventTypePrefix = "(event type)"
        const val definitionPropertyPrefix = "(definition property)"
        const val definitionValuePrefix = "(definition value)"
        const val propertyPrefix = "(property)"
        const val localePrefix = "(locale)"
        const val localisationPrefix = "(localisation)"
        const val localisationSyncedPrefix = "(localisation_synced)"
        const val localisationPropertyPrefix = "(localisation property)"
        const val localisationColorPrefix = "(localisation color)"
        const val localisationIconPrefix = "(localisation icon)"
        const val inlineScriptPrefix = "(inline script)"

        const val blockFolder = "{...}"
        val parameterConditionFolder = { expression: String -> "[[$expression]...]" }
        const val inlineMathFolder = "@[...]"
        const val commandFolder = "[...]"
        const val conceptCommandFolder = "['...']"
        const val conceptCommandWithTextFolder = "['...', ...]"

        const val suppressInspectionsTagName = "noinspection"
    }

    object Patterns {
        val scriptedVariableName = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()
        val localisationPropertyName = """[a-zA-Z0-9_.\-']+""".toRegex()
        val parameterName = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()

        val conceptName = """[a-zA-Z0-9_:]+""".toRegex()
    }

    object Settings {
        /** 是否需要在IDE启动后首次打开某个项目时，刷新此项目已打开的脚本文件和本地化文件 */
        const val refreshOnProjectStartup = true

        /** 渲染本地化文本时，使用的文本字体大小。 */
        const val locFontSize = 18
        /** 渲染本地化文本时，视为文本图标的图标的大小限制。 */
        const val locTextIconSizeLimit = 36

        /** 内嵌提示中的本地化文本的默认文本长度限制 */
        const val textLengthLimit = 30
        /** 内嵌提示中的本地化文本的默认图标高度限制 */
        const val iconHeightLimit = locTextIconSizeLimit

        /** 默认的封装变量的名字（执行重构与生成操作时会用到） */
        const val defaultScriptedVariableName = "var"
        /** 定义相对于脚本文件的最大深度（用于优化性能） */
        const val maxDefinitionDepth = 4
        /** 在提示信息中显示的条目的数量限制 */
        const val itemLimit = 5
    }

    object Samples {
        val cwtColorSettings by lazy { "/samples/Cwt.colorSettings.txt".toClasspathUrl(locationClass).readText() }
        val cwtCodeStyleSettings by lazy { "/samples/Cwt.codeStyleSettings.txt".toClasspathUrl(locationClass).readText() }

        val paradoxLocalisationColorSettings by lazy { "/samples/ParadoxLocalisation.colorSettings.txt".toClasspathUrl(locationClass).readText() }
        val paradoxLocalisationCodeStyleSettings by lazy { "/samples/ParadoxLocalisation.codeStyleSettings.txt".toClasspathUrl(locationClass).readText() }

        val paradoxScriptColorSettings by lazy { "/samples/ParadoxScript.colorSettings.txt".toClasspathUrl(locationClass).readText() }
        val paradoxScriptCodeStyleSettings by lazy { "/samples/ParadoxScript.codeStyleSettings.txt".toClasspathUrl(locationClass).readText() }
    }

    object Paths {
        val userHome = System.getProperty("user.home").toPath()

        val data by lazy { userHome.resolve(".pls").also { runCatchingCancelable { it.createDirectories() } } }
        val images by lazy { data.resolve("images").also { runCatchingCancelable { it.createDirectory() } } }
        val imagesTemp by lazy { images.resolve("_temp").also { runCatchingCancelable { PathUtils.cleanDirectory(it) } } }

        val texconvExe by lazy { data.resolve("texconv.exe") }
        val texconvExeClasspathUrl by lazy { "/tools/texconv.exe".toClasspathUrl(locationClass) }
        val texconvExeFile by VirtualFileProvider(texconvExe) { VfsUtil.findFileByURL(texconvExeClasspathUrl)!! }
    }
}
