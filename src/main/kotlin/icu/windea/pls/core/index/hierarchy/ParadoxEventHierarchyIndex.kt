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
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

object ParadoxEventHierarchyIndex {
    private const val ID = "paradox.event.hierarchy.index"
    private const val VERSION = 22 //1.0.0
    
    class Data(
        val eventInfos: MutableList<EventInfo> = SmartList()
    ) {
        val eventToEventInfosMap by lazy {
            buildMap<String, Set<EventInfo>> {
                eventInfos.forEachFast { eventInfo ->
                    eventInfo.eventInvocationInfos.forEachFast { eventInvocationInfo ->
                        val set = getOrPut(eventInvocationInfo.name) { mutableSetOf() } as MutableSet
                        set.add(eventInfo)
                    }
                }
            }
        }
    }
    
    class EventInfo(
        val name: String,
        val type: String?,
        val scope: String?,
        val eventInvocationInfos: MutableList<EventInvocationInfo> = SmartList()
    )
    
    @JvmInline
    value class EventInvocationInfo(
        val name: String
    )
    
    private val EmptyData = Data()
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            DataInputOutputUtil.writeSeq(storage, value.eventInfos) { eventInfo ->
                IOUtil.writeUTF(storage, eventInfo.name)
                IOUtil.writeUTF(storage, eventInfo.type.orEmpty())
                IOUtil.writeUTF(storage, eventInfo.scope.orEmpty())
                DataInputOutputUtil.writeSeq(storage, eventInfo.eventInvocationInfos) { eventInvocationInfo ->
                    IOUtil.writeUTF(storage, eventInvocationInfo.name)
                }
            }
        }
        
        override fun read(storage: DataInput): Data {
            val eventInfos = DataInputOutputUtil.readSeq(storage) {
                EventInfo(
                    name = IOUtil.readUTF(storage),
                    type = IOUtil.readUTF(storage).takeIfNotEmpty(),
                    scope = IOUtil.readUTF(storage).takeIfNotEmpty(),
                    eventInvocationInfos = DataInputOutputUtil.readSeq(storage) {
                        EventInvocationInfo(
                            name = IOUtil.readUTF(storage)
                        )
                    }
                )
            }
            if(eventInfos.isEmpty()) return EmptyData
            return Data(eventInfos)
        }
    }
    
    private val gist: VirtualFileGist<Data> = GistManager.getInstance().newVirtualFileGist(ID, VERSION, valueExternalizer) builder@{ project, file ->
        if(file.fileInfo == null) return@builder EmptyData
        if(file.fileType != ParadoxScriptFileType) return@builder EmptyData
        if(!matchesPath(file)) return@builder EmptyData
        val psiFile = file.toPsiFile(project) ?: return@builder EmptyData
        val data = Data()
        var currentEventInfo: EventInfo? = null
        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            //目前暂不支持需要进行内联的情况
            
            override fun visitElement(element: PsiElement) {
                run {
                    if(element is ParadoxScriptProperty && element.parent is ParadoxScriptRootBlock) {
                        val definitionInfo = element.definitionInfo ?: return@run
                        val eventName = definitionInfo.name
                        if(eventName.isParameterized()) return@run
                        val eventType = ParadoxEventHandler.getType(definitionInfo)
                        val eventScope = ParadoxEventHandler.getScope(definitionInfo)
                        val eventInfo = EventInfo(eventName, eventType, eventScope)
                        data.eventInfos.add(eventInfo)
                        currentEventInfo = eventInfo
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
                            currentEventInfo?.eventInvocationInfos?.add(eventInvocationInfo)
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
        return "events".matchesPath(path.path)
    }
    
    fun getData(file: VirtualFile, project: Project): Data? {
        return gist.getFileData(project, file)
    }
}