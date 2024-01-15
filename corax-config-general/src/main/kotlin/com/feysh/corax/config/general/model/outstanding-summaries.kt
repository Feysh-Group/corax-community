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