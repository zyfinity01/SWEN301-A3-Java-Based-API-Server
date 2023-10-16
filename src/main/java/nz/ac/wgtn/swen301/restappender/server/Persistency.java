package nz.ac.wgtn.swen301.restappender.server;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Persistency {
    public static List<JsonObject> DB = new ArrayList<>();

    public static LinkedHashMap<String, LinkedHashMap<String, Integer>> generateStats(){
        LinkedHashMap<String, LinkedHashMap<String, Integer>> DB_TABLE = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
        for(JsonObject log : Persistency.DB){
            String loggerName = log.get("logger").getAsString();
            String loggerLevel = log.get("level").getAsString().toUpperCase();
            if(!DB_TABLE.containsKey(loggerName)){
                LinkedHashMap<String, Integer> logLevelStats = new LinkedHashMap<String, Integer>();
                String[] logLevels = {"ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF"};
                for (String logLevel : logLevels) {
                    logLevelStats.put(logLevel, 0);
                }
                DB_TABLE.put(loggerName, logLevelStats);
            }
            Integer logsForLevel = DB_TABLE.get(loggerName).get(loggerLevel);
            DB_TABLE.get(loggerName).put(loggerLevel, logsForLevel + 1);
        }
        return DB_TABLE;
    }
}


