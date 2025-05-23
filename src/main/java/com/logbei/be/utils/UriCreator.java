package com.logbei.be.utils;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public class UriCreator {
    public static URI createUri(String defaultUrl, long resourceId) {
        return UriComponentsBuilder
                .newInstance()
                .path(defaultUrl + "/{resource-id}")
                .buildAndExpand(resourceId)
                .toUri();
    }

    public static URI createUri(Long questionId) {
        return UriComponentsBuilder
                .newInstance()
                .path("/questions/{question-id}/answers")
                .buildAndExpand(questionId)
                .toUri();
    }

    public static URI createUri(String defaultUrl) {
        return UriComponentsBuilder
                .newInstance()
                .path(defaultUrl)
                .build()
                .toUri();
    }
}
