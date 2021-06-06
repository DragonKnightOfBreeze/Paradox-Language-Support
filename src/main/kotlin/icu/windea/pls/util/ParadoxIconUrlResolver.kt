package icu.windea.pls.util

import com.intellij.openapi.project.*
import icu.windea.pls.*
import org.slf4j.*
import java.nio.file.*
import java.util.concurrent.*

//https://qunxing.huijiwiki.com/
//https://paradox.paradoxwikis.com/
//https://qunxing.huijiwiki.com/wiki/%E6%96%87%E4%BB%B6:Xxx.png
//https://paradox.paradoxwikis.com/File:Xxx.png

/**
 * 图标地址的解析器。
 */
object ParadoxIconUrlResolver {
	private val logger = LoggerFactory.getLogger(ParadoxIconUrlResolver::class.java)
	
	private const val delay: Long = 300
	//private val timeout = Duration.ofMinutes(3)
	
	//private val httpClient = HttpClient.newBuilder().connectTimeout(timeout).build()
	//private val bodyHandler = BodyHandlers.ofLines()
	//private val executor = Executors.newCachedThreadPool()
	private val cache = ConcurrentHashMap<String, String?>()
	
	//private const val paradoxwikisUrl = "https://paradox.paradoxwikis.com"
	//private const val huijiwikiUrl = "https://qunxing.huijiwiki.com"
	
	private const val unknownIconUrl = "https://windea.icu/Paradox-Language-Support/assets/img/unknown.png"
	
	fun resolve(name: String, project: Project, defaultToUnknown: Boolean = true): String {
		if(name.isEmpty()) return getDefaultUrl(defaultToUnknown)
		return try {
			var url = cache[name]
			//如果缓存中没有或者对应的本地png文件不存在，需要重新获取
			if(url == null || url.isEmpty() || Files.notExists(Path.of(url))) {
				url = doResolve(name, project)
				cache.put(name, url.orEmpty())
			}
			//如果还是没有得到则返回默认的url
			if(url == null || url.isEmpty()) getDefaultUrl(defaultToUnknown) else url
		} catch(e: Exception) {
			logger.error("Resolve paradox icon failed.", e)
			//如果出现异常，那么返回默认图标
			getDefaultUrl(defaultToUnknown)
		}
	}
	
	private fun getDefaultUrl(defaultToUnknown: Boolean): String {
		return if(defaultToUnknown) unknownIconUrl else ""
	}
	
	private fun doResolve(name: String, project: Project): String? {
		val iconDefinition = findIcon(name, project) ?: return null
		val fileInfo = iconDefinition.paradoxFileInfo ?: return null
		val rootPath = fileInfo.rootPath //rootPath
		val ddsRelPath = iconDefinition.findProperty("textureFile")?.value ?: return null //paradoxPath
		val ddsAbsPath = rootPath.resolve(ddsRelPath).toString()
		return DdsToPngConverter.convert(ddsAbsPath, ddsRelPath)
	}
	
