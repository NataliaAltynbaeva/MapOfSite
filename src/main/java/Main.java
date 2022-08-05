import java.util.concurrent.ForkJoinPool;

public class Main {
    private static String SITE_URL = "https://skillbox.ru/";

    private static final String SITEMAP_DOC = "src/file/map.txt";

    public static void main(String[] args) {
        SiteMapNode rootUrl = new SiteMapNode(SITE_URL);
        new ForkJoinPool().invoke(new SiteMapNodeRecursiveTask(rootUrl, SITE_URL));
        rootUrl.writeSitemapUrl(SITEMAP_DOC);
    }
}
