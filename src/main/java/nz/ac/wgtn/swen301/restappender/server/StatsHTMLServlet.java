package nz.ac.wgtn.swen301.restappender.server;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;

public class StatsHTMLServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        StringBuilder s = new StringBuilder();
        s.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head><title>Logger Statistics</title></head>")
                .append("<body>")
                .append("<table border='1'>")
                .append("<tr><th>logger</th><th>ALL</th><th>TRACE</th><th>DEBUG</th><th>INFO</th><th>WARN</th><th>ERROR</th><th>FATAL</th><th>OFF</th></tr>");
        LinkedHashMap<String, LinkedHashMap<String, Integer>> DB_TABLE = Persistency.generateStats();
        for(String loggerName : DB_TABLE.keySet()){
            s.append("<tr>")
                    .append("<td>").append(loggerName).append("</td>");
            LinkedHashMap<String, Integer> loggerStats = DB_TABLE.get(loggerName);
            for(String logLevel : loggerStats.keySet()) {
                s.append("<td>").append(loggerStats.get(logLevel)).append("</td>");
            }
            s.append("</tr>");
        }
        s.append("</table>")
                .append("</body>")
                .append("</html>");
        resp.setContentType("text/html");
        resp.getWriter().write(s.toString());
    }
}
