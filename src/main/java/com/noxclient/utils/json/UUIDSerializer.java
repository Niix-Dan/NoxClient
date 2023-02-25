/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.UUID;

public class UUIDSerializer implements JsonSerializer<UUID>, JsonDeserializer<UUID> {
    @Override
    public JsonElement serialize(UUID src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public UUID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return UUID.fromString(json.getAsString());
    }
}
