//package org.springframework.boot.actuate.metrics.web.servlet;
//
//import java.io.IOException;
//import java.lang.reflect.AnnotatedElement;
//import java.util.Collections;
//import java.util.Set;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import io.micrometer.core.annotation.Timed;
//import io.micrometer.core.instrument.MeterRegistry;
//import io.micrometer.core.instrument.Timer;
//import io.micrometer.core.instrument.Timer.Builder;
//import io.micrometer.core.instrument.Timer.Sample;
//
//import org.springframework.boot.actuate.metrics.AutoTimer;
//import org.springframework.core.annotation.MergedAnnotationCollectors;
//import org.springframework.core.annotation.MergedAnnotations;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.filter.OncePerRequestFilter;
//import org.springframework.web.method.HandlerMethod;
//import org.springframework.web.servlet.DispatcherServlet;
//import org.springframework.web.servlet.HandlerMapping;
//import org.springframework.web.util.NestedServletException;
//l
///**
// * Intercepts incoming HTTP requests and records metrics about Spring MVC execution time
// * and results.
// *
// * @author Jon Schneider
// * @author Phillip Webb
// * @since 2.0.0
// */
//public class WebMvcMetricsFilter extends OncePerRequestFilter {
//
//    private final MeterRegistry registry;
//
//    private final WebMvcTagsProvider tagsProvider;
//
//    private final String metricName;
//
//    private final AutoTimer autoTimer;
//
//    /**
//     * Create a new {@link WebMvcMetricsFilter} instance.
//     * @param registry the meter registry
//     * @param tagsProvider the tags provider
//     * @param metricName the metric name
//     * @param autoTimeRequests if requests should be automatically timed
//     * @since 2.0.7
//     * @deprecated since 2.2.0 in favor of
//     * {@link #WebMvcMetricsFilter(MeterRegistry, WebMvcTagsProvider, String, AutoTimer)}
//     */
//    @Deprecated
//    public WebMvcMetricsFilter(MeterRegistry registry, WebMvcTagsProvider tagsProvider, String metricName,
//                               boolean autoTimeRequests) {
//        this(registry, tagsProvider, metricName, AutoTimer.ENABLED);
//    }
//
//    /**
//     * Create a new {@link WebMvcMetricsFilter} instance.
//     * @param registry the meter registry
//     * @param tagsProvider the tags provider
//     * @param metricName the metric name
//     * @param autoTimer the auto-timers to apply or {@code null} to disable auto-timing
//     * @since 2.2.0
//     */
//    public WebMvcMetricsFilter(MeterRegistry registry, WebMvcTagsProvider tagsProvider, String metricName,
//                               AutoTimer autoTimer) {
//        this.registry = registry;
//        this.tagsProvider = tagsProvider;
//        this.metricName = metricName;
//        this.autoTimer = autoTimer;
//    }
//
//    @Override
//    protected boolean shouldNotFilterAsyncDispatch() {
//        return false;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        TimingContext timingContext = TimingContext.get(request);
//        if (timingContext == null) {
//            timingContext = startAndAttachTimingContext(request);
//        }
//        try {
//            filterChain.doFilter(request, response);
//            if (!request.isAsyncStarted()) {
//                // Only record when async processing has finished or never been started.
//                // If async was started by something further down the chain we wait
//                // until the second filter invocation (but we'll be using the
//                // TimingContext that was attached to the first)
//                Throwable exception = (Throwable) request.getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);
//                record(timingContext, request, response, exception);
//            }
//        }
//        catch (NestedServletException ex) {
//            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//            record(timingContext, request, response, ex.getCause());
//            throw ex;
//        }
//        catch (ServletException | IOException | RuntimeException ex) {
//            record(timingContext, request, response, ex);
//            throw ex;
//        }
//    }
//
//    private TimingContext startAndAttachTimingContext(HttpServletRequest request) {
//        Timer.Sample timerSample = Timer.start(this.registry);
//        TimingContext timingContext = new TimingContext(timerSample);
//        timingContext.attachTo(request);
//        return timingContext;
//    }
//
//    /**
//     * fix record func by lk
//     * @param timingContext
//     * @param request
//     * @param response
//     * @param exception
//     */
//    private void record(TimingContext timingContext, HttpServletRequest request, HttpServletResponse response,
//                        Throwable exception) {
//        Object handler = getHandler(request);
//        Set<Timed> annotations = getTimedAnnotations(handler);
//        Timer.Sample timerSample = timingContext.getTimerSample();
//        if (annotations.isEmpty()) {
//            Builder builder = this.autoTimer.builder(this.metricName);
//            timerSample.stop(getTimer(builder, handler, request, response, exception));
//            return;
//        }
//        for (Timed annotation : annotations) {
//            Builder builder = Timer.builder(annotation, this.metricName);
//
//            // Only Long Task Timed Not register
//            // Avoid java.lang.IllegalArgumentException:
//            // Prometheus requires that all meters with the same name have the same set of tag keys.
//            // There is already an existing meter containing tag keys [method, status, uri]. The meter you are attempting to register has keys [exception, method, status, uri].
//            if(!annotation.longTask()){
//                timerSample.stop(getTimer(builder, handler, request, response, exception));
//            }
//            // Original Code
////            timerSample.stop(getTimer(builder, handler, request, response, exception));
//        }
//    }
//
//    private Object getHandler(HttpServletRequest request) {
//        return request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
//    }
//
//    private Set<Timed> getTimedAnnotations(Object handler) {
//        if (!(handler instanceof HandlerMethod)) {
//            return Collections.emptySet();
//        }
//        return getTimedAnnotations((HandlerMethod) handler);
//    }
//
//    private Set<Timed> getTimedAnnotations(HandlerMethod handler) {
//        Set<Timed> methodAnnotations = findTimedAnnotations(handler.getMethod());
//        if (!methodAnnotations.isEmpty()) {
//            return methodAnnotations;
//        }
//        return findTimedAnnotations(handler.getBeanType());
//    }
//
//    private Set<Timed> findTimedAnnotations(AnnotatedElement element) {
//        MergedAnnotations annotations = MergedAnnotations.from(element);
//        if (!annotations.isPresent(Timed.class)) {
//            return Collections.emptySet();
//        }
//        return annotations.stream(Timed.class).collect(MergedAnnotationCollectors.toAnnotationSet());
//    }
//
//    private Timer getTimer(Builder builder, Object handler, HttpServletRequest request, HttpServletResponse response,
//                           Throwable exception) {
//        return builder.tags(this.tagsProvider.getTags(request, response, handler, exception)).register(this.registry);
//    }
//
//    /**
//     * Context object attached to a request to retain information across the multiple
//     * filter calls that happen with async requests.
//     */
//    private static class TimingContext {
//
//        private static final String ATTRIBUTE = TimingContext.class.getName();
//
//        private final Timer.Sample timerSample;
//
//        TimingContext(Sample timerSample) {
//            this.timerSample = timerSample;
//        }
//
//        Timer.Sample getTimerSample() {
//            return this.timerSample;
//        }
//
//        void attachTo(HttpServletRequest request) {
//            request.setAttribute(ATTRIBUTE, this);
//        }
//
//        static TimingContext get(HttpServletRequest request) {
//            return (TimingContext) request.getAttribute(ATTRIBUTE);
//        }
//
//    }
//
//}
