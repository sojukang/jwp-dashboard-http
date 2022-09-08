package org.apache.coyote.http11.response;

import java.util.StringJoiner;

import org.apache.coyote.http11.request.HttpRequest;

class StatusLine {

    private static final String STATUS_LINE_DELIMITER = " ";
    private static final String STATUS_LINE_SUFFIX = " ";

    private final String httpVersion;
    private final StatusCode statusCode;

    StatusLine(HttpRequest request, StatusCode statusCode) {
        this.httpVersion = request.getHttpVersion();
        this.statusCode = statusCode;
    }

    static StatusLine of(HttpRequest request, StatusCode statusCode) {
        return new StatusLine(request, statusCode);
    }

    String generate() {
        StringJoiner joiner = new StringJoiner(STATUS_LINE_DELIMITER, "", STATUS_LINE_SUFFIX);
        return joiner.add(httpVersion)
            .add(String.valueOf(statusCode.getCode()))
            .add(statusCode.name())
            .toString();
    }
}
