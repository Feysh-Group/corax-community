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

package com.feysh.corax.config.general.model

import com.feysh.corax.config.api.*
import com.feysh.corax.config.api.baseimpl.matchSoot
import com.feysh.corax.config.general.utils.primTypesBoxedQuotedString


@Suppress("ClassName")
object `outstanding-summaries` : AIAnalysisUnit() {
    context(AIAnalysisApi) override suspend fun config() {
        method(matchSoot("<java.lang.Object: java.lang.Object clone()>"))
            .modelNoArg {
                `return`.field(MapKeys).value = `this`.field(MapKeys).value
                `return`.field(MapValues).value = `this`.field(MapValues).value
                `return`.field(Elements).value = `this`.field(Elements).value
                `return`.subFields.value = `this`.subFields.value
                `return`.taint = `this`.taint
            }

        if (ConfigCenter.option.taintToStringMethod){
            // taint modeling of toString that declaring in container classes
            eachMethod {
                val sootMethod = this.sootMethod
                if (sootMethod.isStatic || sootMethod.isStaticInitializer ||
                    sootMethod.isSynchronized || sootMethod.isJavaLibraryMethod) {
                    return@eachMethod
                }
                val thisType = sootMethod.declaringClass.type
                if (ConfigCenter.isCollectionClassType(thisType) || ConfigCenter.isOptionalClassType(thisType)) {
                    modelNoArg {
                        `return`.taint += `this`.field(Elements).taint
                    }
                } else if (ConfigCenter.isMapClassType(sootMethod.declaringClass.type)) {
                    modelNoArg {
                        `return`.taint += `this`.field(MapKeys).taint + `this`.field(MapValues).taint
                    }
                }
            }

            for (boxedPrimitiveType in primTypesBoxedQuotedString) {
                method(matchSoot("<$boxedPrimitiveType: java.lang.String toString()>"))
                    .modelNoArg {
                        `return`.taint = `this`.taint
                    }
            }


            /* case
            *  char[] chars = cmd.toCharArray();
            *  Runtime.getRuntime().exec(chars.toString()); // CommandInjection
            */
            method(matchSoot("<java.lang.Object: java.lang.String toString()>"))
                .modelNoArg {
                    `return`.taint += `this`.taint + `this`.field(MapKeys).taint + `this`.field(MapValues).taint + `this`.field(Elements).taint
                }
        }
    }
}