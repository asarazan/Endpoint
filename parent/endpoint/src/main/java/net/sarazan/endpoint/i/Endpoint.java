package net.sarazan.endpoint.i;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Aaron Sarazan on 5/13/14
 * Copyright(c) 2014 Manotaur, LLC.
 */
public interface Endpoint<R> {

    @NotNull Response<R> fetch();

}
