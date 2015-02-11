package demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableAutoConfiguration
@EnableWebSocket
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientConfiguration.class, args);

    }

    @Configuration
    @EnableAutoConfiguration
    @EnableWebSocket
    static class ClientConfiguration implements CommandLineRunner {



        @Override
        public void run(String... strings) throws Exception {
            while (true){
                Thread.sleep(1000);
            }
        }

        @Bean
        public WebSocketConnectionManager wsConnectionManager() {

            WebSocketConnectionManager cm = new WebSocketConnectionManager(new StandardWebSocketClient(), new LogWebSocketHandler(), "");
            cm.setAutoStartup(true);

            return cm;
        }
    }
}
