package com.bc.webdatex;

import com.bc.io.CharFileIO;
import com.bc.net.HttpStreamHandlerForBadStatusLine;
import com.bc.net.RetryConnectionFilter;
import com.bc.net.UserAgents;
import com.bc.task.AbstractStoppableTask;
import com.bc.util.Util;
import com.bc.util.XLogger;
import com.bc.webdatex.util.ParserConnectionManager;
import com.bc.webdatex.tags.ArticleTag;
import com.bc.webdatex.tags.Link;
import com.bc.webdatex.tags.NoscriptTag;
import com.bc.webdatex.tags.StrongTag;
import com.bc.webdatex.tags.Tbody;
import com.bc.webdatex.tags.Tfoot;
import com.bc.webdatex.tags.Thead;
import com.bc.webdatex.nodedata.SimpleDom;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.http.ConnectionMonitor;
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserFeedback;
import com.bc.webdatex.nodedata.Dom;
import com.bc.webdatex.formatter.Formatter;

public class URLParser<E> extends AbstractStoppableTask<E>
  implements Iterator<Dom>, Serializable {
    
  protected final Serializable pageLock = new Serializable() {};
  
  private final Class cls = URLParser.class;
  private final XLogger logger = XLogger.getInstance();
  
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
  
  private Parser parser;
  
  private List<String> cookies;
  
  private RetryConnectionFilter reconnectAfterExceptionFilter;
  
  private com.bc.net.ConnectionManager connMgr;

  public URLParser() {
    this(new LinkedList());
  }
  
  public URLParser(List<String> urlList) {
    init(urlList);
    imageUrlPattern = Pattern.compile(Util.getImageUrlRegex());
  }
  
  private void init(List<String> urlList) {
      
    logger.log(Level.FINER, "Creating", cls);
    
    this.removeParsedUrls = false;
    
    this.batchSize = 10;
    
    this.batchInterval = 10000L;
    
    this.attempted = new HashSet();
    
    this.failed = new HashSet();
    
    this.pageLinks = Collections.synchronizedList(urlList);

    this.reconnectAfterExceptionFilter = new RetryConnectionFilter(2, 2000L);
    
    this.connMgr = new com.bc.net.ConnectionManager();
    this.connMgr.setAddCookies(true);
    this.connMgr.setGetCookies(true);
    this.connMgr.setGenerateRandomUserAgent(true);
    
    this.parser = new Parser();
    
    Parser.setConnectionManager(new ParserConnectionManager());
    
    org.htmlparser.http.ConnectionManager cm = Parser.getConnectionManager();
    
    cm.setRedirectionProcessingEnabled(true);
    
    cm.setCookieProcessingEnabled(true);
    
    cm.setMonitor(newConnectionMonitor());
    
    PrototypicalNodeFactory factory = new PrototypicalNodeFactory();
    
    factory.registerTag(new StrongTag());
    factory.registerTag(new Tbody());
    factory.registerTag(new Thead());
    factory.registerTag(new Tfoot());
    factory.registerTag(new NoscriptTag());
    factory.registerTag(new ArticleTag());
    factory.registerTag(new Link());
    
    this.parser.setNodeFactory(factory);
    
    this.parser.setFeedback(new ParserFeedback(){
        @Override
        public void info(String message) {
            XLogger.getInstance().log(Level.INFO, message, this.getClass());
            
        }
        @Override
        public void warning(String message) {
            XLogger.getInstance().log(Level.WARNING, message, this.getClass());
        }
        @Override
        public void error(String message, ParserException e) {
            XLogger.getInstance().log(Level.WARNING, message, this.getClass(), e);
        }
    });
  }
  
  protected void preParse(String url) {}
  
  protected void postParse(Dom dom) {}

  @Override
  public boolean hasNext() {
      
    if (!this.isStarted()) {
      this.setStarted(true);
    }
  
    if (isStopRequested()) {
      return false;
    }
    
    boolean output = false;

    while ((isWithinParseLimit()) && (hasMoreUrls())) {

      if (isStopRequested()) {
        output = false;
        break;
      }
      
      String s = (String)this.pageLinks.get(this.parsePos);
      logger.log(Level.FINER, "UrlParser.hasNext. checking: {0}", cls, s);
      
      if (isToBeCrawled(s)) {
          
        output = true;
        
        break;
      }
      
      moveForward();
    }
    
    logger.log(Level.FINE, "UrlParser.hasNext: {0}", cls, output);
    
    return output;
  }
  
  @Override
  public Dom next() {
      
    if (!this.isStarted()) {
      this.setStarted(true);
    }
    
    if ((this.batchSize > 0) && (++this.indexWithinBatch >= this.batchSize)) {
        
      this.indexWithinBatch = 0;
      
      waitBeforeNextBatch(this.batchInterval);
    }
    
    String rawUrl = (String)this.pageLinks.get(this.parsePos);

    Dom page;
    
    try {
        
      preParse(rawUrl);

      getAttempted().add(rawUrl);

      final int bookmark = this.pageLinks.size();

      String url = this.formatter == null ? rawUrl.replace("&amp;", "&") : (String)this.formatter.format(rawUrl.replace("&amp;", "&"));

      logger.log(Level.FINER, "Raw: {0}\nURL: {1}", cls, rawUrl, url);

      NodeList list = parse(url);
      
      page = new SimpleDom(rawUrl, url, list);
      
      if (isNoFollow(page)) {
      
      }

      postParse(page);
      
    }catch (Exception e){
        
      page = null;  
        
      boolean added = this.failed.add(rawUrl);

      if(added) { // We don't want to log the same URL twice
        logger.log(Level.WARNING, "Parse failed for: {0}. Reason: {1}", cls, rawUrl, e.toString());
      }
    }finally{
      
      this.moveForward();
    }
    
    return page;
  }
  
  private void moveForward() {
      
    logger.log(Level.FINEST, "Before moving forward. Parse pos: {0}, Url: {1}", 
            cls, this.parsePos, this.pageLinks.get(this.parsePos));
    
    if (!isRemoveParsedUrls()) {
      this.parsePos += 1;
    } else {
      this.pageLinks.remove(this.parsePos);
    }
    
    logger.log(Level.FINER, "After moving forward. Parse pos {0}, Url: {1}", cls, 
    this.parsePos, this.parsePos < this.pageLinks.size() ? this.pageLinks.get(this.parsePos) : "no more URLs");
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
    
    logger.log(Level.FINEST, "Has more urls: {0}", cls, hasMore);
    
    return hasMore;
  }
  
  public boolean isWithinParseLimit() {
      
    boolean withinLimit = isWithLimit(this.parsePos, this.parseLimit);
    
    logger.log(Level.FINEST, "Parse pos: {0}, limit: {1}, within parse limit: {2}", 
            cls, parsePos, parseLimit, withinLimit);
    
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
        
      UserAgents userAgents = this.connMgr.getUserAgents();
      String userAgent;
      try{
        userAgent = userAgents.getAny(url, false);
      }catch(MalformedURLException e) {
          userAgent = userAgents.getAny(false);
      }
      
      org.htmlparser.http.ConnectionManager.getDefaultRequestProperties().put(
              "User-Agent", userAgent);

      try {
          
        list = doParse(url);
        
      }catch (EncodingChangeException ece) {
          
        list = applyBugfix991895(ece);
      }
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
//System.out.println("Nodes found: "+size+", URL: "+url);
    if (logger.isLoggable(Level.FINEST, cls)) {
      logger.log(Level.FINEST, "URL: {0}, Nodes Found Html:\n{1}", cls, url, list.toHtml(false));
    } else if (logger.isLoggable(Level.FINE, cls)) {
      logger.log(Level.FINE, "Nodes found: {0}, URL: {1}", cls, size, url);
    }
    
    return list;
  }

  protected NodeList doParse(String url)  throws ParserException {
    return doParse_0(url);
  }
  
  protected NodeList doParse_0(String url) throws ParserException {
      
    logger.log(Level.FINE, "Pages left: {0}, crawling: {1}", 
            cls, getPageLinks().size() - this.parsePos, url);
    
    this.parser.setURL(url);
    
    NodeList list = null;
    
    NodeIterator e = this.parser.elements();
    
    while (e.hasNext()) {
        
      Node node = e.next();
      
      if (list == null) {
        list = new NodeList();
      }

      list.add(node);
    }
    
    logger.log(Level.FINER, "Found: {0} nodes in page", cls, list == null ? null : Integer.valueOf(list.size()));
    
    return list;
  }
  
  protected NodeList doParse_1(String urlString) throws ParserException {
      
    logger.log(Level.FINE, "Pages left: {0}, crawling: {1}", 
        cls, getPageLinks().size() - this.parsePos, urlString);

    try {
      
      URL url = new URL(null, urlString, new HttpStreamHandlerForBadStatusLine());
      
      InputStream in = this.connMgr.getInputStream(url);
      
      CharSequence html = new CharFileIO().readChars(in);
      
      this.parser.setInputHTML(html.toString());

    } catch (IOException e) {
        
      logger.log(Level.WARNING, "{0}", cls, e.toString());

      this.parser.setURL(urlString);
    }
    
    NodeList list = null;
    
    NodeIterator e = this.parser.elements();
    
    while (e.hasNext()) {
        
      Node node = e.next();
      
      if (list == null) {
        list = new NodeList();
      }
      
      list.add(node);
    }
    
    logger.log(Level.FINER, "Found: {0} nodes in page", cls, list == null ? null : Integer.valueOf(list.size()));
    
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
      logger.log(Level.FINE, "Caught:{0}, Retrying: {1}", cls, t, url == null ? "" : url);

    }
    else
    {
      if (msg == null) { msg = "Link ignored";
      }
      
      logger.log(Level.FINE, "{0}. {1}", cls, msg, e);
    }
    

    return retry;
  }
  
  private NodeList applyBugfix991895(EncodingChangeException ece)
    throws ParserException
  {
    logger.log(Level.WARNING, "PARSER CRASHED! Caught: " + ece.getClass().getName() + "\nApplying bug fix #991895", cls, ece);

    this.parser.reset();
    
    NodeList list = new NodeList();
    
    for (NodeIterator e = this.parser.elements(); e.hasNext();) {
      list.add(e.next());
    }
    
    return list;
  }
  
  private boolean isNoFollow(Dom page) {
    if (page.getRobots() == null) {
      return false;
    }
    String content = page.getRobots().getAttributeValue("content");
    return (content.contains("none")) || (content.contains("nofollow"));
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
    
    if (!toBeCrawled) {
      logger.log(Level.FINER, "Already attempted: {0}", cls, link);
    }
    
    logger.log(Level.FINER, "To be crawled: {0}, Link: {1}", cls, toBeCrawled, link);
    
    return toBeCrawled;
  }
  
  protected boolean isImageLink(String link) {
    return imageUrlPattern.matcher(link).matches();
  }
  
  protected boolean isAttempted(String link) {
    return getAttempted().contains(link);
  }
  
  protected synchronized void waitBeforeNextBatch(long interval)
  {
    try
    {
      if (this.reconnectAfterExceptionFilter != null) {
        this.reconnectAfterExceptionFilter.reset();
      }
      
      if (interval > 0L)
      {
        long freeMemory = Runtime.getRuntime().freeMemory();
        logger.log(Level.FINER, "Waiting for {0} milliseconds, free memory: {1}", cls, Long.valueOf(interval), Long.valueOf(freeMemory));

        wait(interval);
        
        logger.log(Level.FINE, "Done waiting for {0} milliseconds, memory saved: {1}", cls, Long.valueOf(interval), Long.valueOf(freeMemory - Runtime.getRuntime().freeMemory()));
      }
    }
    catch (InterruptedException e)
    {
      logger.log(Level.WARNING, "Wait interrupted", cls, e);
    } finally {
      notifyAll();
    }
  }
  
  private ConnectionMonitor newConnectionMonitor()
  {
    return new ConnectionMonitor()
    {
      @Override
      public void preConnect(HttpURLConnection connection) throws ParserException {
        XLogger.getInstance().log(Level.FINER, "@preConnect. Connection: {0}", getClass(), connection);
        
        addCookies(connection, URLParser.this.cookies);
      }
      
      public void addCookies(URLConnection connection, List<String> cookies) {
        if ((cookies == null) || (cookies.isEmpty())) { return;
        }
        for (String cookie : cookies)
        {

          String str = cookie.split(";", 2)[0];
          XLogger.getInstance().log(Level.FINER, "Adding cookie: {0}", getClass(), str);
          connection.addRequestProperty("Cookie", str);
        }
      }
      
      @Override
      public void postConnect(HttpURLConnection connection) throws ParserException {}
    };
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
  
  public Parser getParser() {
    return this.parser;
  }
  
  public void setParser(Parser parser) {
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
    return this.cookies;
  }
  
  public void setCookies(List<String> cookies) {
    this.cookies = cookies;
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