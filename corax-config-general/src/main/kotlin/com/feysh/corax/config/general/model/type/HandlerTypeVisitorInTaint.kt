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
    param: ILocalT<*>,
    open val taintPrimTypeValue: Boolean = ConfigCenter.option.taintPrimTypeValue
) : HandlerTypeVisitor(builder, param) {

    override fun visit(t: TypeHandler.PrimitiveType) {
        if (!taintPrimTypeValue) {
            return
        }
        super.visit(t)
    }

    override fun visit(t: TypeHandler.BoxedPrimitiveType) {
        if (!taintPrimTypeValue) {
            return
        }
        return super.visit(t)
    }

    override fun visit(t: TypeHandler.OtherClassType) {
        if (t.type.isVoidType) return
        super.visit(t)
    }


    open class TaintFrom(
        builder: IOperatorFactory,
        param: ILocalT<*>,
        taintPrimTypeValue: Boolean = ConfigCenter.option.taintPrimTypeValue
    ) : HandlerTypeVisitorInTaint(builder, param, taintPrimTypeValue) {
        private val taintFrom = mutableListOf<ILocalT<*>>() // Rhs
        override fun visit(accessPath: ILocalT<*>, paramType: Type) {
            taintFrom += accessPath
        }

        override fun visit(t: TypeHandler.OtherClassType) {
            if (!JavaeeAnnotationSource.isWebModelClassType(t)) {
                return
            }
            with(builder) {
                // taint from object instance base
                visit(param, t.type)
                // taint from all the declaring fields
                visit(param.subFields, t.type)
            }
        }

        fun get(hType: TypeHandler.HType): List<ILocalT<*>> {
            taintFrom.clear()
            hType.visit(this)
            return taintFrom
        }

        fun getExpr(hType: TypeHandler.HType): ITaintSet? {
            val from = get(hType)
            return with(builder) {
                from.fold(null as ITaintSet?) { acc, argFrom ->
                    acc?.let { it + argFrom.taint } ?: argFrom.taint
                }
            }
        }

    }

    open class TaintOut(
        builder: IOperatorFactory,
        param: ILocalT<*>,
        taintPrimTypeValue: Boolean = ConfigCenter.option.taintPrimTypeValue
    ) : HandlerTypeVisitorInTaint(builder, param, taintPrimTypeValue) {
        private val taintOut = mutableListOf<ILocalT<*>>() // Lhs
        override fun visit(accessPath: ILocalT<*>, paramType: Type) {
            taintOut += accessPath
        }
        override fun visit(t: TypeHandler.OtherClassType) {
            if (!JavaeeAnnotationSource.isWebModelClassType(t)) {
                return
            }
            with(builder) {
                // taint to object instance base
                visit(param, t.type)
                // taint to all the declaring fields
                visit(param.subFields, t.type)
            }
        }

        fun get(hType: TypeHandler.HType): List<ILocalT<*>> {
            taintOut.clear()
            hType.visit(this)
            return taintOut
        }
    }
}

