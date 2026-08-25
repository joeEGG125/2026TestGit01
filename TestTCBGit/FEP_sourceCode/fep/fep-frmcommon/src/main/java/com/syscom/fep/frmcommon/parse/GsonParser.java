package com.syscom.fep.frmcommon.parse;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.syscom.fep.frmcommon.util.ExceptionUtil;

import java.lang.reflect.Type;

public class GsonParser<T> implements Parser<T, String> {
    private final Gson gson;
    private final Type type;

    public GsonParser(Type type) {
        this(null, type);
    }

    public GsonParser(Gson gson, Type type) {
        this.gson = gson == null ? new Gson() : gson;
        this.type = type;
    }

    @Override
    public T readIn(String jsonStr) throws Exception {
        try {
            return gson.fromJson(jsonStr, this.type);
        } catch (JsonSyntaxException e) {
            throw ExceptionUtil.createException(e, e.getMessage());
        }
    }

    @Override
    public String writeOut(T t) throws Exception {
        return gson.toJson(t);
    }

    public final Type getType() {
        return type;
    }
}