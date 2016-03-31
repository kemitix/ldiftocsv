package net.kemitix.ldiftocsv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LDIF to CSV.
 * <p>
 * Read an LDIF file named on the command line and outputs it as a CSV file to
 * STDOUT.
 *
 * @author pcampbell
 */
public final class App {

    private final List<String> lines;

    private final List<Map<String, String>> entries;

    private App() {
        lines = new ArrayList<>();
        entries = new ArrayList<>();
    }

    /**
     * Main method.
     *
     * @param args the files to be converted
     */
    public static void main(final String[] args) {
        // check at least one filename is specified on command line
        if (args.length == 0) {
            System.err.println("LDIF filename is missing!");
            System.exit(-1);
        }
        final App app = new App();
        app.loadFiles(args);
        // TODO : merge data lines (have leading spaces) with the previous line
        app.parseLines();
        app.writeCSV();
    }

    // load file(s)
    private void loadFiles(final String[] args) {
        for (String filename : args) {
            final Path path = Paths.get(filename).toAbsolutePath();
            System.err.println("Reading: " + path);
            try {
                lines.addAll(Files.readAllLines(Paths.get(filename)));
            } catch (IOException e) {
                System.err.println("Error reading file '" + filename + "': " + e
                        .getMessage());
                System.exit(-1);
            }
        }
    }

    // parse lines into key/value pairs grouped into entries
    private void parseLines() {
        final Map<String, String> entry = new HashMap<>();
        lines.stream()
             .filter(line -> !line.matches("^#.*")) // strip comments
             .filter(line -> line.matches("^.*?: .*")) // match data lines
             // TODO : also include extended data lines
             .forEach(line -> {
                 final String[] split = line.split("\\: ", 2);
                 final String key = split[0];
                 final String value = split[1];
                 if ("dn".equals(key)) {
                     if (entry.size() > 0) {
                         entries.add(new HashMap<>(entry));
                         entry.clear();
                     }
                     entry.put("dn", q(value));
                 } else if (entry.containsKey("dn")) {
                     entry.put(key, q(value));
                 }
             });
        entries.add(new HashMap<>(entry));
    }

    private void writeCSV() {
        final Set<String> keys = new HashSet<>();
        // find all the keys used
        entries.stream()
               .flatMap(map -> map.entrySet().stream())
               .map(Map.Entry::getKey)
               .forEach(keys::add);
        // write CSV header line
        System.out.println(
                String.join(",", keys.stream().collect(Collectors.toList())));
        // write each entry as CSV line
        entries.stream()
               .map(e -> keys.stream().map(e::get))
               .map(e -> e.collect(Collectors.toList()))
               .map(e -> String.join(",", e))
               .forEach(System.out::println);
    }

    private String q(final String value) {
        return "\"" + value + "\"";
    }

}