	//fun resolve(name: String,defaultToUnknown:Boolean=true): String {
	//	if(name.isEmpty()) return getDefaultUrl(defaultToUnknown)
	//	return try {
	//		//尝试从缓存中获取
	//		val url = urlCache[name]
	//		//如果存在，若是空字符串则返回默认图标，否则直接返回
	//		//如果不存在，则先保存空字符串，然后异步解析图标，等待一段时间后，再次尝试获取图标
	//		if(url != null) return url.ifEmpty { getDefaultUrl(defaultToUnknown) }
	//		resolveUrlAsync(name)
	//		Thread.sleep(delay)
	//		val delayUrl = urlCache[name]
	//		if(delayUrl!= null) return delayUrl.ifEmpty { getDefaultUrl(defaultToUnknown) }
	//		getDefaultUrl(defaultToUnknown)
	//	}catch(e: Exception){
	//		//如果出现异常，那么返回默认图标
	//		getDefaultUrl(defaultToUnknown)
	//	}
	//}
	//
	//private fun getDefaultUrl(defaultToUnknown:Boolean): String {
	//	return if(defaultToUnknown) unknownIconUrl else ""
	//}
	//
	//private fun resolveUrlAsync(name: String){
	//	executor.execute {
	//		try {
	//			urlCache[name] = ""
	//			val url = doResolveUrl(name)
	//			urlCache[name] = url
	//		}catch(e: Exception){
	//			//忽略
	//		}
	//	}
	//}
	//
	//private fun doResolveUrl(name: String): String {
	//	return doResolveUrlFromHuijiwiki(name) ?: doResolveUrlFromParadoxwikis(name) ?: ""
	//}
	//
	//private const val huijiwikiPrefix = "<li><a href=\"#filelinks\">文件用途</a></li></ul><div class=\"fullImageLink\" id=\"file\"><a href=\""
	//private const val huijiwikiPrefixLength = huijiwikiPrefix.length
	//
	//private fun doResolveUrlFromHuijiwiki(name: String): String? {
	//	val url = huijiwikiPngUrl(name)
	//	val uri =runCatching { URI.create(url)}.getOrElse { return null }
	//	val httpResponse = httpClient.send(HttpRequest.newBuilder().GET().uri(uri).build(), bodyHandler)
	//	if(httpResponse.statusCode() == 200) {
	//		val lines = httpResponse.body()
	//		return lines.filter {
	//			it.startsWith(huijiwikiPrefix)
	//		}.map {
	//			val index = it.indexOf(huijiwikiPrefix)
	//			val startIndex = index + huijiwikiPrefixLength
	//			val endIndex = it.indexOf('"', startIndex + 1)
	//			it.substring(startIndex, endIndex)
	//		}.findFirst().orElse(null)
	//	}
	//	return null
	//}
	//
	//private fun huijiwikiPngUrl(name:String): String {
	//	val fqName = when{
	//		name == "pops" -> "Pop"
	//		name == "origin_default" -> "Prosperous_Unification"
	//		name.startsWith("origin_") -> name.removePrefix("origin_").toCapitalizedWords()
	//		else -> name.toCapitalizedWord()
	//	}
	//	return "https://qunxing.huijiwiki.com/wiki/%E6%96%87%E4%BB%B6:$fqName.png"
	//}
	//
	//private const val paradoxwikisPrefix = "<div class=\"fullImageLink\" id=\"file\"><a href=\""
	//private const val paradoxwikisPrefixLength = paradoxwikisPrefix.length
	//
	//private fun doResolveUrlFromParadoxwikis(name: String): String? {
	//	val url = paradoxwikisPngUrl(name)
	//	val uri =runCatching { URI.create(url)}.getOrElse { return null }
	//	val httpResponse = httpClient.send(HttpRequest.newBuilder().GET().uri(uri).build(), bodyHandler)
	//	if(httpResponse.statusCode() == 200) {
	//		val lines = httpResponse.body()
	//		return lines.filter {
	//			it.startsWith("<li>") && it.contains(paradoxwikisPrefix)
	//		}.map {
	//			val index = it.indexOf(paradoxwikisPrefix)
	//			val startIndex = index + paradoxwikisPrefixLength
	//			val endIndex = it.indexOf('"', startIndex + 1)
	//			paradoxwikisUrl +  it.substring(startIndex, endIndex)
	//		}.findFirst().orElse(null)
	//	}
	//	return null
	//}
	//
	//private fun paradoxwikisPngUrl(name:String): String {
	//	val fqName = when{
	//		name == "pops" -> "Pop"
	//		name == "origin_fallen_empire" -> "Origins_elder_race"
	//		name.startsWith("origin") -> name.replace("origin","Origins_")
	//		else -> name.toCapitalizedWord()
	//	}
	//	return "https://paradox.paradoxwikis.com/File:$fqName.png"
	//}
}

