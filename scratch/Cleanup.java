import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class Cleanup {
    public static void main(String[] args) throws Exception {
        Set<String> unusedStrings = new HashSet<>();
        Set<String> unusedDimens = new HashSet<>();
        Set<String> unusedStyles = new HashSet<>();

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse("app/build/reports/lint-results-debug.xml");
        NodeList issues = doc.getElementsByTagName("issue");

        for (int i = 0; i < issues.getLength(); i++) {
            Element issue = (Element) issues.item(i);
            if ("UnusedResources".equals(issue.getAttribute("id"))) {
                String msg = issue.getAttribute("message");
                Matcher m = Pattern.compile("`R\\.([^.]+)\\.([^`]+)`").matcher(msg);
                if (m.find()) {
                    String type = m.group(1);
                    String name = m.group(2);
                    if (type.equals("string")) unusedStrings.add(name);
                    else if (type.equals("dimen")) unusedDimens.add(name);
                    else if (type.equals("style")) unusedStyles.add(name);
                }
            }
        }

        cleanFile("app/src/main/res/values/strings.xml", unusedStrings, "string");
        cleanFile("app/src/main/res/values/dimens.xml", unusedDimens, "dimen");
        cleanFile("app/src/main/res/values/themes.xml", unusedStyles, "style");
    }

    static void cleanFile(String path, Set<String> unused, String tagType) throws Exception {
        Path p = Paths.get(path);
        if (!Files.exists(p)) return;
        List<String> lines = Files.readAllLines(p);
        List<String> newLines = new ArrayList<>();
        Pattern ptn = Pattern.compile("<" + tagType + " name=\"([^\"]+)\"");
        
        for (String line : lines) {
            Matcher m = ptn.matcher(line);
            if (m.find()) {
                String name = m.group(1);
                if (unused.contains(name)) continue;
            }
            newLines.add(line);
        }
        Files.write(p, newLines);
        System.out.println("Cleaned " + path);
    }
}
