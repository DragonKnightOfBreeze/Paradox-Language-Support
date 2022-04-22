package icu.windea.pls.core

import java.nio.file.*

class ParadoxFileInfo(
	val name: String,
	val path: ParadoxPath,
	val rootPath: Path,
	val fileType: ParadoxFileType,
	val rootType: ParadoxRootType,
	val gameType: ParadoxGameType
)