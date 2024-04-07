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

package com.feysh.corax.config.community.standard

import com.feysh.corax.config.api.IRule
import com.feysh.corax.config.api.IStandard
import com.feysh.corax.config.builtin.standard.BuiltinGeneralStandard


enum class CERTRules(
    override val realName: String,
    override val desc: String
) : IRule {

    IDS00_J("IDS00-J", "SQL injection vulnerabilities arise in applications where elements of a SQL query originate from an untrusted source"),
    IDS03_J("IDS03-J", "Do not log unsanitized user input"),
    IDS07_J("IDS07-J", "Sanitize untrusted data passed to the Runtime.exec() method"),
    IDS51_J("IDS51-J", "Properly encode or escape output"),
    IDS54_J("IDS54-J", "Prevent LDAP injection"),

    IDS56_J("IDS56-J", "Prevent arbitrary file upload"),

    MSC02_J("MSC02-J", "Generate strong random numbers"),

    MSC61_J("MSC61-J", "Do not use insecure or weak cryptographic algorithms"),

    FIO16_J("FIO16-J", "Canonicalize path names before validating them"),
    ;

    override val standard: IStandard = BuiltinGeneralStandard.CERT

}