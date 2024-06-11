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

@file:Suppress("NOTHING_TO_INLINE")

package com.feysh.corax.config.general.common.collect

import com.google.common.collect.Multimap
import com.google.common.collect.Multimaps

typealias MultiMap<K, V> = Multimap<K, V>
typealias Supplier<T> = com.google.common.base.Supplier<T>

inline fun <K, V> MultiMap<K, V>.forEachCollection(action: (K, Collection<V>) -> Unit) {
    for (k in this.keys()) {
        val c = get(k)
        action(k, c)
    }
}


object Maps {

    inline fun <K, V> newMap(): MutableMap<K, V> {
        return java.util.HashMap()
    }

    inline fun <K, V> newHybridMap(): MutableMap<K, V> = HashMap()

    inline fun <K, V> newMultiMap(map: Map<K, Collection<V>>, setFactory: Supplier<Collection<V>>): MultiMap<K, V> {
        return Multimaps.newMultimap(map, setFactory)
    }

    inline fun <K, V> newMultiMap(map: Map<K, Collection<V>>): MultiMap<K, V> {
        return newMultiMap(map, Sets::newHybridSet)
    }

    fun <K, V> newMultiMap(): MultiMap<K, V> {
        return newMultiMap(newMap(), Sets::newHybridSet)
    }
}