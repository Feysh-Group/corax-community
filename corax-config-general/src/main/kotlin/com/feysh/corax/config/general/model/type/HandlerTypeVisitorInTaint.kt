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

import com.feysh.corax.config.api.ILocalT
import com.feysh.corax.config.api.IOperatorFactory
import com.feysh.corax.config.api.ITaintSet
import com.feysh.corax.config.general.model.ConfigCenter
import com.feysh.corax.config.general.model.javaee.JavaeeAnnotationSource
import com.feysh.corax.config.general.utils.isVoidType
import soot.Type

abstract class HandlerTypeVisitorInTaint(
    builder: IOperatorFactory,
    open val taintPrimTypeValue: Boolean = ConfigCenter.option.taintPrimTypeValue
) : HandlerTypeVisitor(builder) {

    override fun visit(v: ILocalT<*>, t: TypeHandler.PrimitiveType) {
        if (!taintPrimTypeValue) {
            return
        }
        super.visit(v, t)
    }

    override fun visit(v: ILocalT<*>, t: TypeHandler.BoxedPrimitiveType) {
        if (!taintPrimTypeValue) {
            return
        }
        return super.visit(v, t)
    }

    override fun visit(v: ILocalT<*>, t: TypeHandler.OtherClassType) {
        if (t.type.isVoidType) return
        super.visit(v, t)
    }


    open class TaintFrom(
        builder: IOperatorFactory,
        taintPrimTypeValue: Boolean = ConfigCenter.option.taintPrimTypeValue
    ) : HandlerTypeVisitorInTaint(builder, taintPrimTypeValue) {
        private val taintFrom = mutableListOf<ILocalT<*>>() // Rhs
        override fun process(accessPath: ILocalT<*>, paramType: Type) {
            taintFrom += accessPath
        }

        override fun visit(v: ILocalT<*>, t: TypeHandler.OtherClassType) {
            if (!JavaeeAnnotationSource.isWebModelClassType(t)) {
                return
            }
            with(builder) {
                // taint from object instance base
                process(v, t.type)
                // taint from all the declaring fields
                process(v.subFields, t.type)
            }
        }

        fun get(v: ILocalT<*>, hType: TypeHandler.HType): List<ILocalT<*>> {
            taintFrom.clear()
            hType.visit(v, this)
            return taintFrom
        }

        fun getExpr(v: ILocalT<*>, hType: TypeHandler.HType): ITaintSet? {
            val from = get(v, hType)
            return with(builder) {
                from.fold(null as ITaintSet?) { acc, argFrom ->
                    acc?.let { it + argFrom.taint } ?: argFrom.taint
                }
            }
        }

    }

    open class TaintOut(
        builder: IOperatorFactory,
        taintPrimTypeValue: Boolean = ConfigCenter.option.taintPrimTypeValue
    ) : HandlerTypeVisitorInTaint(builder, taintPrimTypeValue) {
        private val taintOut = mutableListOf<ILocalT<*>>() // Lhs
        override fun process(accessPath: ILocalT<*>, paramType: Type) {
            taintOut += accessPath
        }
        override fun visit(v: ILocalT<*>, t: TypeHandler.OtherClassType) {
            if (!JavaeeAnnotationSource.isWebModelClassType(t)) {
                return
            }
            with(builder) {
                // taint to object instance base
                process(v, t.type)
                // taint to all the declaring fields
                process(v.subFields, t.type)
            }
        }

        fun get(v: ILocalT<*>, hType: TypeHandler.HType): List<ILocalT<*>> {
            taintOut.clear()
            hType.visit(v, this)
            return taintOut
        }
    }
}

