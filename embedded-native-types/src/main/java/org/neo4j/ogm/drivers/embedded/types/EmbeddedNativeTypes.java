/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */
package org.neo4j.ogm.drivers.embedded.types;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.neo4j.ogm.driver.TypeAdapterLookupDelegate;
import org.neo4j.ogm.driver.TypeSystem;
import org.neo4j.ogm.drivers.embedded.types.adapter.EmbeddedValueToPointAdapter;
import org.neo4j.ogm.drivers.embedded.types.adapter.PointToEmbeddedValueAdapter;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;
import org.neo4j.values.storable.DateValue;
import org.neo4j.values.storable.LocalDateTimeValue;
import org.neo4j.values.storable.PointValue;
import org.neo4j.values.storable.Values;

/**
 * @author Michael J. Simons
 */
class EmbeddedNativeTypes implements TypeSystem {

    private final TypeAdapterLookupDelegate nativeToMappedAdapter;
    private final TypeAdapterLookupDelegate mappedToNativeAdapter;

    EmbeddedNativeTypes() {

        Map<Class<?>, Function> nativeToMappedAdapter = new HashMap<>();
        Map<Class<?>, Function> mappedToNativeAdapter = new HashMap<>();

        addSpatialFeatures(nativeToMappedAdapter, mappedToNativeAdapter);
        addJavaTimeFeature(nativeToMappedAdapter, mappedToNativeAdapter);

        this.nativeToMappedAdapter = new TypeAdapterLookupDelegate(nativeToMappedAdapter);
        this.mappedToNativeAdapter = new TypeAdapterLookupDelegate(mappedToNativeAdapter);
    }

    private static void addSpatialFeatures(Map<Class<?>, Function> nativeToMappedAdapter,
        Map<Class<?>, Function> mappedToNativeAdapter) {

        nativeToMappedAdapter.put(PointValue.class, new EmbeddedValueToPointAdapter());

        PointToEmbeddedValueAdapter pointToEmbeddedValueAdapter = new PointToEmbeddedValueAdapter();
        mappedToNativeAdapter.put(CartesianPoint2d.class, pointToEmbeddedValueAdapter);
        mappedToNativeAdapter.put(CartesianPoint3d.class, pointToEmbeddedValueAdapter);
        mappedToNativeAdapter.put(GeographicPoint2d.class, pointToEmbeddedValueAdapter);
        mappedToNativeAdapter.put(GeographicPoint3d.class, pointToEmbeddedValueAdapter);
    }

    private static void addJavaTimeFeature(Map<Class<?>, Function> nativeToMappedAdapter,
        Map<Class<?>, Function> mappedToNativeAdapter) {

        nativeToMappedAdapter.put(DateValue.class, (Function<DateValue, LocalDate>) v -> v.asObjectCopy());
        nativeToMappedAdapter.put(LocalDateTimeValue.class, (Function<LocalDateTimeValue, LocalDateTime>) v -> v.asObjectCopy());

        mappedToNativeAdapter.put(LocalDate.class, v -> Values.of(v));
        mappedToNativeAdapter.put(LocalDateTime.class, v -> Values.of(v));
    }

    public boolean supportsAsNativeType(Class<?> clazz) {
        return mappedToNativeAdapter.hasAdapterFor(clazz);
    }

    @Override
    public Function<Object, Object> getNativeToMappedTypeAdapter(Class<?> clazz) {
        return nativeToMappedAdapter.findAdapterFor(clazz);
    }

    @Override
    public Function<Object, Object> getMappedToNativeTypeAdapter(Class<?> clazz) {
        return mappedToNativeAdapter.findAdapterFor(clazz);
    }
}
