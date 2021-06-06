package icu.windea.pls.model

import java.nio.file.*

data class ParadoxFileInfo(
	val name: String,
	val path: ParadoxPath,
	val rootPath: Path,
	val fileType: ParadoxFileType,
	val rootType: ParadoxRootType,
	val gameType: ParadoxGameType
)