import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class SiteMapNode {
    private String url;
    private volatile SiteMapNode parent;
    private volatile Set<SiteMapNode> sublinks = new CopyOnWriteArraySet<>();

    public SiteMapNode(String url) {
        this.url = url;
    }

    public void addSublinks(SiteMapNode sublink) {
        if (!sublinks.contains(sublink) && sublink.getUrl().startsWith(url)) {
            this.sublinks.add(sublink);
            sublink.setParent(this);
        }
    }

    public void setParent(SiteMapNode siteMapNode) {
        synchronized (this) {
            this.parent = siteMapNode;
        }
    }

    public int getDepth() {
        if (parent == null) {
            return 0;
        }
        return parent.getDepth() + 1;
    }

    public Set<SiteMapNode> getSublinks() {
        return Collections.unmodifiableSet(sublinks);
    }

    public String getUrl() {
        return url;
    }

    public void writeSitemapUrl(String sitemapDoc) {
        int depth = getDepth();
        String tabs = String.join("", Collections.nCopies(depth, "\t"));
        StringBuilder result = new StringBuilder(tabs + url + "\n");
        appendStringInFile(sitemapDoc, result.toString());
        sublinks.forEach(link -> link.writeSitemapUrl(sitemapDoc));
    }

    private void appendStringInFile(String fileName, String data) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(new File(fileName), true);
            outputStream.write(data.getBytes(), 0, data.length());
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}