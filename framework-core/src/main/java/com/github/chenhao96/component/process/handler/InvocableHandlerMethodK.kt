package com.github.chenhao96.component.process.handler

import com.github.chenhao96.utils.BaseDataTypeUtil
import com.github.chenhao96.utils.JsonUtils
import com.github.chenhao96.utils.StringUtil
import org.springframework.beans.BeanUtils
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.core.MethodParameter
import org.springframework.core.ParameterNameDiscoverer
import org.springframework.util.CollectionUtils
import java.beans.Introspector
import java.lang.reflect.Method

class InvocableHandlerMethodK(bean: Any, method: Method) : HandlerMethod(bean, method) {

    private val parameterNameDiscoverer: ParameterNameDiscoverer = DefaultParameterNameDiscoverer()

    fun isReturnVoid(): Boolean {
        return isVoid
    }

    @Throws(Exception::class)
    fun getMethodArgumentValues(providedArgs: Map<String, Any>?): Array<Any?> {
        val parameters = methodParameters
        val args = arrayOfNulls<Any>(parameters.size)
        for (index in parameters.indices) {
            val parameter = parameters[index]
            parameter.initParameterNameDiscovery(parameterNameDiscoverer)
            args[index] = resolveProvidedArgument(parameter, providedArgs)
            if (args[index] != null) continue
            if (supportsParameter(parameter)) {
                try {
                    args[index] = resolveArgument(parameter, providedArgs)
                } catch (ex: Exception) {
                    throw Exception(getArgumentResolutionErrorMessage(parameter, "Failed to resolve"), ex)
                }
            }
            if (args[index] == null && BaseDataTypeUtil.isBaseDataType(parameter.parameterType)) {
                throw IllegalStateException(String.format("Could not resolve method parameter at index %d in %s: %s",
                        parameter.parameterIndex, parameter.method.toGenericString(),
                        getArgumentResolutionErrorMessage(parameter, "No suitable resolver for")))
            }
        }
        return args
    }

    @Throws(Exception::class)
    private fun resolveArgument(parameter: MethodParameter, providedArgs: Map<String, Any>?): Any? {
        if (providedArgs == null) return null
        val parameterName = parameter.parameterName
        val parameterValue = providedArgs[parameterName] ?: return null
        if (parameter.parameterType.isInterface) {
            return JsonUtils.jsonStr2Object(JsonUtils.object2Json(parameterValue), parameter.parameterType)
        }
        try {
            val inputBean = BeanUtils.instantiateClass(parameter.parameterType)
            return paramBind(inputBean, parameterValue, providedArgs)
        } catch (e: Exception) {
            return null
        }
    }

    @Throws(Exception::class)
    private fun paramBind(inputBean: Any, parameterValue: Any, providedArgs: Map<String, Any>): Any {
        var resultBean = inputBean
        if (resultBean is Collection<Any?>) {
            resultBean = JsonUtils.jsonStr2Object(JsonUtils.object2Json(parameterValue), resultBean.javaClass)
        } else {
            val beanInfo = Introspector.getBeanInfo(resultBean.javaClass)
            val propertyDescriptors = beanInfo.propertyDescriptors
            for (property in propertyDescriptors) {
                val propertyName = property.name
                if (propertyName == "class") continue
                val setter = property.writeMethod
                var value: Any? = providedArgs[propertyName] ?: continue
                val parameterType = setter.parameterTypes[0]
                if (!parameterType.isInstance(value)) {
                    value = JsonUtils.jsonStr2Object(JsonUtils.object2Json(value), parameterType)
                }
                setter.invoke(resultBean, value)
            }
        }
        return resultBean
    }

    private fun getArgumentResolutionErrorMessage(parameter: MethodParameter, text: String): String {
        parameter.parameterType
        val paramType = parameter.parameterType
        return String.format("%s argument %d of type '%s'", text, parameter.parameterIndex, paramType.name)
    }

    private fun supportsParameter(parameter: MethodParameter): Boolean {
        return !BeanUtils.isSimpleProperty(parameter.parameterType)
    }

    private fun resolveProvidedArgument(parameter: MethodParameter, providedArgs: Map<String, Any>?): Any? {
        if (providedArgs == null) return null
        val parameterName = parameter.parameterName
        val parameterType = BaseDataTypeUtil.baseDataType2BoxDataType(parameter.parameterType)
        val entries = providedArgs.entries
        if (parameterType.isInstance(providedArgs)) {
            val mapParameter = parameter.getParameterAnnotation(MapParameter::class.java)
            if (mapParameter == null || StringUtil.isEmpty(mapParameter.value)) return null
            return processMapParameter(mapParameter.value, entries)
        }
        for (entry in entries) {
            if (parameterName == entry.key) {
                if (parameterType.isInstance(entry.value)) {
                    return entry.value
                }
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun processMapParameter(prefix: String, entries: Set<Map.Entry<String, Any>>): Map<String, Any>? {
        if (CollectionUtils.isEmpty(entries)) return null
        for (entry in entries) {
            val key = entry.key
            if (key == prefix) {
                return entry.value as? Map<String, Any>
            }
        }
        return null
    }
}