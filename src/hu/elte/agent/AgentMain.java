package hu.elte.agent;

import hu.elte.agent.util.AgentUtil;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AgentMain {

    public static final String CIA_FOLDER = Paths.get(".").toAbsolutePath().normalize().toString() + "/cia";
    public static final String KGB_FOLDER = Paths.get(".").toAbsolutePath().normalize().toString() + "/kgb";

    // localhost:20000-20100
    public static final String HOST = "localhost";
    public static final int PORT_LOWER = 20000;
    public static final int PORT_UPPER = 20010;

    public static int TIMEOUT_LOWER;
    public static int TIMEOUT_UPPER;

    public static Agency CIA;
    public static Agency KGB;

    public static void main(String[] args) throws IOException, InterruptedException {
        int ciaSize = Integer.parseInt(args[0]);
        int kgbSize = Integer.parseInt(args[1]);

        // Time in milliseconds
        TIMEOUT_LOWER = Integer.parseInt(args[2]);
        TIMEOUT_UPPER = Integer.parseInt(args[3]);

        CIA = AgentUtil.createAgencyFromFolder(CIA_FOLDER);
        KGB = AgentUtil.createAgencyFromFolder(KGB_FOLDER);

        List<Agent> allAgents = new ArrayList<>(CIA.getAgentList());
        allAgents.addAll(KGB.getAgentList());

        allAgents.forEach(Agent::start);
        for (Agent agent : allAgents) {
            agent.join();
        }

        TimeUnit.SECONDS.sleep(5);
        if (CIA.isWinner()) {


            System.out.println("The 'Central Intelligence Agency' has won the game!");
        } else if (KGB.isWinner()) {
            System.out.println("The 'Komitet gosudarstvennoy bezopasnosti' has won the game!");
        }
    }

}
