package com.psbc.actuator.v2;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTags;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

@Component
@Primary
@EnableConfigurationProperties(CustomMetricsProperties.class)
public class CustomWebMvcTagsProvider implements WebMvcTagsProvider {

    private static final String DATA_REST_PATH_PATTERN_ATTRIBUTE = "org.springframework.data.rest.webmvc.RepositoryRestHandlerMapping.EFFECTIVE_REPOSITORY_RESOURCE_LOOKUP_PATH";

    private static final Tag URI_NOT_FOUND = Tag.of("uri", "NOT_FOUND");

    private static final Tag URI_REDIRECTION = Tag.of("uri", "REDIRECTION");

    private static final Tag URI_ROOT = Tag.of("uri", "root");

    private static final Tag URI_UNKNOWN = Tag.of("uri", "UNKNOWN");

    private static final Pattern TRAILING_SLASH_PATTERN = Pattern.compile("/$");

    private static final Pattern MULTIPLE_SLASH_PATTERN = Pattern.compile("//+");

    private static CustomMetricsProperties customMetricsProperties;

    private final boolean ignoreTrailingSlash;

    public CustomWebMvcTagsProvider() {
        this(false);
    }

    public CustomWebMvcTagsProvider(boolean ignoreTrailingSlash) {
        this.ignoreTrailingSlash = ignoreTrailingSlash;
    }

    @Autowired
    public CustomWebMvcTagsProvider(CustomMetricsProperties customMetricsProperties) {
        this();
        CustomWebMvcTagsProvider.customMetricsProperties = customMetricsProperties;
    }

    @Override
    public Iterable<Tag> getTags(HttpServletRequest request, HttpServletResponse response, Object handler,
                                 Throwable exception) {
        return Tags.of(WebMvcTags.method(request), uri(request, response, this.ignoreTrailingSlash),
                WebMvcTags.exception(exception), WebMvcTags.status(response), WebMvcTags.outcome(response));
    }

    @Override
    public Iterable<Tag> getLongRequestTags(HttpServletRequest request, Object handler) {
        return Tags.of(WebMvcTags.method(request), uri(request, null, this.ignoreTrailingSlash));
    }

    private static String getMatchingPattern(HttpServletRequest request) {
        PathPattern dataRestPathPattern = (PathPattern) request.getAttribute(DATA_REST_PATH_PATTERN_ATTRIBUTE);
        if (dataRestPathPattern != null) {
            return dataRestPathPattern.getPatternString();
        }
        /**
         * modified by lk 2022 9 28
         * do not record tag with PATH BEFORE MAPPING
         */
//		return (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    }

    private static HttpStatus extractStatus(HttpServletResponse response) {
        try {
            return HttpStatus.valueOf(response.getStatus());
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String getPathInfo(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        String uri = StringUtils.hasText(pathInfo) ? pathInfo : "/";
        uri = MULTIPLE_SLASH_PATTERN.matcher(uri).replaceAll("/");
        return TRAILING_SLASH_PATTERN.matcher(uri).replaceAll("");
    }

    private static Tag uri(HttpServletRequest request, HttpServletResponse response, boolean ignoreTrailingSlash) {
        return customUri(request,response,ignoreTrailingSlash, customMetricsProperties.enabled);
    }

    private static Tag customUri(HttpServletRequest request, HttpServletResponse response, boolean ignoreTrailingSlash, boolean uriFilter) {
        if (request != null) {
            String pattern = getMatchingPattern(request);
            if (pattern != null) {
                if (ignoreTrailingSlash) {
                    pattern = TRAILING_SLASH_PATTERN.matcher(pattern).replaceAll("");
                }
                if (uriFilter) {
                    return customMetricsProperties.monitored.contains(pattern) ? Tag.of("uri", pattern) : Tag.of("uri", customMetricsProperties.defaultName);
                } else {
                    return Tag.of("uri", pattern);
                }
            }
            if (response != null) {
                HttpStatus status = extractStatus(response);
                if (status != null) {
                    if (status.is3xxRedirection()) {
                        return URI_REDIRECTION;
                    }
                    if (status == HttpStatus.NOT_FOUND) {
                        return URI_NOT_FOUND;
                    }
                }
            }
            String pathInfo = getPathInfo(request);
            if (pathInfo.isEmpty()) {
                return URI_ROOT;
            } else {
                if (uriFilter) {
                    return customMetricsProperties.monitored.contains(pathInfo) ? Tag.of("uri", pathInfo) : Tag.of("uri", customMetricsProperties.defaultName);
                } else {
                    return Tag.of("uri", pathInfo);
                }
            }
        }
        return URI_UNKNOWN;
    }

}