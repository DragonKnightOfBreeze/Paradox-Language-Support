package icu.windea.pls.lang.index

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ScalarIndexExtension
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import icu.windea.pls.core.castOrNull
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import java.util.*

/**
 * 用于索引文件的语言环境。
 *
 * 当需要从PsiFile获取语言环境时，改为使用此索引以优化性能。
 */
class ParadoxFileLocaleIndex : ScalarIndexExtension<String>() {
    override fun getName() = ParadoxIndexKeys.FileLocale

    override fun getVersion() = 75 // VERSION for 2.0.5

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
