import com.opencsv.CSVWriter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Extractor {

    private static final int MAX_DEPTH = 2;
    private static final int MAX_VISITED_PAGES = 100;
    File file = new File("1.csv");
    ArrayList<String> words = new ArrayList<>(Arrays.asList("wikipedia", "Elon", "car"));// here you add words which you want to find
    ArrayList<String> wordRepeat = new ArrayList<>();//here we save result of counting in form "SUCH_WORD was found NUMBER times"
    ArrayList<String> allInformationToWrite = new ArrayList<>();//this we need to write data to file. One cell contains "title, url, words we looking for"


    public Extractor() {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        HashSet<String> visitedLinks = new HashSet<>();
        new Extractor().crawl(0, "https://en.wikipedia.org/wiki/Elon_Musk", visitedLinks);
    }

    public void crawl(int level, String url, HashSet<String> visited) {
        if (level <= MAX_DEPTH && visited.size() < MAX_VISITED_PAGES) {
            Document document = suitableDocument(url, visited);
            if (document != null) {//it is okay to visit web-site
                for (Element link : document.select("p a[href]")) {//get links
                    String absoluteLink = link.absUrl("href");
                    if (!visited.contains(absoluteLink)) {
                        crawl(level++, absoluteLink, visited);
                    }
                }
            }
        }
        return;
    }

    //check if document is possible to watch
    public Document suitableDocument(String url, HashSet<String> v) {
        if (url == null || url.length() == 0)
            return null;
        try {
            Connection connection = Jsoup.connect(url);
            Document doc = connection.get();
            if (connection.response().statusCode() == 200) {
                for (String word : words) {
                    wordRepeat.add(lookingForWords(word, doc));
                }
                writeInformation(doc.title(), url, wordRepeat);
                v.add(url);
                System.out.println(v.size());//!!!
                wordRepeat.clear();
                return doc;
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }


    //responsible for looking words
    public String lookingForWords(String wordWeLookingFor, Document document) throws IOException {
        Pattern pattern = Pattern.compile("\\b(" + wordWeLookingFor + ")\\b", Pattern.CASE_INSENSITIVE);
        int repeats = 0;
        for (Element element : document.select("div")) {
            String str = element.text();
            Matcher matcher = pattern.matcher(str);
            while (matcher.find()) {
                repeats++;
            }
        }
        return wordWeLookingFor + " repeats " + repeats + " times.";
    }

    //responsible for creating document
    public void writeInformation(String title, String URL, ArrayList<String> toWrite) {//!!!
//        try (OutputStream outputStream = new FileOutputStream(file, true)) {
//            outputStream.write(title.getBytes(StandardCharsets.UTF_8));
//            outputStream.write(URL.getBytes(StandardCharsets.UTF_8));
//            outputStream.write(wordOne.getBytes(StandardCharsets.UTF_8));
//            outputStream.write(10);// i do not know how explain it https://stackoverflow.com/questions/24243348/how-to-write-new-line-in-java-fileoutputstream/24251559 comment with 2 likes
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try (FileWriter infoToWrite = new FileWriter(file, true)) {
            CSVWriter writer = new CSVWriter(infoToWrite, ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            allInformationToWrite.add(title);
            allInformationToWrite.add(URL);
            for (String write : toWrite) {
                allInformationToWrite.add(write);
            }
            String[] transfer = allInformationToWrite.toArray(new String[allInformationToWrite.size()]);
            writer.writeNext(transfer);
            allInformationToWrite.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
