package nz.ac.wgtn.swen301.restappender.server;



import org.apache.logging.log4j.core.LogEvent;

import java.util.ArrayList;
import java.util.List;

public class Persistency {
    public static List<LogEvent> DB = new ArrayList<>();
}
