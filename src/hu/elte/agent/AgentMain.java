package hu.elte.agent;

import java.io.IOException;
import java.nio.file.Paths;

public class AgentMain {

    private static final String CIA_FOLDER = Paths.get(".").toAbsolutePath().normalize().toString() + "/cia";
    private static final String KGB_FOLDER = Paths.get(".").toAbsolutePath().normalize().toString() + "/kgb";

    public static void main(String[] args) throws IOException {
        // Start thread for each of the agents
        int ciaSize = Integer.parseInt(args[0]);
        int kgbSize = Integer.parseInt(args[1]);

        // Time in milliseconds
        int timeoutLowerBound = Integer.parseInt(args[2]);
        int timeoutUpperBound = Integer.parseInt(args[3]);

        Agency cia = Agency.createAgencyFromFiles(CIA_FOLDER);
        Agency kgb = Agency.createAgencyFromFiles(KGB_FOLDER);

        System.out.println("CIA");
        cia.getAgents().forEach(agent -> System.out.println(agent.getNames()));

        System.out.println("KGB");
        kgb.getAgents().forEach(agent -> System.out.println(agent.getNames()));
    }

}
