package org.steven.chen.utils.mapper

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.BeanUtils
import org.steven.chen.utils.StringUtil
import java.io.IOException
import java.util.*

final class Jackson2FlatMapperK {

    companion object {
        private val PARTING = "."
        private val ARRAY_PREFIX = "["
        private val ARRAY_SUFFIX = "]"
        private val mapper: ObjectMapper = getObjectMapper()

        private fun getObjectMapper(): ObjectMapper {
            val result = ObjectMapper()
            result.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            result.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            result.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            return result
        }
    }

    @Throws(IOException::class)
    fun <T> fromFlatMapper(target: Map<String, Any>, clazz: Class<T>): T {
        val cacheMap = fromFlatMapper(target) ?: return BeanUtils.instantiateClass(clazz)
        return mapper.readValue(mapper.writeValueAsString(cacheMap), clazz)
    }

    fun toFlatMapper(obj: Any?): Map<String, Any>? {
        if (obj == null) return null
        val resultMap = LinkedHashMap<String, Any>()
        val source: JsonNode
        if (obj is JsonNode) {
            source = obj
        } else {
            source = mapper.valueToTree<JsonNode>(obj)
        }
        doFlatten("", source, resultMap)
        return resultMap
    }

    fun fromFlatMapper(target: Map<String, Any>?): Map<String, Any>? {
        if (target == null) return null
        val resultMap = LinkedHashMap<String, Any>()
        doUnFlatten("", resultMap, target)
        return resultMap
    }

    private fun doFlatten(propertyPrefix: String, source: JsonNode, resultMap: MutableMap<String, Any>) {
        if (source.isArray) {
            var index = 0
            val arrays = source.elements()
            while (arrays.hasNext()) {
                val array = arrays.next()
                doFlatten(propertyPrefix + ARRAY_PREFIX + index++ + ARRAY_SUFFIX, array, resultMap)
            }
        } else if (source.isObject) {
            var tmpPrefix = propertyPrefix
            if (StringUtil.isNotBlank(propertyPrefix)) {
                tmpPrefix += PARTING
            }
            val iterator = source.fields()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                doFlatten(tmpPrefix + entry.key, entry.value, resultMap)
            }
        } else {
            resultMap.put(propertyPrefix, source.asText())
        }
    }

    private fun doUnFlatten(propertyPrefix: String, resultMap: LinkedHashMap<String, Any>, target: Map<String, Any>) {
        val entries = target.entries
        for ((pKey, value) in entries) {
            if (pKey.startsWith(propertyPrefix)) {
                val key = pKey.substring(propertyPrefix.length)
                if (key.contains(PARTING)) {
                    processMap(propertyPrefix, resultMap, target, key)
                    continue
                } else if (checkArrayKey(key)) {
                    processArray(resultMap, key, value)
                    continue
                }
                resultMap.put(key, value)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun processArray(resultMap: LinkedHashMap<String, Any>, key: String, value: Any) {
        val index = (key.substring(key.indexOf(ARRAY_PREFIX) + 1, key.indexOf(ARRAY_SUFFIX))).toInt()
        val tmpKey = key.substring(0, key.indexOf(ARRAY_PREFIX))
        var item = resultMap[tmpKey] as? UpdateList<Any>
        if (item == null) {
            item = UpdateList<Any>()
            resultMap.put(tmpKey, item)
        }
        item.put(index, value)
    }

    private fun checkArrayKey(key: String): Boolean {
        val prefixIndex = key.indexOf(ARRAY_PREFIX)
        val suffixIndex = key.indexOf(ARRAY_SUFFIX)
        if (prefixIndex >= 0) {
            if (suffixIndex == -1) {
                throw IllegalArgumentException("The array key($key) is not found $ARRAY_SUFFIX !")
            } else if (suffixIndex < prefixIndex) {
                throw IllegalArgumentException("The array key $ARRAY_SUFFIX index < $ARRAY_PREFIX index !")
            }
            return true
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    private fun processMap(propertyPrefix: String, resultMap: LinkedHashMap<String, Any>, target: Map<String, Any>, pKey: String) {
        val key = pKey.substring(0, pKey.indexOf(PARTING))
        if (checkArrayKey(key)) {
            val index = (key.substring(key.indexOf(ARRAY_PREFIX) + 1, key.indexOf(ARRAY_SUFFIX))).toInt()
            val tmpKey = key.substring(0, key.indexOf(ARRAY_PREFIX))
            var item = resultMap[tmpKey] as? UpdateList<Any>
            if (item == null) {
                item = UpdateList<Any>()
                resultMap.put(tmpKey, item)
            }
            var childMap = item[index] as? LinkedHashMap<String, Any>
            if (childMap == null) {
                childMap = LinkedHashMap<String, Any>()
                item.put(index, childMap)
            }
            doUnFlatten(propertyPrefix + key + PARTING, childMap, target)
        } else {
            var item = resultMap[key] as? LinkedHashMap<String, Any>
            if (item == null) {
                item = LinkedHashMap<String, Any>()
                resultMap.put(key, item)
            }
            doUnFlatten(propertyPrefix + key + PARTING, item, target)
        }
    }
}