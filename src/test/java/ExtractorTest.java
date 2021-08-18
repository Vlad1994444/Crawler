import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class ExtractorTest {

    Extractor a;
    HashSet<String> visited;

    @BeforeEach
    public void setUp() {
        a = new Extractor();
        visited = new HashSet<>();
    }
    @Test
    public void if_URL_null_we_do_not_have_URLs_in_collection() {
        a.crawl(0, null, visited);
        int expected = 0;
        int result = visited.size();
        assertEquals(expected, result);
    }
    @Test
    public void if_URL_normal_but_not_from_wiki_than_app_add_one_URL_to_collection(){//this work so because all links inside "div"
        a.crawl(0, "https://yandex.by", visited);
        int expected = 1;
        int result = visited.size();
        assertEquals(expected, result);
    }
    @Test
    public void any_wiki_URL_give_good_result_with_big_number_of_URLs(){//this work so because all links inside "div"
        a.crawl(0, "https://en.wikipedia.org/wiki/Polymorphism_(computer_science)", visited);
        int expected = 100;
        int result = visited.size();
        assertEquals(expected, result);
    }
    @Test
    public void any_working_URL_is_good() {
        a.suitableDocument("https://yandex.by", visited);
        int expected = 1;
        int result = visited.size();
        assertEquals(expected, result);
    }
    @Test
    public void bad_working_URL_do_not_increase_collection() {
        a.suitableDocument("https://yaannnndddddeeeeexxx.by", visited);
        int expected = 0;
        int result = visited.size();
        assertEquals(expected, result);
    }
    @Test
    public void we_find_words_where_they_should_be() throws IOException{
        Document document = Jsoup.connect
                ("https://www.google.com/search?q=ice-cream&client=opera&hs=oQ5&sxsrf=ALeKk03QGKdHm1EY3z7Z2z2y8CdI-45kBA:1629296217539&ei=WRYdYZCLIMT87_UPxaOTyA0&start=10&sa=N&ved=2ahUKEwjQm56y4bryAhVE_rsIHcXRBNkQ8tMDegQIAhA6&biw=1639&bih=898").get();
        boolean weDoNotHave_ice_Word = false;
        String result = a.lookingForWords("ice", document);
        Pattern pattern = Pattern.compile("\\b(0)\\b");// pattern fot number zero
        Matcher matcher = pattern.matcher(result);
        if(matcher.find()){//if we find just zero (not 10 or another) app get "true"
            weDoNotHave_ice_Word = true;
        }
        assertFalse(weDoNotHave_ice_Word);
    }
    @Test
    public void we_do_not_find_unexisting_words() throws IOException{
        Document document = Jsoup.connect
                ("https://www.google.com/search?q=ice-cream&client=opera&hs=oQ5&sxsrf=ALeKk03QGKdHm1EY3z7Z2z2y8CdI-45kBA:1629296217539&ei=WRYdYZCLIMT87_UPxaOTyA0&start=10&sa=N&ved=2ahUKEwjQm56y4bryAhVE_rsIHcXRBNkQ8tMDegQIAhA6&biw=1639&bih=898").get();
        boolean unexistingWord = true;
        String result = a.lookingForWords("abcdefgxyz", document);
        Pattern pattern = Pattern.compile("\\b(0)\\b");
        Matcher matcher = pattern.matcher(result);
        if(matcher.find()){// if we do not find "abcdefgxyz" method returns "0" and app will catch it
            unexistingWord = false;
        }
        assertFalse(unexistingWord);
    }

}
