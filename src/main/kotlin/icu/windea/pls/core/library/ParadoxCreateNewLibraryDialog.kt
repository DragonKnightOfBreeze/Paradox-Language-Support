package icu.windea.pls.core.library

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import javax.swing.*

/**
 * 创建新的库（游戏目录/模组目录）的对话框。
 */
class ParadoxCreateNewLibraryDialog(
	private val project: Project,
	contextDirectory: VirtualFile? = null,
	private var gameType: ParadoxGameType = getSettings().defaultGameType,
	private var rootType: ParadoxRootType = ParadoxRootType.Game
) : DialogWrapper(project, true) {
	var rootFile: VirtualFile? = null
	var rootFilePath: String = contextDirectory?.path.orEmpty()
	var rootInfo: ParadoxRootInfo? = null
	
	init {
		title = PlsBundle.message("library.dialog.createNewLibrary.title")
		init()
	}
	
	//（下拉框）游戏类型
	//（下拉框）根类型
	//（文件选择框）游戏或模组（根）目录
	
	override fun createCenterPanel(): JComponent {
		return panel {
			val dialog = this@ParadoxCreateNewLibraryDialog
			row {
				val values = ParadoxGameType.valueList
				comboBox(values)
					.bindItem(dialog::gameType.toNullableProperty())
					.label(PlsBundle.message("library.dialog.createNewLibrary.gameType"))
			}
			row{
				val values = listOf(ParadoxRootType.Game, ParadoxRootType.Mod)
				comboBox(values)
					.bindItem(dialog::rootType.toNullableProperty())
					.label(PlsBundle.message("library.dialog.createNewLibrary.rootType"))
			}
			row {
				val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
				textFieldWithBrowseButton(PlsBundle.message("library.dialog.createNewLibrary.libraryPath.browseDialogTitle"), project, descriptor) { it.also { rootFile = it }.path }
					.bindText(dialog::rootFilePath)
					.horizontalAlign(HorizontalAlign.FILL)
					.resizableColumn()
					.validationOnApply { validateLibraryPath() }
					.label(PlsBundle.message("library.dialog.createNewLibrary.libraryPath"), LabelPosition.TOP)
			}
		}
	}
	
	private fun ValidationInfoBuilder.validateLibraryPath(): ValidationInfo {
		//验证库的路径是否合法
		val rootFile = rootFile ?: return warning(PlsBundle.message("library.dialog.createNewLibrary.libraryPath.invalid.0"))
		val rootInfo = resolveRootInfo(rootFile, false)
		val descriptorInfo = rootInfo?.descriptorInfo
		if(rootInfo != null && descriptorInfo != null) {
			this@ParadoxCreateNewLibraryDialog.rootInfo = rootInfo
		}
		when(rootType){
			ParadoxRootType.Mod -> return warning(PlsBundle.message("library.dialog.createNewLibrary.libraryPath.invalid.1", gameType.description))
			ParadoxRootType.Game -> return warning(PlsBundle.message("library.dialog.createNewLibrary.libraryPath.invalid.2", gameType.description))
			else -> return warning(PlsBundle.message("library.dialog.createNewLibrary.libraryPath.invalid.3", gameType.description))
		}
	}
}