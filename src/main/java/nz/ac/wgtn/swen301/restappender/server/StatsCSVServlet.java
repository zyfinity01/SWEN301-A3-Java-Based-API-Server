package nz.ac.wgtn.swen301.restappender.server;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;

public class StatsCSVServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        StringBuilder s = new StringBuilder();
        s.append("logger\tALL\tTRACE\tDEBUG\tINFO\tWARN\tERROR\tFATAL\tOFF\n");
        LinkedHashMap<String, LinkedHashMap<String, Integer>> DB_TABLE = Persistency.generateStats();
        for(String loggerName : DB_TABLE.keySet()){
            s.append(loggerName).append("\t");
            LinkedHashMap<String, Integer> loggerStats = DB_TABLE.get(loggerName);
            for(String logLevel : loggerStats.keySet()) {
                s.append(loggerStats.get(logLevel)).append("\t");
            }
            s.append("\n");
        }
        // Setting Content-Type to send CSV data
        resp.setContentType("text/csv");
        resp.getWriter().write(s.toString());
    }
}
