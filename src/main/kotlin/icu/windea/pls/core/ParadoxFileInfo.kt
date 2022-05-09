package icu.windea.pls.core

import com.intellij.openapi.vfs.*
import java.nio.file.*

/**
 * @param root 游戏或模组根目录
 * @param descriptor 描述符文件（descriptor.mod或launcher-settings.json）
 */
class ParadoxFileInfo(
	val name: String,
	val path: ParadoxPath,
	val root: VirtualFile?,
	val descriptor: VirtualFile?,
	val fileType: ParadoxFileType,
	val rootType: ParadoxRootType,
	val gameType: ParadoxGameType
) {
	val rootPath: Path? = root?.toNioPath()
}