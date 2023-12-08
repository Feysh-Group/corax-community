package com.feysh.corax.config.community.checkers.jwt

import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.api.baseimpl.matchSimpleSig
import com.feysh.corax.config.community.IncompleteModelOfEndpointFeatures
import soot.RefType
import soot.Scene
import soot.SootClass

object MissingJWTSignatureCheck : PreAnalysisUnit() {
    /**
     * either the qualifier of a call to the `parse(token)`,
     * `parseClaimsJwt(token)` or `parsePlaintextJwt(token)` methods or
     * the qualifier of a call to a `parse(token, handler)` method
     * where the `handler` is considered insecure.
     */
    context (PreAnalysisApi)
    override fun config() {
        val vulnHandlerDeclareType = mutableSetOf<String>()
        runInScene {
            val jwtHandlerSc = Scene.v().getSootClassUnsafe("io.jsonwebtoken.JwtHandler") ?: return@runInScene
            val jwtHandlerAdapterSc: SootClass? = Scene.v().getSootClassUnsafe("io.jsonwebtoken.JwtHandlerAdapter")
            val missingSigCheckMethodNames = setOf("onClaimsJwt", "onPlaintextJwt")
            val subs = Scene.v().orMakeFastHierarchy.let { h -> h.getSubclassesOf(jwtHandlerSc) +
                    (jwtHandlerAdapterSc?.let { h.getSubclassesOf(it) } ?: emptyList() )
            }
            for (subJwtHandler in subs) {
                if (subJwtHandler == jwtHandlerAdapterSc) {
                    continue
                }
                val overrideMethods = subJwtHandler.methods.filter { it.name in missingSigCheckMethodNames }
                if (overrideMethods.isNotEmpty()) {
                    vulnHandlerDeclareType.add(subJwtHandler.name)
                }
            }
        }


        listOf(
            matchSimpleSig("io.jsonwebtoken.JwtParser: * parse(String accessToken)"),
            matchSimpleSig("io.jsonwebtoken.JwtParser: * parseClaimsJwt(String claimsJwt)"),
            matchSimpleSig("io.jsonwebtoken.JwtParser: * parsePlaintextJwt(String plaintextJwt)")
        ).forEach {
            atInvoke(it) {
                report(IncompleteModelOfEndpointFeatures.HasMissingJwtSignatureCheck)
            }
        }

        atInvoke(matchSimpleSig("io.jsonwebtoken.JwtParser: * parse(String accessToken, JwtHandler JwtHandler)")) {
            val handlerTypes = this.invokeExpr?.getArg(1)?.possibleTypes ?: return@atInvoke
            val handlerTypeStrings = handlerTypes.mapNotNullTo(mutableSetOf()) { (it as? RefType)?.className }
            val intersection = handlerTypeStrings.intersect(vulnHandlerDeclareType)
            if (intersection.isNotEmpty()) {
                report(IncompleteModelOfEndpointFeatures.HasMissingJwtSignatureCheck)
            }
        }
    }
}
