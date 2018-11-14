package com.freebies.app;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.server.reactive.ContextPathCompositeHandler;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;

@SpringBootApplication
@RestController
public class FreebiesApplication extends NettyReactiveWebServerFactory{

	public static void main(String[] args) {
		SpringApplication.run(FreebiesApplication.class, args);
	}
	
	@Value("${server.servlet.context-path}")
	private String contextPath;
		
	@Override
	public WebServer getWebServer(HttpHandler httpHandler) {
	   Map<String, HttpHandler> handlerMap = new HashMap<>();
	   handlerMap.put(contextPath, httpHandler);
	   return super.getWebServer(new ContextPathCompositeHandler(handlerMap));
	}
	
	@SuppressWarnings("rawtypes")
	final FluxProcessor processor;
    final FluxSink<String> sink;
    final AtomicLong counter;
    
    @SuppressWarnings("unchecked")
    public FreebiesApplication() {
        this.processor = DirectProcessor.create().serialize();
        this.sink = processor.sink();
        this.counter = new AtomicLong();
    }

    @GetMapping("/send")
    public void test() {
        sink.next("Hello World #" + counter.getAndIncrement());
    }
	
    @GetMapping("/request")
    public void sendRequest(@RequestParam("param1") String param1 ) {
        sink.next("Your input " + param1 );
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value ="events",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent> sse() {
        return processor.map(e -> ServerSentEvent.builder(e).build());
    }
}
