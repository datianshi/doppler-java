package demo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;

import java.util.function.Function;


@Component
public class WebSocketFactory {
    
    @Autowired 
    PropConfiguration propConfiguration;
    @Autowired
    WebSocketClient webSocketClient;
    @Autowired
    WebSocketHandler webSocketHandler;
    
    @Value("${endpoint}")
    private String endpoint;
    
    public void test(){
        System.out.println("endpoint: " + endpoint);
    }

    
    public WebSocketConnectionManager getWebSocketManager(Function<String, String> getUrl){
        String url = getUrl.apply(propConfiguration.getUrl());
        System.out.println(url);
        WebSocketConnectionManager cm = new WebSocketConnectionManager(webSocketClient, webSocketHandler, url);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", propConfiguration.getToken());
        headers.add("Origin", "http://localhost");
        cm.setHeaders(headers);
        cm.setAutoStartup(false);

        return cm;
        
    }
}
