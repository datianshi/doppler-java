package demo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;


@Component
public class WebSocketWrapper {
    
    Logger logger = LoggerFactory.getLogger(WebSocketWrapper.class);
    
    @Autowired 
    PropConfiguration propConfiguration;
    @Autowired
    WebSocketClient webSocketClient;
    @Autowired
    WebSocketHandler webSocketHandler;
    
    WebSocketSession webSocketSession;

    
    public WebSocketSession openConnection(Function<String, String> getUrl){
        URI uri = UriComponentsBuilder.fromUriString(getUrl.apply(propConfiguration.getUrl())).build().encode().toUri();
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", propConfiguration.getToken());
        headers.add("Origin", "http://localhost");
        
        
        if (logger.isInfoEnabled()) {
            logger.info("Connecting to WebSocket at " + uri.toString());
        }
        
        ListenableFuture<WebSocketSession> future =
                webSocketClient.doHandshake(this.webSocketHandler, headers, uri);

        future.addCallback(new ListenableFutureCallback<WebSocketSession>() {
            @Override
            public void onSuccess(WebSocketSession result) {
                webSocketSession = result;
                logger.info("Successfully connected");
            }
            @Override
            public void onFailure(Throwable ex) {
                logger.error("Failed to connect", ex);
            }
        });
        while(webSocketSession==null){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return webSocketSession;
        
    }
    
    public void stopConnection() throws IOException {
        System.out.println("This stopped");
        webSocketSession.close();
    }
    
//
//
//    public WebSocketConnectionManager getWebSocketManager(Function<String, String> getUrl){
//        String url = getUrl.apply(propConfiguration.getUrl());
//        System.out.println(url);
//        WebSocketConnectionManager cm = new WebSocketConnectionManager(webSocketClient, webSocketHandler, url);
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", propConfiguration.getToken());
//        headers.add("Origin", "http://localhost");
//        cm.setHeaders(headers);
//        cm.setAutoStartup(false);
//
//        return cm;
//
//    }
}
