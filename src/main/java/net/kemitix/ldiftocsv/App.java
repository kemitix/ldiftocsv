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
 * <p>
 * Known Limitation: does not handle '~' expansion
 *
 * @author pcampbell
 */
@SuppressWarnings("hideutilityclassconstructor")
public class App {

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
        // load file(s)
        final List<String> lines = new ArrayList<>();
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
        // TODO : merge data lines (have leading spaces) with the previous line
        // parse lines into key/value pairs grouped into entries
        final List<Map<String, String>> entries = new ArrayList<>();
        final Map<String, String> entry = new HashMap<>();
        lines.stream()
             .filter(line -> !line.matches("^#.*"))
             .filter(line -> line.matches("^.*?: .*"))
             .forEach(line -> {
                 final String[] split = line.split("\\: ", 2);
                 final String key = split[0];
                 final String value = split[1];
                 if ("dn".equals(key)) {
                     if (entry.size() > 0) {
                         entries.add(new HashMap<>(entry));
                         entry.clear();
                     }
                     entry.put("dn", value);
                 } else if (entry.containsKey("dn")) {
                     entry.put(key, value);
                 }
             });
        entries.add(new HashMap<>(entry));
        // find all the keys used
        Set<String> keys = new HashSet<>();
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

}
