package com.windea.plugin.idea.paradox.util

import com.windea.plugin.idea.paradox.*
import java.net.*
import java.net.http.*
import java.net.http.HttpResponse.*
import java.time.*
import java.util.concurrent.*

//https://qunxing.huijiwiki.com/
//https://paradox.paradoxwikis.com/
//https://qunxing.huijiwiki.com/wiki/%E6%96%87%E4%BB%B6:Xxx.png
//https://paradox.paradoxwikis.com/File:Xxx.png

/**
 * 图标地址的解析器。
 *
 * 基于名称以及qunxing.huijiwiki.com和paradox.paradoxwikis.com上的文件解析图标地址。
 */
object ParadoxIconUrlResolver {
	private const val delay:Long = 500
	private val timeout = Duration.ofMinutes(3)
	
	private val httpClient = HttpClient.newBuilder().connectTimeout(timeout).build()
	private val bodyHandler = BodyHandlers.ofLines()
	private val urlCache = ConcurrentHashMap<String, String>()
	private val executor = Executors.newCachedThreadPool()
	private val doResolveCache = CopyOnWriteArraySet<String>()

	private const val unknownIconUrl = "https://huiji-public.huijistatic.com/qunxing/uploads/d/dd/Unknown.png"
	
	fun resolve(name: String,defaultToUnknown:Boolean=true): String {
		if(name.isEmpty()) return getDefaultUrl(defaultToUnknown)
		return try {
			//尝试从缓存中获取
			val url = urlCache[name]
			//如果存在，若是空字符串则返回默认图标，否则直接返回
			//如果不存在，则先保存空字符串，然后异步解析图标，等待一段时间后，再次尝试获取图标
			if(url != null) return url.ifEmpty { getDefaultUrl(defaultToUnknown) }
			resolveUrlAsync(name)
			Thread.sleep(delay)
			val delayUrl = urlCache[name]
			if(delayUrl!= null) return delayUrl.ifEmpty { getDefaultUrl(defaultToUnknown) }
			getDefaultUrl(defaultToUnknown)
		}catch(e: Exception){
			//如果出现异常，那么返回默认图标
			getDefaultUrl(defaultToUnknown)
		}
	}
	
	private fun getDefaultUrl(defaultToUnknown:Boolean): String {
		return if(defaultToUnknown) unknownIconUrl else ""
	}
	
	private fun resolveUrlAsync(name: String){
		executor.execute {
			try {
				val url = doResolveUrl(name)
				urlCache[name] = url
			}catch(e: Exception){
				urlCache[name] = ""
			}
		}
	}
	
	private fun doResolveUrl(name: String): String {
		return doResolveUrlFromHuijiwiki(name) ?: doResolveUrlFromParadoxwikis(name) ?: ""
	}
	
	private const val huijiwikiPrefix = "<li><a href=\"#filelinks\">文件用途</a></li></ul><div class=\"fullImageLink\" id=\"file\"><a href=\""
	private const val huijiwikiPrefixLength = huijiwikiPrefix.length
	
	private fun doResolveUrlFromHuijiwiki(name: String): String? {
		val url = huijiwikiPngUrl(name)
		val uri =runCatching { URI.create(url)}.getOrElse { return null }
		val httpResponse = httpClient.send(HttpRequest.newBuilder().GET().uri(uri).build(), bodyHandler)
		if(httpResponse.statusCode() == 200) {
			val lines = httpResponse.body()
			return lines.filter {
				it.startsWith(huijiwikiPrefix)
			}.map {
				val index = it.indexOf(huijiwikiPrefix)
				val startIndex = index + huijiwikiPrefixLength
				val endIndex = it.indexOf('"', startIndex + 1)
				it.substring(startIndex, endIndex)
			}.findFirst().orElse(null)
		}
		return null
	}
	
	private fun huijiwikiPngUrl(name:String): String {
		val fqName = when{
			name == "pops" -> "Pop"
			name == "origin_default" -> "Prosperous_Unification"
			name.startsWith("origin_") -> name.removePrefix("origin_").toCapitalizedWords()
			else -> name.toCapitalizedWord()
		}
		return "https://qunxing.huijiwiki.com/wiki/%E6%96%87%E4%BB%B6:$fqName.png"
	}
	
	private const val paradoxwikisPrefix = "<div class=\"fullImageLink\" id=\"file\"><a href=\""
	private const val paradoxwikisPrefixLength = paradoxwikisPrefix.length
	
	private fun doResolveUrlFromParadoxwikis(name: String): String? {
		val url = paradoxwikisPngUrl(name)
		val uri =runCatching { URI.create(url)}.getOrElse { return null }
		val httpResponse = httpClient.send(HttpRequest.newBuilder().GET().uri(uri).build(), bodyHandler)
		if(httpResponse.statusCode() == 200) {
			val lines = httpResponse.body()
			return lines.filter {
				it.startsWith("<li>") && it.contains(paradoxwikisPrefix)
			}.map {
				val index = it.indexOf(paradoxwikisPrefix)
				val startIndex = index + paradoxwikisPrefixLength
				val endIndex = it.indexOf('"', startIndex + 1)
				paradoxwikisUrl +  it.substring(startIndex, endIndex)
			}.findFirst().orElse(null)
		}
		return null
	}
	
	private fun paradoxwikisPngUrl(name:String): String {
		val fqName = when{
			name == "pops" -> "Pop"
			name == "origin_fallen_empire" -> "Origins_elder_race"
			name.startsWith("origin") -> name.replace("origin","Origins_")
			else -> name.toCapitalizedWord()
		}
		return "https://paradox.paradoxwikis.com/File:$fqName.png"
	}
}

