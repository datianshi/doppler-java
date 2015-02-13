package demo;

import com.googlecode.protobuf.format.JsonFormat;
import events.EnvelopeOuterClass;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;


public class LogWebSocketHandler extends BinaryWebSocketHandler {

    

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        EnvelopeOuterClass.Envelope envelope = EnvelopeOuterClass.Envelope.parseFrom(message.getPayload().array());
        System.out.println(JsonFormat.printToString(envelope.getLogMessage()));
    }
}
