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

package com.feysh.corax.config.general.rule

import com.feysh.corax.config.general.utils.methodMatch
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import soot.Scene
import soot.SootMethod


interface IMethodSignature {
    val signature: String
    val subtypes: Boolean
}

interface IRuleExt {
    val provenance: String
    val ext: String
}

interface ISelectable : IMethodSignature {
    val enable: Boolean
}

interface IMethodGrouped : IMethodSignature {
    @Required
    @SerialName("kind")
    val group: String
}

interface IVersionConditionsGrouped {
    @Required
    @SerialName("key")
    val key: String
}

interface IMethodAccessPath : IMethodSignature, IRuleExt {
    @Required
    val arg: String
}

interface IMultiMethodAccessPath : IMethodSignature, IRuleExt {
    @Required
    val args: List<String>
}

interface IMethodAccessPathGrouped: IMethodGrouped, IMethodAccessPath
interface IMultiMethodAccessPathGrouped: IMethodGrouped, IMultiMethodAccessPath

@Serializable
data class MethodAccessPath(
    override val enable: Boolean = true,
    @Required
    @SerialName("kind")
    override val group: String,
    @Required
    override val signature: String,
    @Required
    override val subtypes: Boolean,
    override val arg: String = "",
    override val provenance: String,
    override val ext: String
) : IMethodAccessPathGrouped, ISelectable, IRuleExt


@Serializable
data class MultiMethodAccessPath(
    override val enable: Boolean = true,
    @Required
    @SerialName("kind")
    override val group: String,
    @Required
    override val signature: String,
    @Required
    override val subtypes: Boolean,
    override val args: List<String> = emptyList(),
    override val provenance: String,
    override val ext: String
) : IMultiMethodAccessPathGrouped, ISelectable, IRuleExt


@Serializable
open class MethodSignature(
    override val enable: Boolean = true,
    @Required
    override val signature: String,
    @Required
    override val subtypes: Boolean = true,
    override val provenance: String = "",
    override val ext: String = ""
) : IMethodSignature, ISelectable, IRuleExt {
    val matchesMethods: List<SootMethod> get() = matchesMethods(scene = Scene.v())
    fun matchesMethods(scene: Scene): List<SootMethod> = this.takeIf { it.enable }?.methodMatch?.matched(scene = scene) ?: emptyList()
}
