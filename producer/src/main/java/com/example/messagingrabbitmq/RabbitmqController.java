package com.example.messagingrabbitmq;

import datadog.trace.api.DDTags;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.rabbitmq.RabbitMqSendTracingHelper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RabbitmqController {


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    Tracer tracer;

    @RequestMapping("/test")
    public String index() throws InterruptedException {
        System.out.println("Sending message from controller...");
        //rabbitTemplate.convertAndSend(MessagingRabbitmqApplication.topicExchangeName, "foo.bar.baz", "Hello from RabbitMQ!");
        ScopeManager sm = tracer.scopeManager();
        Tracer.SpanBuilder tb = tracer.buildSpan("servlet.request");

        Span span = tb.start();
        try(Scope scope = sm.activate(span)) {
            span.setTag(DDTags.SERVICE_NAME, "springrabbitmqprod");
            span.setTag(DDTags.RESOURCE_NAME, "GET /test");
            span.setTag(DDTags.SPAN_TYPE, "web");
            publishToRabbitMQ();
            Thread.sleep(20);
            span.finish();
        }

        return "\ntest";
    }

    public void publishToRabbitMQ(){
        rabbitTemplate.convertAndSend(MessagingRabbitmqApplication.topicExchangeName, "foo.bar.baz", "Hello from RabbitMQ!");
    }


}
