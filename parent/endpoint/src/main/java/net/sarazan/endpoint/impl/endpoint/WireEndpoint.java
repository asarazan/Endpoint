package net.sarazan.endpoint.impl.endpoint;

import com.squareup.wire.Message;

import net.sarazan.endpoint.impl.serializer.WireSerializer;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Aaron Sarazan on 5/13/14
 * Copyright(c) 2014 Manotaur, LLC.
 */
public abstract class WireEndpoint<T extends Message, R extends Message> extends BaseEndpoint<T, R> {

    private static final String TAG = "WireEndpoint";

    protected WireEndpoint(@NotNull Class<R> cls) {
        super();
        setSerializer(new WireSerializer<T, R>(cls));
    }

    @NotNull
    @Override
    protected Method method() {
        return Method.POST;
    }

    @NotNull
    @Override
    protected String contentType() {
        return "application/x-protobuf";
    }
}
