package icu.windea.pls.core

import com.intellij.openapi.application.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

/**
 * 监听描述符文件或者游戏类型标记文件。
 * 如果文件内容发生变化、或者文件被创建、被删除或者被移动，导致有必要重新解析相关的文件，则要重新解析。
 */
class DescriptorOrMarkerFileListener : AsyncFileListener {
	override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier {
		return object : AsyncFileListener.ChangeApplier {
			override fun afterVfsChange() {
				for(event in events) {
					doReparse(event)
				}
			}
		}
	}
	
	private fun doReparse(event: VFileEvent) {
		//描述符文件被删除或者被移动
		//模组根目录的游戏类型标记文件被删除或者被移动
		//游戏类型变更，也要重新解析，因为folders配置可能会变更
		when {
			event is VFileCreateEvent -> {
				val fileName = event.childName
				if(!isPossibleDescriptorOrMarkerFile(fileName)) return
				val rootFile = event.parent
				val oldRootInfo = rootFile.getCopyableUserData(PlsKeys.rootInfoKey)
				if(oldRootInfo != null) {
					ParadoxRootInfo.values.remove(oldRootInfo)
					rootFile.putCopyableUserData(PlsKeys.rootInfoKey, null)
				}
				val rootInfo = ParadoxCoreHandler.resolveRootInfo(rootFile) ?: return
				if(rootInfo !is ParadoxModRootInfo) return
				runWriteAction {
					if(fileName == rootInfo.descriptorFile.name) {
						ParadoxCoreHandler.reparseFilesInRoot(rootFile)
					} else if(fileName == rootInfo.markerFile?.name && rootInfo.gameType != oldRootInfo?.gameType) {
						ParadoxCoreHandler.reparseFilesInRoot(rootFile)
					}
				}
			}
			event is VFileDeleteEvent -> {
				val file = event.file
				if(!isPossibleDescriptorOrMarkerFile(file.name)) return
				runWriteAction {
					//不能从file.parent.children中获取file，因为已经不合法
					ParadoxCoreHandler.reparseFilesInRoot(file.parent)
				}
			}
			event is VFileMoveEvent -> {
				val file = event.file
				if(!isPossibleDescriptorOrMarkerFile(file.name)) return
				runWriteAction {
					ParadoxCoreHandler.reparseFilesInRoot(event.oldParent)
				}
				val rootFile = event.newParent
				val oldRootInfo = rootFile.getCopyableUserData(PlsKeys.rootInfoKey)
				if(oldRootInfo != null) {
					ParadoxRootInfo.values.remove(oldRootInfo)
					rootFile.putCopyableUserData(PlsKeys.rootInfoKey, null)
				}
				val rootInfo = ParadoxCoreHandler.resolveRootInfo(rootFile) ?: return
				if(rootInfo !is ParadoxModRootInfo) return
				runWriteAction {
					if(file == rootInfo.descriptorFile) {
						ParadoxCoreHandler.reparseFilesInRoot(rootFile)
					} else if(file == rootInfo.markerFile && rootInfo.gameType != oldRootInfo?.gameType) {
						ParadoxCoreHandler.reparseFilesInRoot(rootFile)
					}
				}
			}
			event is VFileContentChangeEvent -> {
				val file = event.file
				if(!isPossibleDescriptorOrMarkerFile(file.name)) return
				val rootFile = file.parent ?: return
				val oldFileInfo = rootFile.getCopyableUserData(PlsKeys.rootInfoKey)
				if(oldFileInfo != null) {
					ParadoxRootInfo.values.remove(oldFileInfo)
					rootFile.putCopyableUserData(PlsKeys.rootInfoKey, null)
				}
				val rootInfo = ParadoxCoreHandler.resolveRootInfo(rootFile) ?: return
				if(rootInfo !is ParadoxModRootInfo) return
				runWriteAction {
					if(file == rootInfo.descriptorFile) {
						file.putUserData(PlsKeys.descriptorInfoKey, null) //清空描述符信息缓存
					} else if(file == rootInfo.markerFile && file.name == PlsConstants.launcherSettingsFileName) {
						ParadoxCoreHandler.reparseFilesInRoot(rootFile) //这种情况下也需要重新解析
					}
				}
			}
			event is VFilePropertyChangeEvent && event.propertyName == "name" -> {
				val file = event.file
				if(!isPossibleDescriptorOrMarkerFile(file.name)) return
				val rootFile = file.parent
				val oldRootInfo = rootFile.getCopyableUserData(PlsKeys.rootInfoKey)
				if(oldRootInfo != null) {
					ParadoxRootInfo.values.remove(oldRootInfo)
					rootFile.putCopyableUserData(PlsKeys.rootInfoKey, null)
				}
				val rootInfo = ParadoxCoreHandler.resolveRootInfo(rootFile) ?: return
				if(rootInfo !is ParadoxModRootInfo) return
				runWriteAction {
					if(file == rootInfo.descriptorFile) {
						ParadoxCoreHandler.reparseFilesInRoot(rootFile)
					} else if(file == rootInfo.markerFile && rootInfo.gameType != oldRootInfo?.gameType) {
						ParadoxCoreHandler.reparseFilesInRoot(rootFile)
					}
				}
			}
		}
	}
	
	private fun isPossibleDescriptorOrMarkerFile(fileName: String): Boolean {
		return when {
			//fileName.equals(PlsConstants.launcherSettingsFileName, true) -> true //不监听 - 不应当发生变化
			fileName.equals(PlsConstants.descriptorFileName, true) -> true
			fileName.startsWith('.') -> ParadoxGameType.resolve(fileName.drop(1)) != null
			else -> false
		}
	}
}