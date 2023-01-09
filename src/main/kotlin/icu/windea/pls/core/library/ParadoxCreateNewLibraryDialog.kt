package icu.windea.pls.core.library

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.config.core.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*

/**
 * 创建新的库（游戏目录/模组目录）的对话框。
 */
class ParadoxCreateNewLibraryDialog(
	private val project: Project,
	contextDirectory: VirtualFile? = null,
	gameType: ParadoxGameType = getSettings().defaultGameType,
	rootType: ParadoxRootType = ParadoxRootType.Game
) : DialogWrapper(project, true) {
	var rootFile: VirtualFile? = contextDirectory ?: project.guessProjectDir()
	var rootInfo: ParadoxRootInfo? = null
	
	private val propertyGraph = PropertyGraph()
	private val gameTypeProperty = propertyGraph.property(gameType)
		.apply {
			afterChange { gameType ->
				descriptor?.title = PlsBundle.message("library.dialog.createNewLibrary.libraryPath.browseDialogTitle", rootType, gameType)
			}
		}
	private val rootTypeProperty = propertyGraph.property(rootType)
	private val rootFilePathProperty = propertyGraph.property(contextDirectory?.path.orEmpty())
	
	var gameType by gameTypeProperty
	var rootType by rootTypeProperty
	var rootFilePath by rootFilePathProperty
	
	var descriptor: FileChooserDescriptor? = null
	
	init {
		title = PlsBundle.message("library.dialog.createNewLibrary.title")
		init()
	}
	
	//（下拉框）游戏类型  （下拉框）根类型
	//（文件选择框）游戏或模组（根）目录
	
	override fun createCenterPanel() = panel {
		row {
			label(PlsBundle.message("library.dialog.createNewLibrary.gameType")).widthGroup("left")
			comboBox(ParadoxGameType.valueList).bindItem(dialog.gameTypeProperty)
			
			label(PlsBundle.message("library.dialog.createNewLibrary.rootType"))
			comboBox(listOf(ParadoxRootType.Game, ParadoxRootType.Mod)).bindItem(dialog.rootTypeProperty)
		}
		row {
			label(PlsBundle.message("library.dialog.createNewLibrary.libraryPath")).widthGroup("left")
			val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
				.withTitle(PlsBundle.message("library.dialog.createNewLibrary.libraryPath.browseDialogTitle", rootType, gameType))
				.apply { putUserData(PlsDataKeys.gameTypePropertyKey, gameTypeProperty) }
				.apply { putUserData(PlsDataKeys.rootTypePropertyKey, rootTypeProperty) }
				.apply { descriptor = this }
			textFieldWithBrowseButton(null, project, descriptor) { it.path }
				.bindText(dialog.rootFilePathProperty)
				.horizontalAlign(HorizontalAlign.FILL)
				.resizableColumn()
				.validationOnApply { validateLibraryPath() }
		}
		row {
			pathCompletionShortcutComment()
		}
	}.apply {
		withPreferredWidth(width * 2) //2倍宽度 - 基于调试结果
	}
	
	private fun ValidationInfoBuilder.validateLibraryPath(): ValidationInfo? {
		//验证库的路径是否合法
		//存在，不在项目中，拥有对应的rootInfo和descriptorInfo，且匹配gameType和rootType
		//NOTE 仍然可以添加路径重复的库（相对于其他库来说）
		val rootFile = VfsUtil.findFile(rootFilePath.toPath(), false)
			?.takeIf { it.exists() }
			?: return error(PlsBundle.message("library.dialog.createNewLibrary.libraryPath.invalid.0"))
		project.guessProjectDir()?.let { projectDir ->
			if(VfsUtilCore.isAncestor(projectDir, rootFile, false)) {
				return error(PlsBundle.message("library.dialog.createNewLibrary.libraryPath.invalid.4"))
			}
		}
		val rootInfo = ParadoxCoreHandler.resolveRootInfo(rootFile, false)
		val descriptorInfo = rootInfo?.descriptorInfo
		if(rootInfo != null && descriptorInfo != null && gameType == rootInfo.gameType && rootType == rootInfo.rootType) {
			this@ParadoxCreateNewLibraryDialog.rootFile = rootFile
			this@ParadoxCreateNewLibraryDialog.rootInfo = rootInfo
			return null
		}
		when(rootType) {
			ParadoxRootType.Mod -> return error(PlsBundle.message("library.dialog.createNewLibrary.libraryPath.invalid.1", gameType.description))
			ParadoxRootType.Game -> return error(PlsBundle.message("library.dialog.createNewLibrary.libraryPath.invalid.2", gameType.description))
			else -> return error(PlsBundle.message("library.dialog.createNewLibrary.libraryPath.invalid.3", gameType.description))
		}
	}
}
