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

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


interface IMethodSignature {
    val signature: String
    val subtypes: Boolean
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

interface IMethodAccessPath : IMethodSignature {
    @Required
    val arg: String
}

interface IMethodAccessPathGrouped: IMethodGrouped, IMethodAccessPath

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
) : IMethodAccessPathGrouped, ISelectable

