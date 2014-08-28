package net.sarazan.endpoint.impl.endpoint;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;

import net.sarazan.endpoint.factory.Responses;
import net.sarazan.endpoint.i.Endpoint;
import net.sarazan.endpoint.i.Processor;
import net.sarazan.endpoint.i.Response;
import net.sarazan.endpoint.i.Serializer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aaron Sarazan on 5/18/14
 * Copyright(c) 2014 Manotaur, LLC.
 */
public abstract class BaseEndpoint<REQ, RES> implements Endpoint<RES> {

    public enum Method {
        GET,
        POST,
        // TODO
    }

    private static final String TAG = "BaseEndpoint";

    private Serializer<REQ, RES> mSerializer;
    private Processor mProcessor;

    @NotNull
    protected abstract URL getURL();

    @Nullable
    protected abstract REQ getRequest();

    @NotNull
    @Override
    public Response<RES> fetch() {
        OkHttpClient client = new OkHttpClient();
        Request.Builder b = new Builder().url(getURL());
        Map<String, String> headers = applyHeaders(new HashMap<String, String>());
        for (String key : headers.keySet()) {
            b.header(key, headers.get(key));
        }
        if (mProcessor != null) {
            mProcessor.processRequest(b);
        }
        InputStream is = null;
        try {
            switch (method()) {
                case GET:
                    b.get();
                    break;
                case POST:
                    REQ request = getRequest();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    mSerializer.serializeRequest(bos, request);
                    b.post(RequestBody.create(MediaType.parse(contentType()), bos.toByteArray()));
                    break;
            }
            Request req = b.build();
            Call call = client.newCall(req);
            com.squareup.okhttp.Response res = call.execute();
            if (!res.isSuccessful()) {
                return fail(Responses.<RES>failedResponse(res.code()));
            }
            is = res.body().byteStream();
            RES response = mSerializer.deserializeResponse(is);
            return succeed(Responses.createResponse(response));
        } catch (IOException e) {
            fail(Responses.<RES>failedResponse(e));
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ignored) {}
        }
        return Responses.failedResponse(new Exception("Unknown Error"));
    }

    @NotNull
    protected Map<String, String> applyHeaders(@NotNull Map<String, String> headers) {
        return headers;
    }

    @NotNull
    protected Method method() {
        return Method.GET;
    }

    @NotNull
    protected String contentType() {
        return "text/plain";
    }

    private Response<RES> succeed(Response<RES> response) {
        onSuccess(response);
        return response;
    }

    private Response<RES> fail(Response<RES> response) {
        onFailure(response);
        return response;
    }

    protected void onSuccess(Response<RES> response) {

    }

    protected void onFailure(Response<RES> response) {

    }

    public Serializer<REQ, RES> getSerializer() {
        return mSerializer;
    }

    public void setSerializer(Serializer<REQ, RES> serializer) {
        mSerializer = serializer;
    }

    public Processor getProcessor() {
        return mProcessor;
    }

    public void setProcessor(Processor processor) {
        mProcessor = processor;
    }

    @Override
    public String toString() {
        return getURL().getPath();
    }
}
