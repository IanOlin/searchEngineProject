import java.io.IOException;
import java.text.Collator;
import java.util.*;
import java.util.Map.Entry;

import redis.clients.jedis.Jedis;

public class WikiSearch {

    // map from URLs that contain the term(s) to relevance score
    private Map<String, Integer> map;
    Jedis jedis;

    public WikiSearch(Map<String, Integer> map){
        this.map = map;
    }

    public Integer getRelevance(String url) {
        if (map.get(url) != null) {
            return map.get(url);
        }
        return 0;
    }

    // Prints the contents in order of term frequency.
    private  void print() {
        List<Entry<String, Integer>> entries = sort();
        for (Entry<String, Integer> entry: entries) {
            System.out.println(entry);
        }
    }

    // Computes the union of two search results.
    public WikiSearch or(WikiSearch that) {
        Map<String, Integer> unionMap = new HashMap<>();
        for (String url : map.keySet()) {
            unionMap.put(url, map.get(url));
        }
        for (String url : that.map.keySet()) {
            Integer mapCount = map.get(url);
            if (mapCount != null) {
                unionMap.put(url, that.map.get(url) + mapCount);
            } else {
                unionMap.put(url, that.map.get(url));
            }
        }
        return new WikiSearch(unionMap);
    }

    // Computes the intersection of two search results.
    public WikiSearch and(WikiSearch that) {
        Map<String, Integer> intersectionMap = new HashMap<>();
        for (String url : map.keySet()) {
            if (that.map.get(url) != null) {
                intersectionMap.put(url, map.get(url) + that.map.get(url));
            }
        }
        return new WikiSearch(intersectionMap);
    }

    // Computes the difference of two search results.
    public WikiSearch minus(WikiSearch that) {
        Map<String, Integer> differenceMap = new HashMap<>();
        for (String url : map.keySet()) {
            if (that.map.get(url) == null) {
                differenceMap.put(url, map.get(url));
            }
        }
        return new WikiSearch(differenceMap);
    }

    // Computes the relevance of a search with multiple terms.
    protected int totalRelevance(Integer rel1, Integer rel2) {
        return rel1 + rel2;
    }

    // Sort the results by relevance.
    public List<Entry<String, Integer>> sort() {
        List res = new LinkedList<>(map.entrySet());
        Collections.sort(res, new Comparator<Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> o1, Entry<String, Integer>  o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        return res;
    }


    // Performs a search and makes a WikiSearch object.
    public static WikiSearch search(String term, Index index) {

        // Fix this
        Map<String, Integer> map = index.getCounts(term);

        // Store the map locally in the WikiSearch
        return new WikiSearch(map);
    }

    // TODO: Choose an extension and add your methods here

    public static void main(String[] args) throws IOException {

        System.out.println("Enter a search term");
        Scanner sc = new Scanner(System.in);
        String term = sc.next();

        // make a Index
        Jedis jedis = JedisMaker.make();
        Index index = new Index(jedis); // You might need to change this, depending on how your constructor works.

        // search for the first term
//        String term1 = "java";
        System.out.println("Query: " + term);
        WikiSearch search1 = search(term, index);
        search1.print();

//        // search for the second term
//        String term2 = "programming";
//        System.out.println("Query: " + term2);
//        WikiSearch search2 = search(term2, index);
//        search2.print();
//
//        // compute the intersection of the searches
//        System.out.println("Query: " + term1 + " AND " + term2);
//        WikiSearch intersection = search1.and(search2);
//        intersection.print();
    }
}