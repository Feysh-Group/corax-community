package com.feysh.corax.config.general.model.type

import com.feysh.corax.config.api.*
import com.feysh.corax.config.general.model.ConfigCenter
import com.feysh.corax.config.general.utils.isBoxedPrimitives
import com.feysh.corax.config.general.utils.isStringType
import soot.ArrayType
import soot.PrimType
import soot.RefType
import soot.Scene
import soot.SootMethod
import soot.Type

class TypeHandler {

    interface Visitor<R> {
        fun visit(t: StringType) = visitDefault(t)
        fun visit(t: PrimitiveType) = visitDefault(t)
        fun visit(t: BoxedPrimitiveType) = visitDefault(t)
        fun visit(t: MapType) = visitDefault(t)
        fun visit(t: CollectionType) = visitDefault(t)
        fun visit(t: OptionalType) = visitDefault(t)
        fun visit(t: OtherClassType) = visitDefault(t)
        fun visit(t: UnknownType) = visitDefault(t)
        fun visitDefault(t: HType): R
    }

    sealed class HType {
        abstract fun <R> visit(v: Visitor<R>): R
    }

    data class StringType(val type: Type) : HType() {
        override fun <R> visit(v: Visitor<R>) = v.visit(this)
    }

    data class PrimitiveType(val type: PrimType) : HType() {
        override fun <R> visit(v: Visitor<R>) = v.visit(this)
    }

    data class BoxedPrimitiveType(val type: RefType) : HType() {
        override fun <R> visit(v: Visitor<R>) = v.visit(this)
    }


    data class MapType(val type: Type, val elementDeclaredType: Type? = null, val isMultiValueMap: Boolean = false) :
        HType() {
        override fun <R> visit(v: Visitor<R>) = v.visit(this)
    }


    data class CollectionType(val type: Type) : HType() {
        override fun <R> visit(v: Visitor<R>) = v.visit(this)
    }

    data class OptionalType(val type: Type) : HType() {
        override fun <R> visit(v: Visitor<R>) = v.visit(this)
    }

    data class OtherClassType(val type: RefType) : HType() {
        override fun <R> visit(v: Visitor<R>) = v.visit(this)
    }

    data class UnknownType(val type: Type) : HType() {
        override fun <R> visit(v: Visitor<R>) = v.visit(this)
    }

    companion object {
        fun getHandlerType(type: Type): HType {
            if (type.isStringType) return StringType(type)
            if (type is PrimType) return PrimitiveType(type)
            if (type.isBoxedPrimitives) return BoxedPrimitiveType(type as RefType)
            if (ConfigCenter.isMultiValueMapClassType(type)) return MapType(type, isMultiValueMap = true)
            if (ConfigCenter.isMapClassType(type)) return MapType(type)
            if (ConfigCenter.isCollectionClassType(type)) return CollectionType(type)
            if (ConfigCenter.isOptionalClassType(type)) return OptionalType(type)
            if (type is RefType) return OtherClassType(type)
            return UnknownType(type)
        }

        fun getHandlerType(sootMethod: SootMethod) =
            Array(sootMethod.parameterCount) { getHandlerType(sootMethod.getParameterType(it)) }.withIndex()
    }
}


abstract class HandlerTypeVisitor(val builder: IOperatorFactory, open val param: ILocalT<*>) :
    TypeHandler.Visitor<Unit> {
    abstract fun visit(accessPath: ILocalT<*>, paramType: Type)

    override fun visit(t: TypeHandler.StringType) {
        visit(param, t.type)
    }

    override fun visit(t: TypeHandler.BoxedPrimitiveType) {
        visit(param, t.type)
    }

    override fun visit(t: TypeHandler.OtherClassType) {
        visit(param, t.type)
    }

    override fun visit(t: TypeHandler.CollectionType) {
        with(builder) {
            val elementType = if (t.type is ArrayType) {
                t.type.elementType
            } else {
                Scene.v().objectType
            }
            visit(param.field(Elements), elementType)
        }
    }

    override fun visit(t: TypeHandler.MapType) {
        with(builder) {
            visit(param.field(MapKeys), Scene.v().objectType)
            if (t.isMultiValueMap) {
                visit(param.field(MapValues).field(Elements), Scene.v().objectType)
            } else {
                visit(param.field(MapValues), Scene.v().objectType)
            }
        }
    }

    override fun visit(t: TypeHandler.PrimitiveType) {
        with(builder) {
            visit(param.field(MapKeys), Scene.v().objectType)
            visit(param.field(MapValues), Scene.v().objectType)
        }
    }

    override fun visit(t: TypeHandler.OptionalType) {
        with(builder) {
            visit(param.field(Elements), Scene.v().objectType)
        }
    }

    override fun visitDefault(t: TypeHandler.HType) {}
}
