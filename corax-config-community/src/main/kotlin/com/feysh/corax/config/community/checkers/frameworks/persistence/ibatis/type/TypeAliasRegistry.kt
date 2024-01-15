package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.type

import com.feysh.corax.config.api.utils.sootTypeName
import com.feysh.corax.config.api.utils.typename
import com.feysh.corax.config.community.language.soot.annotation.*
import org.apache.ibatis.type.Alias
import org.apache.ibatis.type.TypeException
import soot.RefType
import soot.Scene
import soot.SootClass
import soot.Type
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.ResultSet
import java.util.*


/**
 * @author Clinton Begin
 */
class TypeAliasRegistry {
    private val typeAliases: MutableMap<String, String> = Collections.synchronizedMap(HashMap())

    init {
        registerAlias("string", String::class.java)
        registerAlias("byte", Byte::class.java)
        registerAlias("long", Long::class.java)
        registerAlias("short", Short::class.java)
        registerAlias("int", Int::class.java)
        registerAlias("integer", Int::class.java)
        registerAlias("double", Double::class.java)
        registerAlias("float", Float::class.java)
        registerAlias("boolean", Boolean::class.java)
        registerAlias("byte[]", Array<Byte>::class.java)
        registerAlias("long[]", Array<Long>::class.java)
        registerAlias("short[]", Array<Short>::class.java)
        registerAlias("int[]", Array<Int>::class.java)
        registerAlias("integer[]", Array<Int>::class.java)
        registerAlias("double[]", Array<Double>::class.java)
        registerAlias("float[]", Array<Float>::class.java)
        registerAlias("boolean[]", Array<Boolean>::class.java)
        registerAlias("_byte", Byte::class.javaPrimitiveType!!)
        registerAlias("_long", Long::class.javaPrimitiveType!!)
        registerAlias("_short", Short::class.javaPrimitiveType!!)
        registerAlias("_int", Int::class.javaPrimitiveType!!)
        registerAlias("_integer", Int::class.javaPrimitiveType!!)
        registerAlias("_double", Double::class.javaPrimitiveType!!)
        registerAlias("_float", Float::class.javaPrimitiveType!!)
        registerAlias("_boolean", Boolean::class.javaPrimitiveType!!)
        registerAlias("_byte[]", ByteArray::class.java)
        registerAlias("_long[]", LongArray::class.java)
        registerAlias("_short[]", ShortArray::class.java)
        registerAlias("_int[]", IntArray::class.java)
        registerAlias("_integer[]", IntArray::class.java)
        registerAlias("_double[]", DoubleArray::class.java)
        registerAlias("_float[]", FloatArray::class.java)
        registerAlias("_boolean[]", BooleanArray::class.java)
        registerAlias("date", Date::class.java)
        registerAlias("decimal", BigDecimal::class.java)
        registerAlias("bigdecimal", BigDecimal::class.java)
        registerAlias("biginteger", BigInteger::class.java)
        registerAlias("object", Any::class.java)
        registerAlias("date[]", Array<Date>::class.java)
        registerAlias("decimal[]", Array<BigDecimal>::class.java)
        registerAlias("bigdecimal[]", Array<BigDecimal>::class.java)
        registerAlias("biginteger[]", Array<BigInteger>::class.java)
        registerAlias("object[]", Array<Any>::class.java)
        registerAlias("map", MutableMap::class.java)
        registerAlias("hashmap", HashMap::class.java)
        registerAlias("list", MutableList::class.java)
        registerAlias("arraylist", ArrayList::class.java)
        registerAlias("collection", MutableCollection::class.java)
        registerAlias("iterator", MutableIterator::class.java)
        registerAlias("ResultSet", ResultSet::class.java)
    }

    // throws class cast exception as well if types cannot be assigned
    fun resolveAlias(string: String): String? {
        return try {
            // issue #748
            val key = string.lowercase()
            val value = if (typeAliases.containsKey(key)) {
                typeAliases[key]
            } else {
                null
            }
            value
        } catch (e: ClassNotFoundException) {
            throw TypeException("Could not resolve type alias '$string'.  Cause: $e", e)
        }
    }

    fun registerAliases(packageName: String, superType: SootClass) {
        val fastHierarchy = Scene.v().orMakeFastHierarchy
        val subClasses = if (superType.isInterface) {
            fastHierarchy.getAllImplementersOfInterface(superType)
        } else{
            // maybe abstract class
            fastHierarchy.getSubclassesOf(superType)
        }
        for (type in subClasses) {
            if (!type.name.startsWith(packageName)) {
                continue
            }
            // Ignore inner classes and interfaces (including package-info.java)
            // Skip also inner classes. See issue #6
            if (!type.isAnonymousClass && !type.isInterface && !type.isMemberClass) {
                registerAlias(type.type)
            }
        }
    }
    fun registerAlias(classType: RefType) {
        val clazz = classType.sootClass ?: return
        var alias = clazz.shortName

        val aliasAnnotation = clazz.getAnnotation(Alias::class.sootTypeName)
        if (aliasAnnotation != null) {
            alias = (aliasAnnotation.getElement("value") as? StringElement)?.value
        }

        registerAlias(alias, classType)
    }

    fun registerAlias(type: Class<*>) {
        var alias = type.getSimpleName()
        val aliasAnnotation = type.getAnnotation(Alias::class.java)
        if (aliasAnnotation != null) {
            alias = aliasAnnotation.value
        }
        registerAlias(alias, type)
    }

    fun registerAlias(alias: String, value: Class<*>) {
        registerAlias(alias, value.sootTypeName)
    }


    private fun registerAlias(alias: String, value: String) {
        val key = alias.lowercase()
        if (typeAliases.containsKey(key) && typeAliases[key] != null && typeAliases[key] != value) {
            throw TypeException("The alias '$alias' is already mapped to the value '" + typeAliases[key] + "'.")
        }
        typeAliases[key] = value
    }

    fun registerAlias(alias: String, type: Type) = type.typename?.let { registerAlias(alias, it) }

    /**
     * Gets the type aliases.
     *
     * @return the type aliases
     * @since 3.2.2
     */
    fun getTypeAliases(): Map<String, String> {
        return Collections.unmodifiableMap(typeAliases)
    }

    fun meet(typeAliasRegistry: TypeAliasRegistry) {
        this.typeAliases += typeAliasRegistry.typeAliases
    }
}
