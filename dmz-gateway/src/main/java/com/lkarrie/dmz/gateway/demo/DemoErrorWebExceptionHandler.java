package com.lkarrie.dmz.gateway.demo;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.all;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Basic global {@link org.springframework.web.server.WebExceptionHandler}, rendering
 * {@link ErrorAttributes}.
 * <p>
 * More specific errors can be handled either using Spring WebFlux abstractions (e.g.
 * {@code @ExceptionHandler} with the annotation model) or by adding
 * {@link RouterFunction} to the chain.
 * <p>
 * This implementation will render error as HTML views if the client explicitly supports
 * that media type. It attempts to resolve error views using well known conventions. Will
 * search for templates and static assets under {@code '/error'} using the
 * {@link HttpStatus status code} and the {@link HttpStatus#series() status series}.
 * <p>
 * For example, an {@code HTTP 404} will search (in the specific order):
 * <ul>
 * <li>{@code '/<templates>/error/404.<ext>'}</li>
 * <li>{@code '/<static>/error/404.html'}</li>
 * <li>{@code '/<templates>/error/4xx.<ext>'}</li>
 * <li>{@code '/<static>/error/4xx.html'}</li>
 * <li>{@code '/<templates>/error/error'}</li>
 * <li>{@code '/<static>/error/error.html'}</li>
 * </ul>
 * <p>
 * If none found, a default "Whitelabel Error" HTML view will be rendered.
 * <p>
 * If the client doesn't support HTML, the error information will be rendered as a JSON
 * payload.
 *
 * @author Brian Clozel
 * @author Scott Frederick
 * @since 2.0.0
 */

/**
 * 参考 DefaultErrorWebExceptionHandler
 * 统一异常处理思路：
 * 目前的版本的 AbstractErrorWebExceptionHandler 提供了供子类使用的获取请求异常的方法 getError
 * 可以在子类 渲染JSON response renderErrorResponse方法中 获取当请求异常类型 根据异常类型重写报错信息即可
 */
public class DemoErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    private static final MediaType TEXT_HTML_UTF8 = new MediaType("text", "html", StandardCharsets.UTF_8);

    private static final Map<HttpStatus.Series, String> SERIES_VIEWS;

    static {
        Map<HttpStatus.Series, String> views = new EnumMap<>(HttpStatus.Series.class);
        views.put(HttpStatus.Series.CLIENT_ERROR, "4xx");
        views.put(HttpStatus.Series.SERVER_ERROR, "5xx");
        SERIES_VIEWS = Collections.unmodifiableMap(views);
    }

    private final ErrorProperties errorProperties;

    /**
     * Create a new {@code DefaultErrorWebExceptionHandler} instance.
     * @param errorAttributes the error attributes
     * @param resources the resources configuration properties
     * @param errorProperties the error configuration properties
     * @param applicationContext the current application context
     * @since 2.4.0
     */
    public DemoErrorWebExceptionHandler(ErrorAttributes errorAttributes, Resources resources,
                                        ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resources, applicationContext);
        this.errorProperties = errorProperties;
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        // 根据 请求头Accept类型 渲染不同类型请求返回 支持html则渲染html返回 其余统一渲染JSON格式返回
        return route(acceptsTextHtml(), this::renderErrorView).andRoute(all(), this::renderErrorResponse);
    }

    /**
     * Render the error information as an HTML view.
     * @param request the current request
     * @return a {@code Publisher} of the HTTP response
     */
    protected Mono<ServerResponse> renderErrorView(ServerRequest request) {
        Map<String, Object> error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.TEXT_HTML));
        int errorStatus = getHttpStatus(error);
        ServerResponse.BodyBuilder responseBody = ServerResponse.status(errorStatus).contentType(TEXT_HTML_UTF8);
        return Flux.just(getData(errorStatus).toArray(new String[] {}))
                .flatMap((viewName) -> renderErrorView(viewName, responseBody, error))
                .switchIfEmpty(this.errorProperties.getWhitelabel().isEnabled()
                        ? renderDefaultErrorView(responseBody, error) : Mono.error(getError(request)))
                .next();
    }

    private List<String> getData(int errorStatus) {
        List<String> data = new ArrayList<>();
        data.add("error/" + errorStatus);
        HttpStatus.Series series = HttpStatus.Series.resolve(errorStatus);
        if (series != null) {
            data.add("error/" + SERIES_VIEWS.get(series));
        }
        data.add("error/error");
        return data;
    }

    /**
     * Render the error information as a JSON payload.
     * 获取异常类型 根据类型判断 覆盖异常信息
     * @param request the current request
     * @return a {@code Publisher} of the HTTP response
     */
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable throwable = this.getError(request);
        Map<String, Object> error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        // 默认返回异常信息
        String body = Constants.SYSTEM_UNAVAILABLE;
        // 异常统一处理
        if (throwable instanceof HttpClientErrorException) {
            HttpStatus httpStatus = ((HttpClientErrorException) throwable).getStatusCode();
            if(httpStatus == HttpStatus.FORBIDDEN){
                body = ((HttpClientErrorException) throwable).getStatusText();
            }
            // 处理返回码
            error.put("status",((HttpClientErrorException) throwable).getRawStatusCode());
            // 处理返回异常信息
            error.put("error",((HttpClientErrorException) throwable).getStatusCode());
            error.put("message",body);
        }
        return ServerResponse.status(getHttpStatus(error)).contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(error));
    }

    protected ErrorAttributeOptions getErrorAttributeOptions(ServerRequest request, MediaType mediaType) {
        ErrorAttributeOptions options = ErrorAttributeOptions.defaults();
        if (this.errorProperties.isIncludeException()) {
            options = options.including(Include.EXCEPTION);
        }
        if (isIncludeStackTrace(request, mediaType)) {
            options = options.including(Include.STACK_TRACE);
        }
        if (isIncludeMessage(request, mediaType)) {
            options = options.including(Include.MESSAGE);
        }
        if (isIncludeBindingErrors(request, mediaType)) {
            options = options.including(Include.BINDING_ERRORS);
        }
        return options;
    }

    /**
     * Determine if the stacktrace attribute should be included.
     * @param request the source request
     * @param produces the media type produced (or {@code MediaType.ALL})
     * @return if the stacktrace attribute should be included
     */
    protected boolean isIncludeStackTrace(ServerRequest request, MediaType produces) {
        switch (this.errorProperties.getIncludeStacktrace()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
                return isTraceEnabled(request);
            default:
                return false;
        }
    }

    /**
     * Determine if the message attribute should be included.
     * @param request the source request
     * @param produces the media type produced (or {@code MediaType.ALL})
     * @return if the message attribute should be included
     */
    protected boolean isIncludeMessage(ServerRequest request, MediaType produces) {
        switch (this.errorProperties.getIncludeMessage()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
                return isMessageEnabled(request);
            default:
                return false;
        }
    }

    /**
     * Determine if the errors attribute should be included.
     * @param request the source request
     * @param produces the media type produced (or {@code MediaType.ALL})
     * @return if the errors attribute should be included
     */
    protected boolean isIncludeBindingErrors(ServerRequest request, MediaType produces) {
        switch (this.errorProperties.getIncludeBindingErrors()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
                return isBindingErrorsEnabled(request);
            default:
                return false;
        }
    }

    /**
     * Get the HTTP error status information from the error map.
     * @param errorAttributes the current error information
     * @return the error HTTP status
     */
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        return (int) errorAttributes.get("status");
    }

    /**
     * Predicate that checks whether the current request explicitly support
     * {@code "text/html"} media type.
     * <p>
     * The "match-all" media type is not considered here.
     * @return the request predicate
     */
    protected RequestPredicate acceptsTextHtml() {
        return (serverRequest) -> {
            try {
                List<MediaType> acceptedMediaTypes = serverRequest.headers().accept();
                acceptedMediaTypes.removeIf(MediaType.ALL::equalsTypeAndSubtype);
                MediaType.sortBySpecificityAndQuality(acceptedMediaTypes);
                return acceptedMediaTypes.stream().anyMatch(MediaType.TEXT_HTML::isCompatibleWith);
            }
            catch (InvalidMediaTypeException ex) {
                return false;
            }
        };
    }

}
