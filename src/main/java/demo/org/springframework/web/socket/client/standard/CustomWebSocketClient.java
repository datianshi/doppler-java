package demo.org.springframework.web.socket.client.standard;


import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.UsesJava7;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureTask;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketHandlerAdapter;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.adapter.standard.WebSocketToStandardExtensionAdapter;
import org.springframework.web.socket.client.AbstractWebSocketClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.websocket.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;


// Help implement skip ssl certificate validation
public class CustomWebSocketClient extends AbstractWebSocketClient{
    
    static final SSLContext SKIP_SSL_CONTEXT;
    
    static {
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
        SKIP_SSL_CONTEXT = sc;
    }
    
    private final boolean sslValidation;

    private final WebSocketContainer webSocketContainer;

    private AsyncListenableTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
    
    public CustomWebSocketClient(boolean sslValidation){
        this.sslValidation = sslValidation;
        this.webSocketContainer = ContainerProvider.getWebSocketContainer();
    }

    /**
     * Set an {@link AsyncListenableTaskExecutor} to use when opening connections.
     * If this property is set to {@code null}, calls to  any of the
     * {@code doHandshake} methods will block until the connection is established.
     * <p>By default, an instance of {@code SimpleAsyncTaskExecutor} is used.
     */
    public void setTaskExecutor(AsyncListenableTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * Return the configured {@link org.springframework.core.task.TaskExecutor}.
     */
    public AsyncListenableTaskExecutor getTaskExecutor() {
        return this.taskExecutor;
    }


    @Override
    protected ListenableFuture<WebSocketSession> doHandshakeInternal(WebSocketHandler webSocketHandler,
                                                                     HttpHeaders headers, final URI uri, List<String> protocols,
                                                                     List<WebSocketExtension> extensions, Map<String, Object> attributes) {

        int port = getPort(uri);
        InetSocketAddress localAddress = new InetSocketAddress(getLocalHost(), port);
        InetSocketAddress remoteAddress = new InetSocketAddress(uri.getHost(), port);

        final StandardWebSocketSession session = new StandardWebSocketSession(headers,
                attributes, localAddress, remoteAddress);

        final ClientEndpointConfig.Builder configBuilder = ClientEndpointConfig.Builder.create();
        configBuilder.configurator(new StandardWebSocketClientConfigurator(headers));
        configBuilder.preferredSubprotocols(protocols);
        configBuilder.extensions(adaptExtensions(extensions));
        final Endpoint endpoint = new StandardWebSocketHandlerAdapter(webSocketHandler, session);

        Callable<WebSocketSession> connectTask = new Callable<WebSocketSession>() {
            @Override
            public WebSocketSession call() throws Exception {
                ClientEndpointConfig config = configBuilder.build();
                config.getUserProperties().put("org.apache.tomcat.websocket.SSL_CONTEXT", SKIP_SSL_CONTEXT);
                webSocketContainer.connectToServer(endpoint, config, uri);
                return session;
            }
        };

        if (this.taskExecutor != null) {
            return this.taskExecutor.submitListenable(connectTask);
        }
        else {
            ListenableFutureTask<WebSocketSession> task = new ListenableFutureTask<WebSocketSession>(connectTask);
            task.run();
            return task;
        }
    }

    private static List<Extension> adaptExtensions(List<WebSocketExtension> extensions) {
        List<Extension> result = new ArrayList<Extension>();
        for (WebSocketExtension extension : extensions) {
            result.add(new WebSocketToStandardExtensionAdapter(extension));
        }
        return result;
    }

    @UsesJava7  // fallback to InetAddress.getLoopbackAddress()
    private InetAddress getLocalHost() {
        try {
            return InetAddress.getLocalHost();
        }
        catch (UnknownHostException ex) {
            return InetAddress.getLoopbackAddress();
        }
    }

    private int getPort(URI uri) {
        if (uri.getPort() == -1) {
            String scheme = uri.getScheme().toLowerCase(Locale.ENGLISH);
            return ("wss".equals(scheme) ? 443 : 80);
        }
        return uri.getPort();
    }


    private class StandardWebSocketClientConfigurator extends ClientEndpointConfig.Configurator {

        private final HttpHeaders headers;

        public StandardWebSocketClientConfigurator(HttpHeaders headers) {
            this.headers = headers;
        }

        @Override
        public void beforeRequest(Map<String, List<String>> requestHeaders) {
            requestHeaders.putAll(this.headers);
            if (logger.isTraceEnabled()) {
                logger.trace("Handshake request headers: " + requestHeaders);
            }
        }
        @Override
        public void afterResponse(HandshakeResponse response) {
            if (logger.isTraceEnabled()) {
                logger.trace("Handshake response headers: " + response.getHeaders());
            }
        }
    }

}
