package demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Created by dings on 2/10/15.
 */
public class LogWebSocketHandler extends TextWebSocketHandler {

    protected Log logger = LogFactory.getLog(LogWebSocketHandler.class);


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws Exception {
        this.logger.info("Received: " + message);
        session.close();
    }

}
