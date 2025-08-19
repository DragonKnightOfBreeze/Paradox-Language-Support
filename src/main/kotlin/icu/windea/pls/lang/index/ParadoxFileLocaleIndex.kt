package icu.windea.pls.lang.index

import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import java.util.*

/**
 * 用于索引文件的语言区域。
 *
 * 当需要从PsiFile获取语言区域时，改为使用此索引以优化性能。
 */
class ParadoxFileLocaleIndex : ScalarIndexExtension<String>() {
    override fun getName() = ParadoxIndexKeys.FileLocale

    override fun getVersion() = 72 // VERSION for 2.0.2

    override fun getIndexer(): DataIndexer<String, Void, FileContent> {
        return DataIndexer { inputData ->
            val locale = when (inputData.fileType) {
                is ParadoxLocalisationFileType -> inputData.psiFile.castOrNull<ParadoxLocalisationFile>()?.propertyList?.locale?.name.orEmpty()
                else -> ""
            }
            Collections.singletonMap(locale, null)
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
