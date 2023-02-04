package icu.windea.pls.lang.model

import com.google.common.cache.*
import icu.windea.pls.core.*

/**
 * 文件或目录相对于游戏或模组根路径的路径。保留大小写。
 *
 * 示例：
 * * `common/buildings/00_capital_buildings.txt`
 * * `localisation/simp_chinese/l_simp_chinese.yml`
 *
 * @property path 使用"/"分割的路径（保留括起的双引号）。
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
		private val cache: LoadingCache<String, ParadoxPath> = CacheBuilder.newBuilder().buildCache {
			if(it.isEmpty()) EmptyParadoxPath else ParadoxPathImpl(it)
		}
		
		fun resolve(path: String): ParadoxPath {
			return cache[path]
		}
		
		fun resolve(subPaths: List<String>): ParadoxPath {
			return cache[subPaths.joinToString("/")]
		}
	}
}

class ParadoxPathImpl(
	override val path: String
) : ParadoxPath {
	override val subPaths = path.split('/')
	override val parent = path.substringBeforeLast('/', "")
	override val root = path.substringBefore('/', "")
	override val fileName = subPaths.lastOrNull().orEmpty()
	override val fileExtension = fileName.substringAfterLast('.', "")
	override val length = subPaths.size
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxPathImpl && path == other.path
	}
	
	override fun hashCode(): Int {
		return path.hashCode()
	}
	
	override fun toString(): String {
		return path
	}
}

object EmptyParadoxPath : ParadoxPath {
	override val path: String = ""
	override val subPaths: List<String> = emptyList()
	override val parent: String = ""
	override val root: String = ""
	override val fileName: String = ""
	override val fileExtension: String = ""
	override val length: Int = 0
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxPathImpl && path == other.path
	}
	
	override fun hashCode(): Int {
		return path.hashCode()
	}
	
	override fun toString(): String {
		return path
	}
}

fun ParadoxPath.canBeScriptFilePath(): Boolean {
	return !canBeLocalisationFilePath()
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