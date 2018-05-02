package hu.elte.agent;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class AgentMain {

    public static final String CIA_FOLDER = Paths.get(".").toAbsolutePath().normalize().toString() + "/cia";
    public static final String KGB_FOLDER = Paths.get(".").toAbsolutePath().normalize().toString() + "/kgb";

    public static final int PORT_LOWER = 20000;
    public static final int PORT_UPPER = 20004;

    public static int TIMEOUT_LOWER;
    public static int TIMEOUT_UPPER;

    public static void main(String[] args) throws IOException {
        // Start thread for each of the agents
        int ciaSize = Integer.parseInt(args[0]);
        int kgbSize = Integer.parseInt(args[1]);

        // Time in milliseconds
        TIMEOUT_LOWER = Integer.parseInt(args[2]);
        TIMEOUT_UPPER = Integer.parseInt(args[3]);

        List<Agent> cia = AgentUtil.createAgetsFromFolder(CIA_FOLDER);
        List<Agent> kgb = AgentUtil.createAgetsFromFolder(KGB_FOLDER);

        cia.forEach(Agent::start);
        kgb.forEach(Agent::start);
    }

}
