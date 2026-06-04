package com.numpty.framework.infra.api;

import org.bson.conversions.Bson;

public record FindOptions(Bson sort, Integer skip, Integer limit) {

    public static FindOptions empty() {
        return new FindOptions(null, null, null);
    }

    public static FindOptions sort(Bson sort) {
        return new FindOptions(sort, null, null);
    }

    public static FindOptions page(int page, int size) {
        return new FindOptions(null, page * size, size);
    }

    public FindOptions withSort(Bson sort) {
        return new FindOptions(sort, this.skip, this.limit);
    }

    public FindOptions withSkip(int skip) {
        return new FindOptions(this.sort, skip, this.limit);
    }

    public FindOptions withLimit(int limit) {
        return new FindOptions(this.sort, this.skip, limit);
    }
}
