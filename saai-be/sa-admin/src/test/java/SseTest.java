import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class SseTest {
    public static void main(String[] args) throws Exception {
        String token = login();
        System.out.println("Token: " + token);
        System.out.println("=== SSE Stream ===");

        URL url = new URL("http://localhost:11024/novel/write/start/stream");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        conn.setDoOutput(true);
        conn.setReadTimeout(300000);

        String body = "{\"projectId\":2,\"chapterNo\":8,\"pov\":\"林澈\",\"chapterGoal\":\"播放父亲留下的磁带，听到关键线索\"}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            int tokenCount = 0;
            while ((line = reader.readLine()) != null && tokenCount < 30) {
                if (line.startsWith("data:")) {
                    String data = line.substring(5);
                    System.out.println("[" + (++tokenCount) + "] " +
                            (data.length() < 80 ? data : data.substring(0, 80) + "..."));
                } else if (line.startsWith("event:")) {
                    System.out.println("EVENT: " + line.substring(6));
                }
            }
        }
    }

    static String login() throws Exception {
        URL capUrl = new URL("http://localhost:11024/login/getCaptcha");
        HttpURLConnection capConn = (HttpURLConnection) capUrl.openConnection();
        String capResp = new String(capConn.getInputStream().readAllBytes());
        String capUuid = extractJson(capResp, "captchaUuid");
        String capCode = extractJson(capResp, "captchaText");

        URL loginUrl = new URL("http://localhost:11024/login");
        HttpURLConnection loginConn = (HttpURLConnection) loginUrl.openConnection();
        loginConn.setRequestMethod("POST");
        loginConn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        loginConn.setDoOutput(true);
        String loginBody = "{\"loginName\":\"admin\",\"password\":\"YTdhMzBmM2RiOTE3YmNmZDczNmNiNjJhMzRkN2U1ZWM=\",\"loginDevice\":1,\"captchaCode\":\"" + capCode + "\",\"captchaUuid\":\"" + capUuid + "\"}";
        try (OutputStream os = loginConn.getOutputStream()) {
            os.write(loginBody.getBytes(StandardCharsets.UTF_8));
        }
        String loginResp = new String(loginConn.getInputStream().readAllBytes());
        return extractJson(loginResp, "token");
    }

    static String extractJson(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return "";
        idx = json.indexOf("\"", idx + key.length() + 3);
        int end = json.indexOf("\"", idx + 1);
        return json.substring(idx + 1, end);
    }
}
