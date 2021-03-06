package com.dslplatform.client;

import java.io.IOException;
import java.nio.charset.Charset;

import akka.serialization.JSerializer;

import com.dslplatform.client.AkkaSerialization;
import com.dslplatform.client.Bootstrap;
import com.dslplatform.client.JsonSerialization;
import com.dslplatform.patterns.ServiceLocator;
import com.fasterxml.jackson.databind.JavaType;

public class AkkaSerialization extends JSerializer {
    private final ServiceLocator locator;
    private final JsonSerialization jsonSerialization;

    public AkkaSerialization() {
        locator = Bootstrap.getLocator();
        jsonSerialization = locator.resolve(JsonSerialization.class);
    }

    @Override
    public int identifier() {
        return 1025;
    }

    @Override
    public boolean includeManifest() {
        return false;
    }

    @Override
    public byte[] toBinary(final Object obj) {
        try {
            final String json = jsonSerialization.serialize(obj);
            return (obj.getClass().getName() + ':' + json).getBytes("UTF-8");
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object fromBinaryJava(final byte[] bytes, final Class<?> notUsed) {
        try {
            final String body = new String(bytes, Charset.forName("UTF-8"));
            final int split = body.indexOf(':');
            final Class<?> clazz = Class.forName(body.substring(0, split));
            final JavaType type = JsonSerialization.buildType(clazz);
            final String json = body.substring(split + 1);
            return jsonSerialization.deserialize(type, json, locator);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
