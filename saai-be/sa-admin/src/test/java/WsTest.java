import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class WsTest {
    public static void main(String[] args) throws Exception {
        String token = login();
        System.out.println("Token: " + token);

        CompletableFuture<WebSocket> fut = HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create("ws://localhost:11024/ws/novel/write?token=" + token),
                        new Listener());

        WebSocket ws = fut.get(5, TimeUnit.SECONDS);
        System.out.println("Connected");

        String form = "{\"action\":\"start\",\"projectId\":2,\"chapterNo\":10,\"pov\":\"林澈\",\"chapterGoal\":\"测试WebSocket流式生成\"}";
        ws.sendText(form, true);
        System.out.println("Sent: " + form);

        Thread.sleep(180000);
    }

    static class Listener implements WebSocket.Listener {
        int tokenCount = 0;

        @Override
        public void onOpen(WebSocket ws) { System.out.println("WS Opened"); ws.request(1); }

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            String msg = data.toString();
            if (msg.contains("\"token\"")) {
                tokenCount++;
                int idx = msg.indexOf("\"data\":\"");
                int end = msg.indexOf("\"", idx + 8);
                String t = end > idx ? msg.substring(idx + 8, end) : "...";
                if (t.length() > 50) t = t.substring(0, 50) + "...";
                System.out.println("[" + tokenCount + "] " + t);
            } else {
                System.out.println("Event: " + msg.substring(0, Math.min(200, msg.length())));
            }
            ws.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int code, String reason) { System.out.println("Closed: " + code + " " + reason); return null; }

        @Override
        public void onError(WebSocket ws, Throwable t) { t.printStackTrace(); }
    }

    static String login() throws Exception {
        URL capUrl = new URL("http://localhost:11024/login/getCaptcha");
        HttpURLConnection c = (HttpURLConnection) capUrl.openConnection();
        String resp = new String(c.getInputStream().readAllBytes());
        String uuid = extract(resp, "captchaUuid");
        String code = extract(resp, "captchaText");

        URL loginUrl = new URL("http://localhost:11024/login");
        HttpURLConnection l = (HttpURLConnection) loginUrl.openConnection();
        l.setRequestMethod("POST");
        l.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        l.setDoOutput(true);
        String body = "{\"loginName\":\"admin\",\"password\":\"YTdhMzBmM2RiOTE3YmNmZDczNmNiNjJhMzRkN2U1ZWM=\",\"loginDevice\":1,\"captchaCode\":\"" + code + "\",\"captchaUuid\":\"" + uuid + "\"}";
        l.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        return extract(new String(l.getInputStream().readAllBytes()), "token");
    }

    static String extract(String json, String key) {
        int i = json.indexOf("\"" + key + "\"");
        if (i < 0) return "";
        i = json.indexOf("\"", i + key.length() + 3);
        int e = json.indexOf("\"", i + 1);
        return json.substring(i + 1, e);
    }
}
