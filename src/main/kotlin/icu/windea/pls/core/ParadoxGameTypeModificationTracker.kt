package icu.windea.pls.core

import com.google.common.cache.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import java.util.concurrent.atomic.*

/**
 * 跟踪游戏类型的更改
 */
class ParadoxGameTypeModificationTracker : ModificationTracker {
	private val count = AtomicLong()
	
	fun increment() {
		count.getAndIncrement()
	}
	
	override fun getModificationCount(): Long {
		return count.get()
	}
	
	companion object {
		private val cache: LoadingCache<VirtualFile, ParadoxGameTypeModificationTracker> = CacheBuilder.newBuilder()
			.weakKeys().buildFrom { ParadoxGameTypeModificationTracker() }
		private val mock = ParadoxGameTypeModificationTracker()
		
		fun from(file: VirtualFile): ParadoxGameTypeModificationTracker {
			val root = file.fileInfo?.rootFile ?: return mock
			return fromRoot(root)
		}
		
		fun fromRoot(root: VirtualFile): ParadoxGameTypeModificationTracker {
			return cache.get(root)
		}
	}
}