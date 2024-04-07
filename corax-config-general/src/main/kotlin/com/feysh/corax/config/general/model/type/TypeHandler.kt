/*
 *  CoraxJava - a Java Static Analysis Framework
 *  Copyright (C) 2024.  Feysh-Tech Group
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

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

    interface Visitor<Obj, R> {
        fun visit(v: Obj, t: StringType) = visitDefault(v, t)
        fun visit(v: Obj, t: PrimitiveType) = visitDefault(v, t)
        fun visit(v: Obj, t: BoxedPrimitiveType) = visitDefault(v, t)
        fun visit(v: Obj, t: MapType) = visitDefault(v, t)
        fun visit(v: Obj, t: CollectionType) = visitDefault(v, t)
        fun visit(v: Obj, t: OptionalType) = visitDefault(v, t)
        fun visit(v: Obj, t: OtherClassType) = visitDefault(v, t)
        fun visit(v: Obj, t: UnknownType) = visitDefault(v, t)
        fun visitDefault(v: Obj, t: HType): R
    }

    sealed class HType {
        abstract fun <Obj, R> visit(obj: Obj, v: Visitor<Obj, R>): R
    }

    data class StringType(val type: Type) : HType() {
        override fun <Obj, R> visit(obj: Obj, v: Visitor<Obj, R>) = v.visit(obj,this)
    }

    data class PrimitiveType(val type: PrimType) : HType() {
        override fun <Obj, R> visit(obj: Obj, v: Visitor<Obj, R>) = v.visit(obj,this)
    }

    data class BoxedPrimitiveType(val type: RefType) : HType() {
        override fun <Obj, R> visit(obj: Obj, v: Visitor<Obj, R>) = v.visit(obj,this)
    }


    data class MapType(val type: Type, val elementDeclaredType: Type? = null, val isMultiValueMap: Boolean = false) :
        HType() {
        override fun <Obj, R> visit(obj: Obj, v: Visitor<Obj, R>) = v.visit(obj,this)
    }


    data class CollectionType(val type: Type) : HType() {
        override fun <Obj, R> visit(obj: Obj, v: Visitor<Obj, R>) = v.visit(obj,this)
    }

    data class OptionalType(val type: Type) : HType() {
        override fun <Obj, R> visit(obj: Obj, v: Visitor<Obj, R>) = v.visit(obj,this)
    }

    data class OtherClassType(val type: RefType) : HType() {
        override fun <Obj, R> visit(obj: Obj, v: Visitor<Obj, R>) = v.visit(obj,this)
    }

    data class UnknownType(val type: Type) : HType() {
        override fun <Obj, R> visit(obj: Obj, v: Visitor<Obj, R>) = v.visit(obj,this)
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


abstract class HandlerTypeVisitor(val builder: IOperatorFactory) :
    TypeHandler.Visitor<ILocalT<*>, Unit> {
    abstract fun process(accessPath: ILocalT<*>, paramType: Type)

    override fun visit(v: ILocalT<*>, t: TypeHandler.StringType) {
        process(v, t.type)
    }

    override fun visit(v: ILocalT<*>, t: TypeHandler.BoxedPrimitiveType) {
        process(v, t.type)
    }

    override fun visit(v: ILocalT<*>, t: TypeHandler.OtherClassType) {
        process(v, t.type)
    }

    override fun visit(v: ILocalT<*>, t: TypeHandler.CollectionType) {
        with(builder) {
            val elementType = if (t.type is ArrayType) {
                t.type.elementType
            } else {
                Scene.v().objectType
            }
            process(v.field(Elements), elementType)
        }
    }

    override fun visit(v: ILocalT<*>, t: TypeHandler.MapType) {
        with(builder) {
            process(v.field(MapKeys), Scene.v().objectType)
            if (t.isMultiValueMap) {
                process(v.field(MapValues).field(Elements), Scene.v().objectType)
            } else {
                process(v.field(MapValues), Scene.v().objectType)
            }
        }
    }

    override fun visit(v: ILocalT<*>, t: TypeHandler.PrimitiveType) {
        process(v, t.type)
    }

    override fun visit(v: ILocalT<*>, t: TypeHandler.OptionalType) {
        with(builder) {
            process(v.field(Elements), Scene.v().objectType)
        }
    }

    override fun visitDefault(v: ILocalT<*>, t: TypeHandler.HType) {}
}
