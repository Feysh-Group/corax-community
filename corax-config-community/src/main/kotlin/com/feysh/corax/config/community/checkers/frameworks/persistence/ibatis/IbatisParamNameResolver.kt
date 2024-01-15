package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis

import com.feysh.corax.config.api.utils.sootTypeName
import com.feysh.corax.config.api.utils.typename
import com.feysh.corax.config.community.language.soot.annotation.StringElement
import com.feysh.corax.config.community.language.soot.annotation.getParamAnnotations
import com.feysh.corax.config.community.language.soot.annotation.getParamName
import org.apache.ibatis.reflection.ParamNameResolver
import org.apache.ibatis.session.ResultHandler
import org.apache.ibatis.session.RowBounds
import soot.SootMethod
import java.util.*

class IbatisParamNameResolver(
    val method: SootMethod
) {
    private var hasParamAnnotation = false
    val names: SortedMap<Int, MutableList<String>>
    val paramCount: Int

    init {
        val map: SortedMap<Int, MutableList<String>> = TreeMap()
        paramCount = method.parameterCount
        for (paramIndex in 0 until method.parameterCount) {
            if (isSpecialParameter(method.getParameterType(paramIndex).typename)) {
                // skip special parameters
                continue
            }
            val annotations = method.getParamAnnotations(paramIndex)
            var name: String? = null
            for (annotation in annotations) {
                if (annotation.type == "org.apache.ibatis.annotations.Param") {
                    hasParamAnnotation = true
                    val value = (annotation.getElement("value") as? StringElement)?.value
                    if (value != null) {
                        name = value
                        break
                    }
                }
            }
            if (name != null) {
                map.getOrPut(paramIndex) { mutableListOf() }.add(name)
            }
            // @Param was not specified.
            name = getActualParamName(method, paramIndex)
            if (name != null) {
                map.getOrPut(paramIndex) { mutableListOf() }.add(name)
            }
            // use the parameter index as the name ("0", "1", ...)
            // gcode issue #71
            name = map.size.toString()
            map.getOrPut(paramIndex) { mutableListOf() }.add(name)

            // add generic param names (param1, param2, ...)
            val genericParamName = ParamNameResolver.GENERIC_NAME_PREFIX + (paramIndex + 1)
            map.getOrPut(paramIndex) { mutableListOf() }.add(genericParamName)
        }
        names = Collections.unmodifiableSortedMap(map)
    }

    private fun isSpecialParameter(clazz: String?): Boolean {
        if (clazz == null) return false
        return RowBounds::class.java.sootTypeName == clazz || ResultHandler::class.java.sootTypeName == clazz
    }

    private fun getActualParamName(method: SootMethod, paramIndex: Int): String? {
        return method.getParamName(paramIndex)
    }

    fun getParamByName(name: String): Int? {
        if ((!hasParamAnnotation && paramCount == 1) && (name == "collection" || name == "list" || name == "array")) {
            return 0
        }
        names.forEach { (index: Int, u) ->
            if (name in u) return index
        }
        return null
    }

}