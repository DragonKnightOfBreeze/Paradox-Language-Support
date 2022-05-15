package icu.windea.pls.core

import com.intellij.openapi.vfs.*
import java.nio.file.*
import java.util.*

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
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxFileInfo
			&& path == other.path && root?.path == other.root?.path && gameType == other.gameType
	}
	
	override fun hashCode(): Int {
		return Objects.hash(path, root?.path, gameType)
	}
}