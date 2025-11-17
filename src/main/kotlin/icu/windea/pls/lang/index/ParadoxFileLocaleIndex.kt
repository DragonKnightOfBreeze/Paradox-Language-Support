package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.PsiDependentFileContent
import com.intellij.util.indexing.ScalarIndexExtension
import com.intellij.util.io.EnumeratorStringDescriptor
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationLightTreeUtil
import java.util.*

/**
 * 文件的语言环境的索引。
 *
 * 当需要从 [VirtualFile] 得到语言环境时，改为使用此索引以优化性能。
 */
class ParadoxFileLocaleIndex : ScalarIndexExtension<String>() {
    private val inputFilter = FileBasedIndex.InputFilter { true }
    private val indexer = DataIndexer<String, Void, FileContent> { indexData(it) }
    private val keyDescriptor = EnumeratorStringDescriptor.INSTANCE

    override fun getName() = PlsIndexKeys.FileLocale

    override fun getVersion() = PlsIndexVersions.FileLocale

    override fun getInputFilter() = inputFilter

    override fun dependsOnFileContent() = true

    override fun getIndexer() = indexer

    override fun getKeyDescriptor() = keyDescriptor

    private fun indexData(inputData: FileContent): Map<String, Void?> {
        if (inputData.fileType !is ParadoxLocalisationFileType) return emptyMap()
        if (inputData is PsiDependentFileContent) {
            // use lighter AST if possible to optimize performance
            val tree = inputData.lighterAST
            val locale = ParadoxLocalisationLightTreeUtil.getLocaleFromRootNode(tree)
            if (locale.isNotNullOrEmpty()) return emptyMap()
            return Collections.singletonMap(locale, null)
        } else {
            // fallback to PSI
            val psiFile = inputData.psiFile
            if (psiFile !is ParadoxLocalisationFile) return emptyMap()
            val locale = psiFile.propertyList?.locale?.name
            if (locale.isNullOrEmpty()) return emptyMap()
            return Collections.singletonMap(locale, null)
        }
    }
}
