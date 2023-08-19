package immersive_melodies.util;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class MidiConverter {
    private static final String URL = "https://api.conczin.net/v1/convert/";

    public static class Response {
        int responseCode;
        @Nullable
        String error;
        byte[] body;

        public Response(int responseCode, @Nullable String error, byte[] body) {
            this.responseCode = responseCode;
            this.error = error;
            this.body = body;
        }

        public Response(int responseCode, @Nullable String error) {
            this(responseCode, error, new byte[0]);
        }

        public int getResponseCode() {
            return responseCode;
        }

        public @Nullable String getError() {
            return error;
        }

        public byte[] getBody() {
            return body;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "responseCode=" + responseCode +
                    ", error='" + error + '\'' +
                    ", body length=" + body.length +
                    '}';
        }
    }

    public static Response request(byte[] body) {
        return request(body, "midi", Map.of("from_format", "abc"));
    }

    public static Response request(byte[] body, String toFormat, Map<String, String> queryParams) {
        try {
            String fullUrl = URL + toFormat;

            // Append query params
            if (queryParams != null) {
                fullUrl = queryParams.keySet().stream()
                        .map(key -> key + "=" + URLEncoder.encode(queryParams.get(key), StandardCharsets.UTF_8))
                        .collect(Collectors.joining("&", fullUrl + "?", ""));
            }

            HttpURLConnection con = (HttpURLConnection) (new URL(fullUrl)).openConnection();

            // Set request method
            con.setDoOutput(true);
            con.setRequestMethod("POST");

            // Set request headers
            con.setRequestProperty("Content-Type", "application/octet-stream");
            con.setRequestProperty("Accept", "application/octet-stream");

            // Send request body
            OutputStream outputStream = con.getOutputStream();
            outputStream.write(body);
            outputStream.flush();

            // Read response
            int responseCode = con.getResponseCode();
            String error = null;
            if (con.getErrorStream() != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                error = response.toString();
            }

            // Parse answer
            byte[] bytes = con.getInputStream().readAllBytes();

            return new Response(responseCode, error, bytes);
        } catch (Exception e) {
            return new Response(400, e.toString());
        }
    }
}
