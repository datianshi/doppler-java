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
import org.springframework.core.env.Environment;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;

import javax.validation.Valid;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {
    protected Log logger = LogFactory.getLog(DemoApplication.class);

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DemoApplication.class);
        application.setAddCommandLineProperties(true);
        application.run(args);
    }

    @Autowired
    WebSocketFactory factory;

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


    public void run(String... args) throws Exception {
        WebSocketConnectionManager manager = null;

        if (endpoint.equals("firehose")) {
            manager = factory.getWebSocketManager((String url) -> url + "/firehose/firehose-a");
            manager.start();
        } else if (endpoint.equals("app")) {
            manager = factory.getWebSocketManager((String url) -> url + "/apps/" + appEndpoint.getGuid() + "/" + appEndpoint.getType());
        }
        manager.start();
    }

    private void usage() {
        System.out.println("[Usage]: \njava demo.DemoApplication firehose\njava demo.DemoApplication app [instanceId]");
        System.exit(1);
    }

}
