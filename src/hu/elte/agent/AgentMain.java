package hu.elte.agent;

import hu.elte.agent.util.AgentUtil;
import hu.elte.agent.util.NoAgentFilesException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class AgentMain {

    // localhost:20000-20100
    public static final String HOST = "localhost";
    public static final int PORT_LOWER = 20000;
    public static final int PORT_UPPER = 20100;

    public static int TIMEOUT_LOWER = 50;
    public static int TIMEOUT_UPPER = 100;

    public static final String CIA_FOLDER = Paths.get(".").toAbsolutePath().normalize().toString() + "/resources/cia";
    public static final String KGB_FOLDER = Paths.get(".").toAbsolutePath().normalize().toString() + "/resources/kgb";

    public static Agency CIA;
    public static Agency KGB;

    public static int CIA_SIZE = 5;
    public static int KGB_SIZE = 5;

    public static int MAX_AGENCY_SIZE;

    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            setupParams(args);
        } catch (IOException | NoAgentFilesException e) {
            System.out.println("Please create agent files before starting the game.");
            return;
        }

        CIA = AgentUtil.createAgencyFromFolder(CIA_FOLDER, CIA_SIZE);
        KGB = AgentUtil.createAgencyFromFolder(KGB_FOLDER, KGB_SIZE);

        List<Agent> allAgents = new ArrayList<>(CIA.getAgentList());
        allAgents.addAll(KGB.getAgentList());

        long startTime = System.currentTimeMillis();

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

        long endTime = System.currentTimeMillis();
        System.out.println("The game took " + (endTime - startTime)/1000 + " seconds to finish!");
    }

    private static void setupParams(String[] args) throws IOException, NoAgentFilesException {
        if (args.length == 4) {
            CIA_SIZE = Integer.parseInt(args[0]);
            KGB_SIZE = Integer.parseInt(args[1]);
            TIMEOUT_LOWER = Integer.parseInt(args[2]);
            TIMEOUT_UPPER = Integer.parseInt(args[3]);
        }

        long numOfCiaFiles;
        try (Stream<Path> files = Files.list(Paths.get(CIA_FOLDER))) {
            numOfCiaFiles = files.count();
        }

        long numOfKgbFiles;
        try (Stream<Path> files = Files.list(Paths.get(KGB_FOLDER))) {
            numOfKgbFiles = files.count();
        }

        if (numOfCiaFiles == 0 || numOfKgbFiles == 0) {
            throw new NoAgentFilesException();
        }

        MAX_AGENCY_SIZE = Math.toIntExact(numOfCiaFiles > numOfKgbFiles ? numOfCiaFiles : numOfKgbFiles);
    }


}
