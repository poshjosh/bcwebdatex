package com.bc.webdatex;

import com.bc.net.RetryConnectionFilter;
import com.bc.net.UserAgents;
import com.bc.task.AbstractStoppableTask;
import com.bc.dom.HtmlDocumentImpl;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import com.bc.webdatex.formatter.Formatter;
import java.text.MessageFormat;
import java.util.logging.Logger;
import com.bc.dom.HtmlDocument;
import com.bc.net.UrlUtil;

public class BaseCrawler<E> extends AbstractStoppableTask<E>
  implements Iterator<HtmlDocument>, Serializable {

    private static final Logger logger = Logger.getLogger(BaseCrawler.class.getName());
    
  protected final Serializable pageLock = new Serializable() {};
  
  private boolean removeParsedUrls;
  
  private int parsePos;
  
  private int parseLimit;
  
  private int batchSize;
  
  private int indexWithinBatch;
  
  private long batchInterval;
  
  private Set<String> attempted;
  
  private Set<String> failed;
  
  private List<String> pageLinks;
  
  private final Pattern imageUrlPattern;
  
  private Formatter<String> formatter;
  
  private ParserImpl parser;
  
  private RetryConnectionFilter reconnectAfterExceptionFilter;
  
  private UserAgents userAgents;

  public BaseCrawler() {
    this(new LinkedList());
  }
  
  public BaseCrawler(List<String> urlList) {
    init(urlList);
    imageUrlPattern = Pattern.compile(UrlUtil.getImageUrlRegex());
  }
  
  private void init(List<String> urlList) {
      
    logger.finer("Creating");
    
    this.removeParsedUrls = false;
    
    this.batchSize = 10;
    
    this.batchInterval = 10000L;
    
    this.attempted = new HashSet();
    
    this.failed = new HashSet();
    
    this.pageLinks = Collections.synchronizedList(urlList);

    this.reconnectAfterExceptionFilter = new RetryConnectionFilter(2, 2000L);
    
    this.userAgents = new UserAgents();
    
    this.parser = new ParserImpl();
  }
  
  protected void preParse(String url) {}
  
  protected void postParse(HtmlDocument dom) {}

  @Override
  public boolean hasNext() {
      
    if (!this.isStarted()) {
      this.setStarted(true);
    }
  
    boolean hasNext = false;
    
    if (isStopRequested()) {
      hasNext = false;
    }else{
        while ((isWithinParseLimit()) && (hasMoreUrls())) {

          if (isStopRequested()) {
            hasNext = false;
            break;
          }

          final String link = (String)this.pageLinks.get(this.parsePos);
          logger.log(Level.FINER, "UrlParser.hasNext. checking: {0}", link);

          if (isToBeCrawled(link)) {

            hasNext = true;

            break;
          }

          moveForward();
        }
    }
    
    if(this.isStopRequested()) {
        logger.fine(() -> "STOP REQUESTED, Last URL: " + this.getLinkSafely(parsePos - 1, null));
    }
    
    if(!hasNext) {
      logger.fine(() -> "UrlParser.hasNext: false, Last URL: " + this.getLinkSafely(parsePos - 1, null));
    }
    
    return hasNext;
  }
  
  private String getLinkSafely(int index, String outputIfNone) {
    final String link;
    if(pageLinks == null) {
        link = null;
    }else{
        if(index >= 0 && index < pageLinks.size()) {
            link = pageLinks.get(index);
        }else{
            link = null;
        }
    }
    return link == null ? outputIfNone : link;
  }
  
  @Override
  public HtmlDocument next() {
      
    if (!this.isStarted()) {
      this.setStarted(true);
    }
    
    if ((this.batchSize > 0) && (++this.indexWithinBatch >= this.batchSize)) {
        
      this.indexWithinBatch = 0;
      
      waitBeforeNextBatch(this.batchInterval);
    }
    
    final String rawUrl = (String)this.pageLinks.get(this.parsePos);

    String url = null;
    
    int bookmark = -1;
    
    HtmlDocument page;
    
    try {
        
      preParse(rawUrl);

      getAttempted().add(rawUrl);

      bookmark = this.pageLinks.size();

      url = this.formatter == null ? rawUrl : (String)this.formatter.apply(rawUrl);

      if(logger.isLoggable(Level.FINER)) {
        logger.log(Level.FINER, "Raw: {0}\nURL: {1}", new Object[]{rawUrl, url});
      }
      NodeList list = parse(url);
      
      page = new HtmlDocumentImpl(url, list);
      
      if (isNoFollow(page)) {
      
      }

      postParse(page);
      
    }catch (Exception e){
        
      page = null;  
      
      if(url != null && bookmark != -1) {
          
        final String updatedUrl = url.replace("&amp;", "&");
      
        if(updatedUrl.length() != url.length()) {
          
          this.pageLinks.add(bookmark, updatedUrl);
        }
      }
        
      boolean added = this.failed.add(rawUrl);

      if(added) { // We don't want to log the same URL twice
        logger.warning(() -> "Parse failed for: " + rawUrl + ". Reason: {1}" + e.toString());
      }
    }finally{
      
      this.moveForward();
    }
    
    return page;
  }
  
  private void moveForward() {
      
    logger.finest(() -> "Before moving forward. Parse pos: " + 
            this.parsePos + ", URL: {1}" + this.getLinkSafely(parsePos, null));
    
    if (!isRemoveParsedUrls()) {
      this.parsePos += 1;
    } else {
      this.pageLinks.remove(this.parsePos);
    }
    
    logger.finer(() -> "After moving forward. Parse pos: " + 
            this.parsePos + ", URL: {1}" + this.getLinkSafely(parsePos, "No more URLs"));
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Not supported!");
  }
  
  @Override
  protected E doCall() throws Exception {
    throw new UnsupportedOperationException("Please provide an implementation of this method");
  }
  
  protected boolean hasMoreUrls() {
      
    boolean hasMore = this.parsePos < this.pageLinks.size();
    
    logger.log(Level.FINEST, "Has more urls: {0}", hasMore);
    
    return hasMore;
  }
  
  public boolean isWithinParseLimit() {
      
    final boolean withinLimit = isWithLimit(this.parsePos, this.parseLimit);
    
    logger.finest(() -> MessageFormat.format("Parse pos: {0}, limit: {1}, within parse limit: {2}", 
            parsePos, parseLimit, withinLimit));
    
    return withinLimit;
  }
  
  protected boolean isWithLimit(int offset, int limit) {
    boolean withinLimit = true;
    if (limit > 0) {
      withinLimit = offset < limit;
    }
    return withinLimit;
  }

  public NodeList parse(String url) throws ParserException {
    NodeList list;
    
    try {
        
      String userAgent;
      try{
        userAgent = userAgents.getAny(url, false);
      }catch(MalformedURLException e) {
          userAgent = userAgents.getAny(false);
      }
      
      org.htmlparser.http.ConnectionManager.getDefaultRequestProperties().put(
              "User-Agent", userAgent);

      logger.fine(() -> MessageFormat.format("Pages left: {0}, crawling: {1}", 
              this.pageLinks.size() - this.parsePos, url));
    
      list = this.parser.parse(url);
      
      logger.log(Level.FINER, "Found: {0} nodes in page", list == null ? null : list.size());
    
    }catch (ParserException e) {

      boolean retry = isRetry(e, url);
      
      if (retry) {

        list = parse(url);
        
      } else {
          
        list = null;
      }
      
      if (list == null) {
          
        throw e;
      }
    }
    
    String size = String.valueOf(list == null ? null : list.size());  
    
    logger.fine(() -> "Nodes found: "+size+", URL: " + url);
    
    final NodeList logRef = list;
    logger.finest(() -> "Nodes found HTML:\n" + logRef.toHtml(false));
    
//System.out.println("Nodes found: "+size+", URL: "+url);
    return list;
  }

  protected boolean isRetry(ParserException e, String url)
    throws ParserException
  {
    if (this.reconnectAfterExceptionFilter == null) {
      return false;
    }
    
    Throwable t = e.getThrowable();
    
    boolean retry = false;
    
    String msg = null;
    
    if (null != t)
    {
      if (this.reconnectAfterExceptionFilter.accept(t))
      {
        retry = true;


      }
      else if (((t instanceof FileNotFoundException)) || (((t = t.getCause()) instanceof FileNotFoundException)))
      {
        msg = "Broken link ignored";
      }
    }
    

    if (retry)
    { 
      if(logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "Caught: {0}, while retrying: {1}", new Object[]{t, url});
      }
    }
    else
    {
      if (msg == null) { 
          msg = "Link ignored";
      }
      
      if(logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, "{0}. {1}", new Object[]{msg, e});
      }
    }

    return retry;
  }
  
  private boolean isNoFollow(HtmlDocument page) {
    if (page.getRobots() == null) {
      return false;
    }
    return page.isRobotsMetaTagContentContaining("nofollow");
  }
  
  protected void nofollow(int bookmark) {
    synchronized (this.pageLock)
    {
      for (int i = bookmark; i < this.pageLinks.size(); i++) {
        this.pageLinks.remove(i);
      }
    }
  }

  public boolean isToBeCrawled(String link) {

    boolean toBeCrawled = !isAttempted(link);
    
    if(toBeCrawled) {
      toBeCrawled = !this.isImageLink(link);
    }
    
    if(logger.isLoggable(Level.FINER)) {
      logger.log(Level.FINER, "To be crawled: {0}, Link: {1}", new Object[]{toBeCrawled, link});
    }
    
    return toBeCrawled;
  }
  
  protected boolean isImageLink(String link) {
    return imageUrlPattern.matcher(link).matches();
  }
  
  protected boolean isAttempted(String link) {
    final boolean alreadyAttempted = getAttempted().contains(link);
    if(alreadyAttempted) {
      logger.log(Level.FINER, "Already attempted: {0}", link);  
    }
    return alreadyAttempted;
  }
  
  protected synchronized void waitBeforeNextBatch(long interval)
  {
    try
    {
      if (this.reconnectAfterExceptionFilter != null) {
        this.reconnectAfterExceptionFilter = this.reconnectAfterExceptionFilter.copy();
      }
      
      if (interval > 0L)
      {
        final long mb4 = com.bc.util.Util.availableMemory();
        logger.finer(() -> "Waiting for "+interval+" milliseconds, free memory: " + mb4);

        wait(interval);
        
        logger.finer(() -> "Done waiting for "+interval+" milliseconds, memory saved: " + 
                com.bc.util.Util.usedMemory(mb4));
      }
    }
    catch (InterruptedException e)
    {
      logger.log(Level.WARNING, "Wait interrupted", e);
    } finally {
      notifyAll();
    }
  }
  
  public int getParsePos() {
    return this.parsePos;
  }
  
  public Set<String> getAttempted() {
    return this.attempted;
  }
  
  public Set<String> getFailed() {
    return this.failed;
  }
  
  public boolean isRemoveParsedUrls() {
    return this.removeParsedUrls;
  }
  
  public void setRemoveParsedUrls(boolean b) {
    this.removeParsedUrls = b;
  }
  
  public ParserImpl getParser() {
    return this.parser;
  }
  
  public void setParser(ParserImpl parser) {
    this.parser = parser;
  }
  
  public List<String> getPageLinks() {
    return this.pageLinks;
  }
  
  public void setPageLinks(List<String> pageLinks) {
    this.pageLinks = Collections.synchronizedList(pageLinks);
  }
  
  public long getBatchInterval() {
    return this.batchInterval;
  }
  
  public void setBatchInterval(long batchInterval) {
    this.batchInterval = batchInterval;
  }
  
  public int getBatchSize() {
    return this.batchSize;
  }
  
  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }
  
  public RetryConnectionFilter getReconnectAfterExceptionFilter() {
    return this.reconnectAfterExceptionFilter;
  }
  
  public void setReconnectAfterExceptionFilter(RetryConnectionFilter filter) {
    this.reconnectAfterExceptionFilter = filter;
  }
  
  public Formatter<String> getFormatter() {
    return this.formatter;
  }
  
  public void setFormatter(Formatter<String> urlFormatter) {
    this.formatter = urlFormatter;
  }
  
  public List<String> getCookies() {
    return this.parser.getCookies();
  }
  
  public boolean addCookies(List<String> cookies) {
    return this.parser.addCookies(cookies);
  }
  
  public int getParseLimit() {
    return this.parseLimit;
  }
  
  public void setParseLimit(int limit) {
    this.parseLimit = limit;
  }
  
  @Override
  public String getTaskName() {
    return getClass().getName();
  }
  
  @Override
  public void print(StringBuilder builder) {
    builder.append(getTaskName());
    builder.append(", ParsePos: ").append(this.parsePos);
    builder.append(", Urls Left: ").append(this.pageLinks == null ? null : Integer.valueOf(this.pageLinks.size() - this.parsePos));
    builder.append(", Attempted: ").append(this.attempted == null ? null : Integer.valueOf(this.attempted.size()));
    builder.append(", Failed: ").append(this.failed == null ? null : Integer.valueOf(this.failed.size()));
  }
}
