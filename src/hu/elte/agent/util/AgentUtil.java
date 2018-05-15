package hu.elte.agent.util;

import hu.elte.agent.Agency;
import hu.elte.agent.Agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class AgentUtil {

    public static Agency createAgencyFromFolder(String folderPath, int size) throws IOException {
        List<Agent> agents = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            paths.limit(size + 1).filter(Files::isRegularFile).forEach(p -> {
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

        Faction faction = matcher.group(1).equals("1") ? Faction.CIA : Faction.KGB;
        int serialNumber = Integer.parseInt(matcher.group(2));

        Scanner sc = new Scanner(file);
        List<String> names = Arrays.asList(sc.nextLine().split(" "));
        List<String> secrets = new ArrayList<>();
        secrets.add(sc.nextLine());

        return new Agent(faction, serialNumber, names, secrets);
    }

    public static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
        return new HashSet<>(list1).equals(new HashSet<>(list2));
    }
}
