import java.io.IOException;

public class CrawlerMain {
  public static void main(String[] args) throws IOException, InterruptedException {
    Crawler crl = new Crawler("female");
    crl.getActors();
  }
}
