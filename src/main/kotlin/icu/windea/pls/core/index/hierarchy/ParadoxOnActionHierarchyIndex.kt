package icu.windea.pls.core.index.hierarchy

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.*
import com.intellij.util.gist.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import java.io.*

object ParadoxOnActionHierarchyIndex {
    private const val ID = "paradox.onAction.hierarchy.index"
    private const val VERSION = 22 //1.0.0
    
    class Data(
        val onActionInfos: MutableList<OnActionInfo> = SmartList()
    ) {
        val eventToOnActionsMap by lazy {
            buildMap<String, Set<String>> {
                onActionInfos.forEachFast { onActionInfo ->
                    onActionInfo.eventInvocationInfos.forEachFast { eventInvocationInfo ->
                        val set = getOrPut(eventInvocationInfo.name) { mutableSetOf() } as MutableSet
                        set.add(onActionInfo.name)
                    }
                }
            }
        }
    }
    
    class OnActionInfo(
        val name: String,
        val eventInvocationInfos: MutableList<EventInvocationInfo> = SmartList()
    )
    
    @JvmInline
    value class EventInvocationInfo(
        val name: String
    )
    
    private val EmptyData = Data()
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            DataInputOutputUtil.writeSeq(storage, value.onActionInfos) { onActionInfo ->
                IOUtil.writeUTF(storage, onActionInfo.name)
                DataInputOutputUtil.writeSeq(storage, onActionInfo.eventInvocationInfos) { eventInvocationInfo ->
                    IOUtil.writeUTF(storage, eventInvocationInfo.name)
                }
            }
        }
        
        override fun read(storage: DataInput): Data {
            val onActionInfos = DataInputOutputUtil.readSeq(storage) {
                OnActionInfo(
                    name = IOUtil.readUTF(storage),
                    eventInvocationInfos = DataInputOutputUtil.readSeq(storage) {
                        EventInvocationInfo(
                            name = IOUtil.readUTF(storage)
                        )
                    }
                )
            }
            if(onActionInfos.isEmpty()) return EmptyData
            return Data(onActionInfos)
        }
    }
    
    private val gist: VirtualFileGist<Data> = GistManager.getInstance().newVirtualFileGist(ID, VERSION, valueExternalizer) builder@{ project, file ->
        if(file.fileInfo == null) return@builder EmptyData
        if(!matchesPath(file)) return@builder EmptyData
        val psiFile = file.toPsiFile(project) ?: return@builder EmptyData
        if(psiFile !is ParadoxScriptFile) return@builder EmptyData
        val data = Data()
        var currentOnActionInfo: OnActionInfo? = null
        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            //目前暂不支持需要进行内联的情况
            
            override fun visitElement(element: PsiElement) {
                run {
                    if(element is ParadoxScriptProperty && element.parent is ParadoxScriptRootBlock) {
                        val onActionName = element.name
                        if(onActionName.isParameterized()) return@run
                        val onActionInfo = OnActionInfo(onActionName)
                        data.onActionInfos.add(onActionInfo)
                        currentOnActionInfo = onActionInfo
                    }
                }
                run {
                    if(element is ParadoxScriptStringExpressionElement) {
                        val expression = element.value
                        if(expression.isParameterized()) return@run
                        //这里直接使用静态匹配即可
                        val configs = ParadoxConfigHandler.getConfigs(element, matchOptions = ParadoxConfigMatcher.Options.StaticMatch)
                        if(configs.any { isEventDefinitionConfig(it) }) {
                            val eventInvocationInfo = EventInvocationInfo(expression)
                            currentOnActionInfo?.eventInvocationInfos?.add(eventInvocationInfo)
                        }
                    }
                }
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
            
            private fun isEventDefinitionConfig(config: CwtDataConfig<*>): Boolean {
                return config.expression.type == CwtDataType.Definition && config.expression.value.let { it != null && (it == "event" || it.startsWith("event")) }
            }
        })
        data
    }
    
    private fun matchesPath(file: VirtualFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        val path = fileInfo.pathToEntry //这里使用pathToEntry
        return "common/on_actions".matchesPath(path.path)
    }
    
    fun getData(file: VirtualFile, project: Project): Data? {
        return gist.getFileData(project, file)
    }
}

