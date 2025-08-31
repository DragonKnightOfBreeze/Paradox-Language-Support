package icu.windea.pls.lang.settings.tools

import java.util.concurrent.ConcurrentHashMap

/**
 * A MutableMap adapter backed by the SQLite database via ProfilesDatabase.
 *
 * - Reads are cached per key. First access loads from DB.
 * - Writes update the in-memory cache; call flush() to persist all cached values.
 * - remove()/clear() are persisted immediately.
 */
class DbBackedStateMap<V : Any>(
    private val category: String,
    private val valueClass: Class<V>,
) : MutableMap<String, V> {

    private val cache = LinkedHashMap<String, V>()
    private val missing = ConcurrentHashMap.newKeySet<String>() // keys known to be absent in DB

    override val size: Int
        get() = keys.size

    override fun containsKey(key: String): Boolean {
        if (cache.containsKey(key)) return true
        if (missing.contains(key)) return false
        return ProfilesDatabase.get(category, key) != null
    }

    override fun containsValue(value: V): Boolean {
        ensureAllLoaded()
        return cache.containsValue(value)
    }

    override fun get(key: String): V? {
        cache[key]?.let { return it }
        if (missing.contains(key)) return null
        val xml = ProfilesDatabase.get(category, key) ?: run {
            missing.add(key)
            return null
        }
        val value = XmlStateCodec.deserialize(xml, valueClass)
        cache[key] = value
        return value
    }

    override fun isEmpty(): Boolean {
        if (cache.isNotEmpty()) return false
        return ProfilesDatabase.keys(category).isEmpty()
    }

    override val entries: MutableSet<MutableMap.MutableEntry<String, V>>
        get() {
            ensureAllLoaded()
            return cache.entries
        }

    override val keys: MutableSet<String>
        get() {
            // merge DB keys with cached-only keys
            val result = ProfilesDatabase.keys(category).toMutableSet()
            if (cache.isNotEmpty()) result.addAll(cache.keys)
            return LinkedHashSet(result)
        }

    override val values: MutableCollection<V>
        get() {
            ensureAllLoaded()
            return cache.values
        }

    override fun clear() {
        cache.clear()
        missing.clear()
        ProfilesDatabase.clear(category)
    }

    override fun put(key: String, value: V): V? {
        val prev = cache.put(key, value)
        missing.remove(key)
        return prev
    }

    override fun putAll(from: Map<out String, V>) {
        from.forEach { (k, v) -> put(k, v) }
    }

    override fun remove(key: String): V? {
        val prev = cache.remove(key)
        missing.add(key)
        ProfilesDatabase.remove(category, key)
        return prev
    }

    fun flush() {
        // Persist all cached values
        cache.forEach { (k, v) ->
            val xml = XmlStateCodec.serialize(v)
            ProfilesDatabase.put(category, k, xml)
        }
    }

    private fun ensureAllLoaded() {
        val dbKeys = ProfilesDatabase.keys(category)
        // add cached-only keys too
        val allKeys = LinkedHashSet<String>(dbKeys.size + cache.size)
        allKeys.addAll(dbKeys)
        allKeys.addAll(cache.keys)
        for (k in allKeys) {
            if (!cache.containsKey(k) && !missing.contains(k)) {
                val xml = ProfilesDatabase.get(category, k)
                if (xml == null) {
                    missing.add(k)
                } else {
                    val value = XmlStateCodec.deserialize(xml, valueClass)
                    cache[k] = value
                }
            }
        }
    }

    companion object {
        fun <V : Any> create(category: String, valueClass: Class<V>): DbBackedStateMap<V> {
            return DbBackedStateMap(category, valueClass)
        }
    }
}
