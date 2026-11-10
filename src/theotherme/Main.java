package theotherme;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.Executors;

public final class Main {
    private static final String HTML = """
<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>The Other Me</title>
<style>body{margin:0;background:#111827;color:#f3f4f6;font-family:system-ui,Arial}.app{max-width:760px;margin:32px auto;padding:22px;background:#1f2937;border-radius:18px}h1{margin:0 0 8px}.sub{color:#9ca3af}#chat{height:430px;overflow:auto;background:#111827;border-radius:12px;padding:16px;margin:18px 0}.msg{margin:12px 0;padding:12px;border-radius:10px;white-space:pre-wrap}.you{background:#374151;margin-left:12%}.bot{background:#263c45;margin-right:12%}form{display:flex;gap:8px}input{flex:1;background:#111827;color:white;border:1px solid #4b5563;border-radius:8px;padding:12px}button,select{background:#0f766e;color:white;border:0;border-radius:8px;padding:12px;cursor:pointer}.toolbar{display:flex;align-items:center;gap:12px;margin-top:15px}label{color:#d1d5db}</style></head><body><main class="app"><h1>The Other Me</h1><div class="sub">Your local companion — no cloud AI or accounts</div><div class="toolbar"><label for="persona">Personality</label><select id="persona"><option value="donna">Donna</option><option value="alter">Alter Ego</option></select></div><div id="chat" aria-live="polite"></div><form id="form"><input id="message" placeholder="What's on your mind?" autocomplete="off" required><button>Send</button></form></main><script>
const chat=document.getElementById('chat');const input=document.getElementById('message');const persona=document.getElementById('persona');
function add(who,text){const div=document.createElement('div');div.className='msg '+(who==='You'?'you':'bot');div.textContent=who+': '+text;chat.append(div);chat.scrollTop=chat.scrollHeight;}
add('Donna','Hello. Pick a thought, a problem, or a decision. We can work with it.');
document.getElementById('form').addEventListener('submit',async e=>{e.preventDefault();const message=input.value.trim();if(!message)return;add('You',message);input.value='';try{const r=await fetch('/api/chat',{method:'POST',headers:{'Content-Type':'text/plain;charset=utf-8','X-Persona':persona.value},body:message});add(persona.value==='donna'?'Donna':'Alter Ego',await r.text());}catch(err){add('System','Local server is unavailable.');}});
</script></body></html>
""";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8085), 0);
        ConversationEngine engine = new ConversationEngine();
        server.createContext("/", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                send(exchange, 405, "Method not allowed", "text/plain");
                return;
            }
            send(exchange, 200, HTML, "text/html; charset=utf-8");
        });
        server.createContext("/api/chat", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                send(exchange, 405, "Method not allowed", "text/plain");
                return;
            }
            byte[] bytes = exchange.getRequestBody().readNBytes(8193);
            if (bytes.length > 8192) {
                send(exchange, 413, "Message too long", "text/plain");
                return;
            }
            String message = new String(bytes, StandardCharsets.UTF_8);
            String persona = exchange.getRequestHeaders().getFirst("X-Persona");
            send(exchange, 200, engine.reply(message, persona), "text/plain; charset=utf-8");
        });
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("The Other Me is running at http://127.0.0.1:8085");
    }

    private static void send(HttpExchange exchange, int status, String content, String type) throws IOException {
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(status, data.length);
        try (var stream = exchange.getResponseBody()) {
            stream.write(data);
        }
    }
}

final class ConversationEngine {
    String reply(String message, String persona) {
        String m = message.strip().toLowerCase(Locale.ROOT);
        boolean donna = !"alter".equalsIgnoreCase(persona);
        if (m.isEmpty()) return "Tell me something, and we'll begin.";
        if (m.contains("hello") || m.equals("hi") || m.equals("hey"))
            return donna ? "Hi. What's the real thing on your mind?" : "Hello, other me. What are we untangling today?";
        if (m.contains("decision") || m.contains("decide") || m.contains("choose"))
            return donna ? "Let's name the options, the trade-offs, and what matters most." : "What choice would future-you thank you for making?";
        if (m.contains("tired") || m.contains("exhausted"))
            return donna ? "Noted. What's one thing that actually needs your attention?" : "Then we make the next step smaller. No performance required.";
        if (m.contains("work") || m.contains("job"))
            return donna ? "What's the specific problem to solve, and what's within your control?" : "What kind of work would make you feel most like yourself?";
        return donna ? "I hear you. What's the important part of that, and what do you want to do next?"
                     : "Interesting. Tell me what that means to you, beneath the surface.";
    }
}
