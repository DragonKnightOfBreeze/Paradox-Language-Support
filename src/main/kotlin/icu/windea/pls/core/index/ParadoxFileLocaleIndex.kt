package icu.windea.pls.core.index

import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import java.util.*

private val NAME = ID.create<String, Void>("paradox.file.locale.index")
private const val VERSION = 32 //1.1.3

/**
 * 用于索引文件的语言区域。
 *
 * 当需要从PsiFile获取语言区域时，改为使用此索引以优化性能。
 */
class ParadoxFileLocaleIndex : ScalarIndexExtension<String>() {
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun getIndexer(): DataIndexer<String, Void, FileContent> {
        return DataIndexer { inputData ->
            val locale = when(inputData.fileType) {
                ParadoxLocalisationFileType -> inputData.psiFile.castOrNull<ParadoxLocalisationFile>()?.propertyList?.locale?.name.orEmpty()
                else -> ""
            }
            Collections.singletonMap<String, Void>(locale, null)
        }
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { true }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return true
    }
}