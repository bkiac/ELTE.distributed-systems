package hu.elte.agent;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE;

public class CreateAgentFiles {

    private static final String[] SPIED_FOR_AMERICA = {
            "Aleksandr Dmitrievich Ogorodnik",
            "Francis Gary Powers",
            "Otto von Bolschwing",
            "Arkady Shevchenko",
            "Boris Yuzhin",
            "Gerry Droller",
            "Heinz Barwich",
            "John Birch",
            "Milton Bearden",
            "Nicholas Shadrin"
    };
    private static final String[] SPIED_FOR_USSR = {
            "Alan Nunn May",
            "Allan Robert Rosenberg",
            "Donald Niven Wheeler",
            "Harry Dexter White",
            "Jane Foster Zlatovski",
            "Nathan Gregory Silvermaster",
            "Vilyam Genrikhovich Fisher",
            "Agnes Smedley",
            "Al Sarant",
            "Aldrich Ames"
    };

    private static final String[] CRYPTONYMS = {
            "APPLE",
            "ARTICHOKE",
            "AZORIAN",
            "BIRCH",
            "BLACKSHIELD",
            "BLUEBIRD",
            "BOND",
            "CATIDE",
            "CHARITY",
            "CHERRY",

            "MOCKINGBIRD",
            "MONGOOSE",
            "OAK",
            "PANCHO",
            "PAPERCLIP",
            "PHOENIX",
            "PINE",
            "RAINBOW",
            "QKWAVER",
            "RUFUS"
    };

    public static void main(String[] args) throws IOException {
        Path ciaPath = Paths.get("./resources/cia");
        Path kgbPath = Paths.get("./resources/kgb");
        if (!Files.exists(ciaPath)) {
            Files.createDirectories(ciaPath);
        }
        if (!Files.exists(kgbPath)) {
            Files.createDirectories(kgbPath);
        }

        int cryptonymCounter = 0;

        for (int i = 0; i < SPIED_FOR_AMERICA.length; i++) {
            List<String> lines = Arrays.asList(SPIED_FOR_AMERICA[i], CRYPTONYMS[cryptonymCounter]);

            writeToFile("cia/agent1-" + (i + 1) + ".txt", lines);

            cryptonymCounter++;
        }
        for (int i = 0; i < SPIED_FOR_USSR.length; i++) {
            List<String> lines = Arrays.asList(SPIED_FOR_USSR[i], CRYPTONYMS[cryptonymCounter]);

            writeToFile("kgb/agent2-" + (i + 1) + ".txt", lines);

            cryptonymCounter++;
        }
    }

    private static void writeToFile(String fileName, List<String> lines) throws IOException {
        Path file = Paths.get("./resources/" + fileName);
        Files.write(file, lines, Charset.forName("UTF-8"), CREATE);
    }
}
