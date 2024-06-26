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

package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.api.MethodConfig
import com.feysh.corax.config.api.baseimpl.matchMethod
import com.feysh.corax.config.api.baseimpl.matchSoot
import com.feysh.corax.config.api.utils.sootTypeName
import com.feysh.corax.config.api.utils.typename
import com.feysh.corax.config.community.OpenRedirectChecker
import com.feysh.corax.config.general.checkers.internetControl
import com.feysh.corax.config.general.model.javaee.JavaeeFrameworkConfigs
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.AbstractUrlBasedView
import org.springframework.web.servlet.view.RedirectView

// has tests
@Suppress("ClassName")
object `open-redirect` : AIAnalysisUnit() {

    private val modelAndViewType: String = org.springframework.web.servlet.ModelAndView::class.sootTypeName

    context (AIAnalysisApi)
    override suspend fun config() {
        listOf(
            matchMethod(javax.servlet.http.HttpServletResponse::sendRedirect) to 0,
            matchMethod(javax.servlet.http.HttpServletResponseWrapper::sendRedirect) to 0,

            // request dispatcher
            matchSoot("<javax.servlet.RequestDispatcher: void forward(javax.servlet.ServletRequest,javax.servlet.ServletResponse)>") to -1,
            matchSoot("<javax.servlet.RequestDispatcher: void include(javax.servlet.ServletRequest,javax.servlet.ServletResponse)>") to -1,

        ).forEach { (method, location) ->
            method(method).modelNoArg {
                check(
                    parameter(location).taint.containsAll(taintOf(internetControl)),
                    OpenRedirectChecker.UnvalidatedRedirect
                )
            }
        }

        listOf(
            method(javax.servlet.http.HttpServletResponse::addHeader),
            method(javax.servlet.http.HttpServletResponse::setHeader)
        ).forEach {
            it.model { name, value ->
                check(
                    name.getString().toLowerCase().stringEquals("location")
                            and value.taint.containsAll(taintOf(internetControl)),
                    OpenRedirectChecker.UnvalidatedRedirect
                )
            }
        }


        eachMethod {
            val visibilityAnnotationTag = visibilityAnnotationTag ?: return@eachMethod
            if (!visibilityAnnotationTag.hasAnnotations()) {
                return@eachMethod
            }
            for (annotation in visibilityAnnotationTag.annotations) {
                when (annotation.type) {
                    in JavaeeFrameworkConfigs.option.REQUEST_MAPPING_ANNOTATION_TYPES -> {
                        when (sootMethod.returnType.typename) {
                            null -> {}
                            "java.lang.String" -> {
                                /*
                                * @RequestMapping("/redirect2")
                                * public String redirect(@RequestParam("url") String url) { return "redirect:" + url; }
                                * */
                                this.modelNoArg(config = {
                                    at = MethodConfig.CheckCall.PostCallInCallee
                                }) {
                                    check(
                                        `return`.getString().contains("redirect:") and
                                                `return`.taint.containsAll(taintOf(internetControl)),
                                        OpenRedirectChecker.UnvalidatedRedirect
                                    )
                                }
                            }

                            modelAndViewType -> {
                                this.modelNoArg(config = {
                                    at = MethodConfig.CheckCall.PostCallInCallee
                                }) {
                                    /*
                                    * @RequestMapping("/redirect4")
                                    * public ModelAndView redirect4(@RequestParam("url") String url) {
                                    *     return new ModelAndView("redirect:" + url);
                                    * } */
                                    check(
                                        `return`.field(ModelAndView::view, Any::class).getString().contains("redirect:") and
                                                `return`.taint.containsAll(taintOf(internetControl)),
                                        OpenRedirectChecker.UnvalidatedRedirect
                                    )

                                    /*
                                    * @RequestMapping("/redirect4")
                                    * public ModelAndView redirect4(@RequestParam("url") String url) {
                                    *     return new RedirectView(url);
                                    * } */
                                    check(`return`.field(ModelAndView::view, Any::class).isInstanceOf(RedirectView::class.sootTypeName) and
                                            `return`.field(ModelAndView::view, Any::class).field(AbstractUrlBasedView::url, String::class).taint.containsAll(taintOf(internetControl)),
                                        OpenRedirectChecker.UnvalidatedRedirect
                                    )
                                }
                            }
                        }
                        break
                    }
                }
            }

        }
    }
}