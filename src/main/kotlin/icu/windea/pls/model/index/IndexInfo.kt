package icu.windea.pls.model.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.QueryExecutor
import icu.windea.pls.core.toPsiFile

/**
 * 索引信息。
 *
 * @property virtualFile 所属的虚拟文件。需要先通过 [bind] 绑定才能获取（使用 [QueryExecutor] 进行查询时会自动绑定）。
 * @property project 所属的项目。需要先通过 [bind] 绑定才能获取（使用 [QueryExecutor] 进行查询时会自动绑定）。
 */
sealed class IndexInfo {
    @Volatile private var _virtualFile: VirtualFile? = null
    @Volatile private var _project: Project? = null

    val virtualFile: VirtualFile? get() = _virtualFile
    val project: Project? get() = _project

    fun bind(virtualFile: VirtualFile, project: Project) {
        _virtualFile = virtualFile
        _project = project
    }

    val file: PsiFile?
        get() = project?.let { project -> virtualFile?.toPsiFile(project) }
}
