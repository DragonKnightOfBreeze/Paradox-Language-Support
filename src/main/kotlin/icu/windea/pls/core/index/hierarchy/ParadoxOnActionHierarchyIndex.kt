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
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import java.io.*

object ParadoxOnActionHierarchyIndex {
    private const val ID = "paradox.onAction.hierarchy.index"
    private const val VERSION = 22 //1.0.0
    
    class Data(
        val onActionInfos: MutableList<OnActionInfo> = SmartList()
    ) {
        val eventToOnActionMap by lazy {
            if(onActionInfos.isEmpty()) return@lazy emptyMap()
            buildMap<String, Set<String>> {
                onActionInfos.forEach { onActionInfo ->
                    onActionInfo.eventInfos.forEach { eventInfo ->
                        val set = getOrPut(eventInfo.name) { mutableSetOf() } as MutableSet
                        set.add(onActionInfo.name)
                    }
                }
            }
        }
    }
    
    class OnActionInfo(
        val name: String,
        val eventInfos: MutableList<EventInfo> = SmartList()
    ) 
    
    class EventInfo(
        val name: String
    )
    
    private val EmptyData = Data()
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            DataInputOutputUtil.writeSeq(storage, value.onActionInfos) { onAction ->
                IOUtil.writeUTF(storage, onAction.name)
                DataInputOutputUtil.writeSeq(storage, onAction.eventInfos) { event ->
                    IOUtil.writeUTF(storage, event.name)
                }
            }
        }
        
        override fun read(storage: DataInput): Data {
            return Data(DataInputOutputUtil.readSeq(storage) {
                OnActionInfo(
                    name = IOUtil.readUTF(storage),
                    eventInfos = DataInputOutputUtil.readSeq(storage) {
                        EventInfo(
                            name = IOUtil.readUTF(storage)
                        )
                    }
                )
            })
        }
    }
    
    private val gist: VirtualFileGist<Data> = GistManager.getInstance().newVirtualFileGist(ID, VERSION, valueExternalizer) builder@{ project, file ->
        if(file.fileInfo == null) return@builder EmptyData
        if(!matchesPath(file)) return@builder EmptyData
        val psiFile = file.toPsiFile<ParadoxScriptFile>(project) ?: return@builder EmptyData
        val data = Data()
        var currentOnActionInfo: OnActionInfo? = null 
        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptProperty && element.parent is ParadoxScriptRootBlock) {
                    val onActionName = element.name
                    if(!onActionName.isParameterized()) {
                        val onActionInfo = OnActionInfo(onActionName)
                        data.onActionInfos.add(onActionInfo)
                        currentOnActionInfo = onActionInfo
                    }
                }
                if(element is ParadoxScriptPropertyKey) {
                    val expression = element.value
                    if(!expression.isParameterized()) {
                        //这里直接使用静态匹配即可
                        val configs = ParadoxConfigHandler.getConfigs(element, matchType = CwtConfigMatchType.STATIC)
                        if(configs.any { isEventDefinitionConfig(it) }) {
                            val eventInfo = EventInfo(expression)
                            currentOnActionInfo?.eventInfos?.add(eventInfo)
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