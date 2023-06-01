package icu.windea.pls.lang.model

/**
 * 文件或目录相对于游戏或模组根路径的路径。保留大小写。
 *
 * 示例：
 * * `common/buildings/00_capital_buildings.txt`
 * * `localisation/simp_chinese/l_simp_chinese.yml`
 *
 * @property path 使用"/"分割的路径。
 */
interface ParadoxPath : Iterable<String> {
	val path: String
	val subPaths: List<String>
	val parent: String
	val root: String
	val fileName: String
	val fileExtension: String
	val length: Int
	
	fun isEmpty(): Boolean = length == 0
	
	fun isNotEmpty(): Boolean = length != 0
	
	override fun iterator(): Iterator<String> = subPaths.iterator()
	
	override fun equals(other: Any?): Boolean
	
	override fun hashCode(): Int
	
	override fun toString(): String
	
	companion object Resolver {
		fun resolve(path: String): ParadoxPath {
			return ParadoxPathImplA(path)
		}
		
		fun resolve(subPaths: List<String>): ParadoxPath {
			return ParadoxPathImplB(subPaths)
		}
	}
}

fun ParadoxPath.canBeScriptFilePath(): Boolean {
	return length > 1 && !canBeLocalisationFilePath()
}

fun ParadoxPath.canBeLocalisationFilePath(): Boolean {
	return canBeLocalisationPath() || canBeSyncedLocalisationPath()
}

fun ParadoxPath.canBeLocalisationPath(): Boolean {
	return root == "localisation" || root == "localization"
}

fun ParadoxPath.canBeSyncedLocalisationPath(): Boolean {
	return root == "localisation_synced" || root == "localization_synced"
}

class ParadoxPathImplA(
	override val path: String
) : ParadoxPath {
	override val subPaths: List<String> = path.split('/')
	override val parent: String = path.substringBeforeLast('/', "")
	override val root: String = path.substringBefore('/', "")
	override val fileName: String = subPaths.lastOrNull().orEmpty()
	override val fileExtension: String = fileName.substringAfterLast('.', "")
	override val length: Int = subPaths.size
	
	override fun equals(other: Any?) = this === other || other is ParadoxPath && path == other.path
	override fun hashCode() = path.hashCode()
	override fun toString() = path
}

class ParadoxPathImplB(
	subPaths: List<String>
) : ParadoxPath {
	override val subPaths: List<String> = subPaths
	override val path: String = subPaths.joinToString("/")
	override val parent: String = path.substringBeforeLast('/', "")
	override val root: String = path.substringBefore('/', "")
	override val fileName: String = subPaths.lastOrNull().orEmpty()
	override val fileExtension: String = fileName.substringAfterLast('.', "")
	override val length: Int = subPaths.size
	
	override fun equals(other: Any?) = this === other || other is ParadoxPath && path == other.path
	override fun hashCode() = path.hashCode()
	override fun toString() = path
}

object EmptyParadoxPath : ParadoxPath {
	override val path: String = ""
	override val subPaths: List<String> = emptyList()
	override val parent: String = ""
	override val root: String = ""
	override val fileName: String = ""
	override val fileExtension: String = ""
	override val length: Int = 0
	
	override fun equals(other: Any?) = this === other || other is ParadoxPath && path == other.path
	override fun hashCode() = path.hashCode()
	override fun toString() = path
}
