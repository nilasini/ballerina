import ballerina.io;
import ballerina.net.http;
import ballerina.net.ws;

@http:configuration {
    basePath:"/test",
    webSocket:@http:webSocket {
                  upgradePath:"/ws",
                  serviceName:"wsServic"
              }
}
service<http> httpService {

    @http:resourceConfig {
        path:"/world",
        methods:["POST","GET","PUT","My"]
    }
    resource testResource(http:Connection conn, http:Request req) {
        http:Response resp = {};
        var payload, _ = req.getStringPayload();
        io:println(payload);
        resp.setStringPayload("I received");
        _ = conn.respond(resp);
    }
}

@ws:configuration {
    subProtocols:["xml, json"],
    idleTimeoutInSeconds:5
}
service<ws> wsService  {

    resource onOpen(ws:Connection conn) {
        io:println("New WebSocket connection: " + conn.getID());
    }

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        io:println(frame.text);
        conn.pushText(frame.text);
    }

    resource onIdleTimeout(ws:Connection conn) {
        io:println("Idle timeout: " + conn.getID());
    }
}