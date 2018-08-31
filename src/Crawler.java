import java.io.*;
import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import main.java.com.arun.trie.MapTrie;
class Crawler {
  private Document doc;
  private int docidx = 1;
  private String sex;
  private MapTrie<Boolean> trie;
  Crawler(String sex) {
    this.sex =  sex;
    trie = new MapTrie<>();
    try {
      if (sex == "male") {
        doc = Jsoup.connect("https://en.wikipedia.org/wiki/Category:American_male_film_actors").get();
      } else {
        doc = Jsoup.connect("https://en.wikipedia.org/wiki/Category:American_film_actresses").get();
      }
      buildTrie();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void buildTrie() throws IOException {
    BufferedReader br = new BufferedReader(new FileReader("imdb/imdb.txt"));
    String line  = br.readLine();
    while (line != null) {
      String[] profile = line.split(",");
      String job = Objects.equals(sex, "male") ? "actor" : "actress";
      if (Objects.equals(profile[5], sex) || job.equals(profile[4])) {
        String name = profile[1].toLowerCase().replace(" ", "").replace(".", "");
        System.out.println("Inserting " + name);
        trie.insert(name, true);
      }
      line = br.readLine();
    }
    System.out.println("Build Trie complete. Size: " + trie.size());

  }

  void printPage() {
    System.out.println(doc.html());
  }

  void getActors() throws InterruptedException, IOException {
    Elements actors = doc.select("#mw-pages li a");
    PrintWriter writer = new PrintWriter(sex + "_actors"+docidx+".txt", "UTF-8");
    for (Element actor : actors) {
      writer.print("Name:" + actor.text()+";");
      if (notInterested(actor.text())) {
        System.out.println("Skipping " + actor.text());
        continue;
      }
      System.out.println("Saving " + actor.text());
      Thread.sleep(50);
      Document doc = Jsoup.connect(actor.absUrl("href")).get();
//	    start diggging for information
//      Elements information = doc.select(".biography tr+ tr th , .biography th+ td");
//      if (information.size() == 0) {
//        information = doc.select(".vcard td , .vcard tr+ tr th");
//      }
//      boolean endOfPair = false;
//      for (Element infocell : information) {
//          if (!endOfPair) {
//            writer.print(infocell.text().replace(";", ",").replace(":", "$") + ":");
//            endOfPair = true;
//          } else {
//            writer.print(infocell.text().replace(";", ",").replace(":", "$").replace("\n", "") + ";");
//            endOfPair = false;
//          }
//      }
      Element img = doc.selectFirst(".vcard img");
      if (!Objects.equals(null, img)){
        writer.print("Image:" + img.absUrl("src") + ";");
      }
      Elements links = doc.select(".toclevel-1 a");
      if(links.size() != 0) {
        writer.print("Links:");
        for (Element link : links) {
          writer.print(link.text().replace("$", "").replace(".", "_")+ ":" + link.absUrl("href")+ "$");
        }
      }

      writer.println();
    }
    writer.close();
    docidx++;
    Thread.sleep(200);
    if (doc.selectFirst("#mw-pages a:containsOwn(next page)") != null) {
      System.out.println("Starting new document " + docidx);
      doc = Jsoup.connect(doc.selectFirst("#mw-pages a:containsOwn(next page)").absUrl("href")).get();
      getActors();
    }
  }

  private boolean notInterested(String name) {
    if (name.contains("(")) name = name.substring(0, name.indexOf("(")).trim();
    name = name.toLowerCase().replace(" ", "").replace(".", "");
    return !trie.contains(name);
  }
}
