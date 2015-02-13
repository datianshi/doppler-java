package demo;

import demo.org.springframework.web.socket.client.standard.CustomWebSocketClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;

import java.io.IOException;
import java.util.function.Function;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {
    protected Log logger = LogFactory.getLog(DemoApplication.class);

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DemoApplication.class);
        application.setAddCommandLineProperties(true);
        application.run(args);
    }

    @Autowired
    WebSocketWrapper websocket;

    @Value("${endpoint}")
    String endpoint;
    
    @Autowired
    AppEndpoint appEndpoint;


    @Bean
    public WebSocketClient webSocketClient(PropConfiguration propConfiguration) {
        return new CustomWebSocketClient(false);
    }

    @Bean
    public WebSocketHandler webSocketHandler() {
        return new LogWebSocketHandler();
    }

    
    public void run(String... args) throws InterruptedException {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                WebSocketSession session = websocket.openConnection(getUrl(endpoint));
                Runtime.getRuntime().addShutdownHook(new Thread(){
                    public void run(){
                        try {
                            websocket.stopConnection();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                        while(session.isOpen()){
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
            }
        });
        t.start();
        
    }
    
    private Function<String, String> getUrl(String endpoint){
        if(endpoint.equals("firehose")){
            return (String url) -> url + "/firehose/firehose-a";
        }
        else if(endpoint.equals("app")){
            return (String url) -> url + "/apps/" + appEndpoint.getGuid() + "/" + appEndpoint.getType();
        }
        return null;
    }
    
    

    private void usage() {
        System.out.println("[Usage]: \njava demo.DemoApplication firehose\njava demo.DemoApplication app [instanceId]");
        System.exit(1);
    }

}
