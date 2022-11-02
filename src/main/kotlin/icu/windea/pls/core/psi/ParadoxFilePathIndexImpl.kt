package icu.windea.pls.core.psi

import com.intellij.openapi.util.registry.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.core.*
import java.util.*

class ParadoxFilePathIndexImpl : ScalarIndexExtension<String>() {
	override fun getName(): ID<String, Void> {
		return ParadoxFilePathIndex.name
	}
	
	override fun getIndexer(): DataIndexer<String, Void, FileContent> {
		return DataIndexer { inputData ->
			val path = inputData.file.fileInfo?.path?.path
			if(path == null) emptyMap() else Collections.singletonMap(path, null)
		}
	}
	
	override fun getKeyDescriptor(): KeyDescriptor<String> {
		return EnumeratorStringDescriptor.INSTANCE
	}
	
	override fun getVersion(): Int {
		return 3 + (if(Registry.`is`("indexing.paradox.file.path.over.vfs")) 0xff else 0)
	}
	
	override fun getInputFilter(): FileBasedIndex.InputFilter {
		return FileBasedIndex.InputFilter { true }
	}
	
	override fun dependsOnFileContent(): Boolean {
		return false
	}
	
	override fun indexDirectories(): Boolean {
		return false
	}
}