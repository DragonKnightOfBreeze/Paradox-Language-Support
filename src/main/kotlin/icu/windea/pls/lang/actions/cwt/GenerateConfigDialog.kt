package icu.windea.pls.lang.actions.cwt

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.util.generators.CwtConfigGenerator
import icu.windea.pls.model.ParadoxGameType
import java.nio.file.Paths
import javax.swing.JComponent

class GenerateConfigDialog(
    project: Project,
    private val generator: CwtConfigGenerator,
) : DialogWrapper(project) {
    init {
        title = PlsBundle.message("config.generation.dialog.title", generator.getName())
        init()
    }

    var gameType: ParadoxGameType? = null
    var inputPath: String? = null
    var outputPath: String? = null

    private lateinit var gameTypeCell: Cell<ComboBox<ParadoxGameType>>
    private lateinit var inputPathCell: Cell<JBTextField>
    private lateinit var outputPathCell: Cell<JBTextField>

    override fun createCenterPanel(): JComponent = panel {
        val allGameTypes = ParadoxGameType.getAll()
        val defaultGameType = allGameTypes.firstOrNull()

        row(PlsBundle.message("config.generation.dialog.field.gameType")) {
            gameTypeCell = comboBox(allGameTypes)
                .applyToComponent {
                    selectedItem = defaultGameType
                }
        }

        row(PlsBundle.message("config.generation.dialog.field.inputPath")) {
            inputPathCell = textField()
                .comment(PlsBundle.message("config.generation.dialog.field.inputPath.comment"))
                .applyToComponent {
                    toolTipText = PlsBundle.message("config.generation.dialog.field.inputPath.tip")
                }
                .align(AlignX.FILL)
        }

        row(PlsBundle.message("config.generation.dialog.field.outputPath")) {
            outputPathCell = textField()
                .applyToComponent {
                    text = generator.getGeneratedFileName()
                }
                .align(AlignX.FILL)
        }
    }

    override fun doValidateAll(): MutableList<ValidationInfo> {
        val errors = mutableListOf<ValidationInfo>()

        val gt = (gameTypeCell.component.selectedItem as? ParadoxGameType)
        if (gt == null) {
            errors += ValidationInfo(PlsBundle.message("config.generation.dialog.validation.gameType"))
        }

        val inPath = inputPathCell.component.text.trim()
        if (inPath.isEmpty()) {
            errors += ValidationInfo(PlsBundle.message("config.generation.dialog.validation.inputPath.empty"), inputPathCell.component)
        } else {
            // 允许：绝对路径的文件/目录；或相对路径（视为脚本目录的相对路径）
            val p = runCatching { Paths.get(inPath) }.getOrNull()
            if (p != null && p.isAbsolute) {
                val vf = VfsUtil.findFile(p, false)
                if (vf == null || !vf.exists()) {
                    errors += ValidationInfo(PlsBundle.message("config.generation.dialog.validation.inputPath.notExists"), inputPathCell.component)
                }
            }
        }

        val outPath = outputPathCell.component.text.trim()
        if (outPath.isEmpty()) {
            errors += ValidationInfo(PlsBundle.message("config.generation.dialog.validation.outputPath.empty"), outputPathCell.component)
        } else {
            val p = runCatching { Paths.get(outPath) }.getOrNull()
            if (p == null || !p.isAbsolute) {
                errors += ValidationInfo(PlsBundle.message("config.generation.dialog.validation.outputPath.absolute"), outputPathCell.component)
            }
        }

        return errors
    }

    override fun doOKAction() {
        // 回填参数
        gameType = gameTypeCell.component.selectedItem as? ParadoxGameType
        inputPath = inputPathCell.component.text.trim()
        outputPath = outputPathCell.component.text.trim()
        super.doOKAction()
    }
}
