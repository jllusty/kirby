package nullusty.kirby;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Knows Geography
// Do we really need more than one of these? Could be a singleton
public class GeographyNerd {
    private final Map<String,String> stateNameToAbbreviationMap = new HashMap<>();
    private final Map<String,String> abbreviationToStateNameMap = new HashMap<>();

    public GeographyNerd(String filepath) throws FileNotFoundException {
        try (BufferedReader reader = new BufferedReader(new FileReader(
                filepath))) {

            String line = reader.readLine();
            while (line != null) {
                String[] fields = line.split(",");
                String stateName = fields[0];
                String abbrev = fields[1];
                stateNameToAbbreviationMap.put(stateName, abbrev);
                abbreviationToStateNameMap.put(abbrev, stateName);

                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getStateNameFromAbbreviation(String stateName) {
        return abbreviationToStateNameMap.get(stateName);
    }

    public String getAbbreviationFromStateName(String abbreviation) {
        return stateNameToAbbreviationMap.get(abbreviation);
    }

    public List<String> getStateNames() {
        return stateNameToAbbreviationMap.keySet().stream().toList();
    }

    public List<String> getStateAbbreviations() {
        return stateNameToAbbreviationMap.values().stream().toList();
    }

}
