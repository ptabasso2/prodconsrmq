package com.example.messagingrabbitmq;

import java.util.concurrent.CountDownLatch;

import datadog.trace.api.DDTags;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Receiver {

	private CountDownLatch latch = new CountDownLatch(1);

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	Tracer tracer;

	//@RabbitListener(queues = "${consumer.rabbitmq.queue}") // Optional. If présent => application.properties
	public void receiveMessage(String message) {
		System.out.println("Received <" + message + ">");
		/*String result = restTemplate.getForObject("https://www.google.fr", String.class);
		System.out.println(result);*/

		Span resultingspan = tracer.activeSpan();

		Tracer.SpanBuilder httpspan = tracer.buildSpan("hey google").asChildOf(resultingspan);
		Span childspan = httpspan.start();

		try(Scope scope = tracer.activateSpan(childspan)){
			childspan.setTag(DDTags.RESOURCE_NAME, "GET /");
			childspan.setTag(DDTags.SPAN_TYPE, "web");
			childspan.setTag(DDTags.SERVICE_NAME, "Google");
			httpRequest();
			System.out.println("Hello google");
			childspan.finish();
		}

		latch.countDown();
	}


	public void httpRequest() {
		String result = restTemplate.getForObject("https://www.google.fr", String.class);
		System.out.println(result);
	}

	public CountDownLatch getLatch() {
		return latch;
	}

}
