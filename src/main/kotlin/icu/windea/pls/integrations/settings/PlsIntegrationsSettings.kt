package icu.windea.pls.integrations.settings

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.annotations.Property
import com.intellij.util.xmlb.annotations.Tag
import icu.windea.pls.integrations.lints.PlsTigerLintManager
import icu.windea.pls.integrations.lints.PlsTigerLintResult.Confidence
import icu.windea.pls.integrations.lints.PlsTigerLintResult.Severity
import icu.windea.pls.model.constants.PlsConstants

/**
 * PLS集成设置。可以在插件的对应设置页面中进行配置。
 */
@Service(Service.Level.APP)
@State(name = "PlsIntegrationsSettings", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class PlsIntegrationsSettings : SimplePersistentStateComponent<PlsIntegrationsSettingsState>(PlsIntegrationsSettingsState())

/**
 * @property image 图片工具的设置。
 * @property lint 检查工具的设置。
 */
class PlsIntegrationsSettingsState : BaseState() {
    @get:Property(surroundWithTag = false)
    var image by property(ImageState())
    // @get:FromProperty(surroundWithTag = false)
    // var translation by property(TranslationState())
    @get:Property(surroundWithTag = false)
    var lint by property(LintState())

    /**
     * @property enableTexconv 是否使用 <a href="https://github.com/microsoft/DirectXTex/wiki/Texconv">Texconv</a> 作为图片处理工具。
     * @property enableMagick 是否使用 <a href="https://www.imagemagick.org">Image Magick</a> 作为图片处理工具。
     * @property magickPath Image Magick 的可执行文件的路径。
     */
    @Tag("image")
    class ImageState : BaseState() {
        var enableTexconv by property(false)
        var enableMagick by property(false)
        var magickPath by string() // e.g., /path/to/magick.exe
    }

    // 目前没有需要配置的项
    // @Tag("translation")
    // class TranslationState: BaseState() {
    //
    // }

    /**
     * @property enableTiger 是否启用 <a href="https://github.com/amtep/tiger">Tiger</a> 检查工具。
     * @property ck3TigerPath ck3-tiger 的可执行文件的路径。
     * @property ck3TigerConfPath ck3-tiger 的.conf配置文件的路径。如果不指定，默认使用模组目录下的`ck3-tiger.conf`。
     * @property irTigerPath imperator-tiger 的可执行文件的路径。
     * @property irTigerConfPath imperator-tiger 的.conf配置文件的路径。如果不指定，则使用模组目录下的`imperator-tiger.conf`。
     * @property vic3TigerPath vic3-tiger 的可执行文件的路径。
     * @property vic3TigerConfPath vic3-tiger 的.conf配置文件的路径。如果不指定，则使用模组目录下的`vic3-tiger.conf`。
     */
    @Tag("lint")
    class LintState : BaseState() {
        var enableTiger by property(false)
        var ck3TigerPath by string() // e.g., /path/to/ck3-tiger
        var ck3TigerConfPath by string() // absolute or relative to mod path
        var irTigerPath by string() // e.g., /path/to/imperator-tiger
        var irTigerConfPath by string() // absolute or relative to mod path
        var vic3TigerPath by string() // e.g., /path/to/vic3-tiger
        var vic3TigerConfPath by string() // absolute or relative to mod path

        @get:Property(surroundWithTag = false)
        var tigerHighlight by property(TigerHighlightState())
    }

    /**
     * Tiger 高亮级别（IDE 严重度）的映射矩阵（Severity x Confidence）。
     *
     * 这些默认值遵循当前实现的直觉映射，并在强置信度时适度提升严重度。具体而言：
     * - TIPS/UNTIDY/WARNING/ERROR/FATAL 分别对应 INFORMATION/WEAK_WARNING/WARNING/ERROR/ERROR
     * - 对于 TIPS/UNTIDY，如果置信度为 STRONG，则提高一级
     */
    @Tag("tigerHighlight")
    class TigerHighlightState : BaseState() {
        // tips
        var tipsWeak by enum(from(Confidence.WEAK, Severity.TIPS))
        var tipsReasonable by enum(from(Confidence.REASONABLE, Severity.TIPS))
        var tipsStrong by enum(from(Confidence.STRONG, Severity.TIPS))
        // untidy
        var untidyWeak by enum(from(Confidence.WEAK, Severity.UNTIDY))
        var untidyReasonable by enum(from(Confidence.REASONABLE, Severity.UNTIDY))
        var untidyStrong by enum(from(Confidence.STRONG, Severity.UNTIDY))
        // warning
        var warningWeak by enum(from(Confidence.WEAK, Severity.WARNING))
        var warningReasonable by enum(from(Confidence.REASONABLE, Severity.WARNING))
        var warningStrong by enum(from(Confidence.STRONG, Severity.WARNING))
        // error
        var errorWeak by enum(from(Confidence.WEAK, Severity.ERROR))
        var errorReasonable by enum(from(Confidence.REASONABLE, Severity.ERROR))
        var errorStrong by enum(from(Confidence.STRONG, Severity.ERROR))
        // fatal
        var fatalWeak by enum(from(Confidence.WEAK, Severity.FATAL))
        var fatalReasonable by enum(from(Confidence.REASONABLE, Severity.FATAL))
        var fatalStrong by enum(from(Confidence.STRONG, Severity.FATAL))

        private fun from(confidence: Confidence, severity: Severity) = PlsTigerLintManager.getDefaultHighlightSeverity(confidence, severity)
    }
}

