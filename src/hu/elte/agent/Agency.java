package hu.elte.agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Agency {

    private List<Agent> agents;

    private Agency(List<Agent> agents) {
        this.agents = agents;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

    public static Agency createAgencyFromFiles(String folderPath) throws IOException {
        List<Agent> agents = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            paths.filter(Files::isRegularFile).forEach(p -> {
                try {
                    agents.add(createAgentFromFile(p));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }

        return new Agency(agents);
    }

    private static Agent createAgentFromFile(Path filePath) throws FileNotFoundException {
        File file = filePath.toFile();

        String fileName = file.getName();
        Pattern pattern = Pattern.compile("([0-9]+)-([0-9]+)");
        Matcher matcher = pattern.matcher(fileName);
        matcher.find(); // result is ignored because file name is always in the correct form

        int serialNumber = Integer.parseInt(matcher.group(2));

        Scanner sc = new Scanner(file);
        List<String> names = Arrays.asList(sc.nextLine().split(" "));
        String msg = sc.nextLine();

        return new Agent(serialNumber, names, msg);
    }

    // TODO: executor service?
    public void startAll() {
        agents.forEach(Agent::start);
    }

}
