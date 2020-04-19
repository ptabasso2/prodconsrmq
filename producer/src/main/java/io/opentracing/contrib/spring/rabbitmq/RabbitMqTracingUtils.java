/**
 * Copyright 2017-2020 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.contrib.spring.rabbitmq;

import datadog.trace.api.DDTags;
import io.opentracing.*;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import org.springframework.amqp.core.MessageProperties;

import java.util.Map;
import java.util.Optional;

/**
 * @author Gilles Robert
 * @author Pejman Tabassomi
 */
final class RabbitMqTracingUtils {

  private RabbitMqTracingUtils() {}

  static Optional<Scope> buildReceiveSpan(MessageProperties messageProperties, Tracer tracer) {
    Optional<SpanContext> context = findParent(messageProperties, tracer);
    Tracer.SpanBuilder spanBuilder =
        tracer
            .buildSpan(RabbitMqTracingTags.SPAN_KIND_CONSUMER)
            .ignoreActiveSpan()
            .withTag(Tags.SPAN_KIND.getKey(), RabbitMqTracingTags.SPAN_KIND_CONSUMER);

    context.ifPresent(spanContext -> spanBuilder.addReference(References.FOLLOWS_FROM, spanContext));
    Scope scope = tracer.scopeManager().activate(spanBuilder.start());

    return Optional.of(scope);
  }

  static Scope buildSendSpan(Tracer tracer, MessageProperties messageProperties) {
    /*Tracer.SpanBuilder spanBuilder =
        tracer
            .buildSpan(RabbitMqTracingTags.SPAN_KIND_PRODUCER)
            .ignoreActiveSpan()
            .withTag(Tags.SPAN_KIND.getKey(), RabbitMqTracingTags.SPAN_KIND_PRODUCER);*/

    Tracer.SpanBuilder spanBuilder =
            tracer
                .buildSpan("amqp command")
                .ignoreActiveSpan()
                .withTag(Tags.SPAN_KIND.getKey(), RabbitMqTracingTags.SPAN_KIND_PRODUCER)
                .withTag(DDTags.RESOURCE_NAME, "send")
                .withTag(DDTags.SPAN_TYPE, "web")
                .withTag(DDTags.SERVICE_NAME, "RabbitMQ");


    ScopeManager scopeManager = tracer.scopeManager();
    Optional<SpanContext> existingSpanContext = Optional.ofNullable(scopeManager)
        .map(ScopeManager::activeSpan)
        .map(Span::context);

    existingSpanContext.ifPresent(spanBuilder::asChildOf);

    if (messageProperties.getHeaders() != null) {
      Optional<SpanContext> messageParentContext = findParent(messageProperties, tracer);
      messageParentContext.ifPresent(spanBuilder::asChildOf);
    }
    Span span = spanBuilder.start();
    return scopeManager.activate(span);
  }

  private static Optional<SpanContext> findParent(
      MessageProperties messageProperties, Tracer tracer) {
    final Map<String, Object> headers = messageProperties.getHeaders();
    SpanContext spanContext =
        tracer.extract(
            Format.Builtin.TEXT_MAP, new RabbitMqMessagePropertiesExtractAdapter(headers));

    if (spanContext == null) {
      return Optional.ofNullable(tracer.activeSpan()).map(Span::context);
    } else {
      return Optional.of(spanContext);
    }
  }
}
