import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveAction;

import static java.lang.Thread.sleep;

public class SiteMapNodeRecursiveTask extends RecursiveAction {
    private SiteMapNode url;
    private String rootUrl;
    private static Set<String> allLinks = new CopyOnWriteArraySet<>();

    private static final int LARGE_TIMEOUT_MS = 100000;

    public SiteMapNodeRecursiveTask(SiteMapNode url, String rootUrl) {
        this.url = url;
        this.rootUrl = rootUrl;
    }

    @Override
    protected void compute() {
        Set<SiteMapNodeRecursiveTask> taskList = new HashSet<>();
        try {
            sleep(150);
            Document doc = Jsoup.connect(url.getUrl()).timeout(LARGE_TIMEOUT_MS).get();
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String absUrl = link.attr("abs:href");
                if (isCorrected(absUrl)) {
                    url.addSublinks((new SiteMapNode(absUrl)));
                    allLinks.add(absUrl);
                }
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
        for (SiteMapNode link : url.getSublinks()) {
            SiteMapNodeRecursiveTask task = new SiteMapNodeRecursiveTask(link, rootUrl);
            task.fork();
            taskList.add(task);
        }
        for (SiteMapNodeRecursiveTask task : taskList) {
            task.join();
        }
    }

    private boolean isCorrected(String url) {
        return (!url.isEmpty() && url.startsWith(rootUrl)
                && !allLinks.contains(url)
                && !url.contains("#")
                && !url.matches("([^\\s]+(\\.(?i)(jpg|png|gif|bmp|pdf))$)"));
    }
}
