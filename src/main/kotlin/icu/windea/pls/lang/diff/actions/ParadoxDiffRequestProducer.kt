package icu.windea.pls.lang.diff.actions

import com.intellij.diff.chains.SimpleDiffRequestChain
import com.intellij.diff.requests.DiffRequest
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

abstract class ParadoxDiffRequestProducer(
    request: DiffRequest,
    val otherFile: VirtualFile,
    val icon: Icon?,
    val isCurrent: Boolean,
) : SimpleDiffRequestChain.DiffRequestProducerWrapper(request)
