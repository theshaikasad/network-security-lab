import com.sun.net.httpserver.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.json.JSONObject;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import javax.crypto.SecretKey;

public class JWTServer {
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 3600000; // 1 hour
    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/", new StaticHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/protected", new ProtectedHandler());
        server.createContext("/api/verify", new VerifyHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("JWT Server started on port 8080");
        System.out.println("Access: http://localhost:8080");
    }
    
    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>JWT Authentication Demo</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial; max-width: 800px; margin: 50px auto; padding: 20px; }\n" +
                "        .container { border: 1px solid #ddd; padding: 20px; margin: 20px 0; border-radius: 5px; }\n" +
                "        input, button { padding: 10px; margin: 5px; }\n" +
                "        button { background: #007bff; color: white; border: none; cursor: pointer; }\n" +
                "        button:hover { background: #0056b3; }\n" +
                "        .response { background: #f8f9fa; padding: 15px; margin: 10px 0; border-radius: 5px; }\n" +
                "        .error { color: red; }\n" +
                "        .success { color: green; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>JWT Authentication Demo</h1>\n" +
                "    \n" +
                "    <div class='container'>\n" +
                "        <h2>1. Login</h2>\n" +
                "        <input type='text' id='username' placeholder='Username' value='admin'>\n" +
                "        <input type='password' id='password' placeholder='Password' value='password'>\n" +
                "        <button onclick='login()'>Login</button>\n" +
                "        <div id='loginResponse' class='response'></div>\n" +
                "    </div>\n" +
                "    \n" +
                "    <div class='container'>\n" +
                "        <h2>2. Access Protected Resource</h2>\n" +
                "        <button onclick='accessProtected()'>Access Protected Data</button>\n" +
                "        <div id='protectedResponse' class='response'></div>\n" +
                "    </div>\n" +
                "    \n" +
                "    <div class='container'>\n" +
                "        <h2>3. Verify Token</h2>\n" +
                "        <button onclick='verifyToken()'>Verify Current Token</button>\n" +
                "        <div id='verifyResponse' class='response'></div>\n" +
                "    </div>\n" +
                "    \n" +
                "    <script>\n" +
                "        let jwtToken = '';\n" +
                "        \n" +
                "        async function login() {\n" +
                "            const username = document.getElementById('username').value;\n" +
                "            const password = document.getElementById('password').value;\n" +
                "            \n" +
                "            const response = await fetch('/api/login', {\n" +
                "                method: 'POST',\n" +
                "                headers: {'Content-Type': 'application/json'},\n" +
                "                body: JSON.stringify({username, password})\n" +
                "            });\n" +
                "            \n" +
                "            const data = await response.json();\n" +
                "            const loginDiv = document.getElementById('loginResponse');\n" +
                "            \n" +
                "            if (response.ok) {\n" +
                "                jwtToken = data.token;\n" +
                "                loginDiv.className = 'response success';\n" +
                "                loginDiv.innerHTML = '<strong>Login Successful!</strong><br>' +\n" +
                "                    'Token: ' + jwtToken.substring(0, 50) + '...<br>' +\n" +
                "                    'Expires: ' + new Date(data.expiresAt).toLocaleString();\n" +
                "            } else {\n" +
                "                loginDiv.className = 'response error';\n" +
                "                loginDiv.innerHTML = 'Error: ' + data.message;\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        async function accessProtected() {\n" +
                "            const protectedDiv = document.getElementById('protectedResponse');\n" +
                "            \n" +
                "            if (!jwtToken) {\n" +
                "                protectedDiv.className = 'response error';\n" +
                "                protectedDiv.innerHTML = 'Please login first!';\n" +
                "                return;\n" +
                "            }\n" +
                "            \n" +
                "            const response = await fetch('/api/protected', {\n" +
                "                headers: {'Authorization': 'Bearer ' + jwtToken}\n" +
                "            });\n" +
                "            \n" +
                "            const data = await response.json();\n" +
                "            \n" +
                "            if (response.ok) {\n" +
                "                protectedDiv.className = 'response success';\n" +
                "                protectedDiv.innerHTML = '<strong>Access Granted!</strong><br>' +\n" +
                "                    'Message: ' + data.message + '<br>' +\n" +
                "                    'User: ' + data.user + '<br>' +\n" +
                "                    'Timestamp: ' + new Date(data.timestamp).toLocaleString();\n" +
                "            } else {\n" +
                "                protectedDiv.className = 'response error';\n" +
                "                protectedDiv.innerHTML = 'Error: ' + data.message;\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        async function verifyToken() {\n" +
                "            const verifyDiv = document.getElementById('verifyResponse');\n" +
                "            \n" +
                "            if (!jwtToken) {\n" +
                "                verifyDiv.className = 'response error';\n" +
                "                verifyDiv.innerHTML = 'No token to verify. Please login first!';\n" +
                "                return;\n" +
                "            }\n" +
                "            \n" +
                "            const response = await fetch('/api/verify', {\n" +
                "                headers: {'Authorization': 'Bearer ' + jwtToken}\n" +
                "            });\n" +
                "            \n" +
                "            const data = await response.json();\n" +
                "            \n" +
                "            if (response.ok) {\n" +
                "                verifyDiv.className = 'response success';\n" +
                "                verifyDiv.innerHTML = '<strong>Token Valid!</strong><br>' +\n" +
                "                    'Subject: ' + data.subject + '<br>' +\n" +
                "                    'Issued At: ' + new Date(data.issuedAt * 1000).toLocaleString() + '<br>' +\n" +
                "                    'Expires At: ' + new Date(data.expiration * 1000).toLocaleString();\n" +
                "            } else {\n" +
                "                verifyDiv.className = 'response error';\n" +
                "                verifyDiv.innerHTML = 'Error: ' + data.message;\n" +
                "            }\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
            
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, html.length());
            OutputStream os = exchange.getResponseBody();
            os.write(html.getBytes());
            os.close();
        }
    }
    
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
                return;
            }
            
            String body = new String(exchange.getRequestBody().readAllBytes());
            JSONObject json = new JSONObject(body);
            
            String username = json.getString("username");
            String password = json.getString("password");
            
            // Simple authentication
            if ("admin".equals(username) && "password".equals(password)) {
                String token = generateToken(username);
                long expiresAt = System.currentTimeMillis() + EXPIRATION_TIME;
                
                JSONObject response = new JSONObject();
                response.put("token", token);
                response.put("expiresAt", expiresAt);
                response.put("message", "Login successful");
                
                sendResponse(exchange, 200, response.toString());
            } else {
                sendResponse(exchange, 401, "{\"message\":\"Invalid credentials\"}");
            }
        }
    }
    
    static class ProtectedHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendResponse(exchange, 401, "{\"message\":\"No token provided\"}");
                return;
            }
            
            String token = authHeader.substring(7);
            
            try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                
                JSONObject response = new JSONObject();
                response.put("message", "Access granted to protected resource");
                response.put("user", claims.getSubject());
                response.put("timestamp", System.currentTimeMillis());
                
                sendResponse(exchange, 200, response.toString());
                
            } catch (JwtException e) {
                sendResponse(exchange, 401, "{\"message\":\"Invalid or expired token\"}");
            }
        }
    }
    
    static class VerifyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendResponse(exchange, 401, "{\"message\":\"No token provided\"}");
                return;
            }
            
            String token = authHeader.substring(7);
            
            try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                
                JSONObject response = new JSONObject();
                response.put("valid", true);
                response.put("subject", claims.getSubject());
                response.put("issuedAt", claims.getIssuedAt().getTime() / 1000);
                response.put("expiration", claims.getExpiration().getTime() / 1000);
                
                sendResponse(exchange, 200, response.toString());
                
            } catch (ExpiredJwtException e) {
                sendResponse(exchange, 401, "{\"message\":\"Token has expired\"}");
            } catch (JwtException e) {
                sendResponse(exchange, 401, "{\"message\":\"Invalid token\"}");
            }
        }
    }
    
    private static String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SECRET_KEY)
            .compact();
    }
    
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}