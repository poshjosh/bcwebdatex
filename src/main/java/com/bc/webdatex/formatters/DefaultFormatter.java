package com.bc.webdatex.formatters;

import com.bc.fxrateservice.FxRate;
import com.bc.fxrateservice.FxRateService;
import com.bc.json.config.JsonConfig;
import com.bc.net.ConnectionManager;
import com.bc.net.UrlUtil;
import com.bc.util.Log;
import com.bc.webdatex.formatters.Formatter;
import com.bc.webdatex.config.Config;
import com.bc.webdatex.context.CapturerContext;
import com.bc.webdatex.util.Util;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class DefaultFormatter
  implements Formatter<Map<String, Object>>, Serializable {
    
  public static final String APPLY_TEXT = "CLICK HERE TO APPLY";
  private CapturerContext context;
  private DateFormat inputDateformat;
  private DateFormat outputDateformat;
  private List jobRequestFields;
  private Map defaultValues;
  
  public DefaultFormatter() {
      
    SimpleDateFormat infmt = new SimpleDateFormat();
    
    infmt.applyPattern("EEE MMM dd HH:mm:ss G yyyy");
    this.inputDateformat = infmt;
    
    SimpleDateFormat outfmt = new SimpleDateFormat();
    
    outfmt.applyPattern("MM dd yyyy HH:mm:ss.S");
    this.outputDateformat = outfmt;
  }
  
  public DefaultFormatter(CapturerContext context) {
      
    this.context = context;
    
    JsonConfig config = context.getConfig();
    
    Object[] arr = config.getArray(new Object[] { Config.Formatter.jobRequestFields });
    if (arr != null) {
      this.jobRequestFields = new ArrayList();
      this.jobRequestFields.addAll(Arrays.asList(arr));
    }
    
    this.defaultValues = context.getNodeExtractorConfig().getDefaults();
    
    Object[] datePatterns = config.getArray(new Object[] { Config.Formatter.datePatterns });
    
    if ((datePatterns == null) || (datePatterns.length == 0)) {
      this.inputDateformat = new SimpleDateFormat();
    } else {
      final String [] datePatternsStrArr = (String[])Arrays.copyOf(datePatterns, datePatterns.length, String[].class); 
      Log.getInstance().log(Level.FINER, "Config: {0}, Date patterns: {1}", 
              getClass(), config.getName(), Arrays.toString(datePatternsStrArr));
      
      this.inputDateformat = new MyDateFormat(datePatternsStrArr);
    }
    
    SimpleDateFormat outfmt = new SimpleDateFormat();
    
    outfmt.applyPattern("MM dd yyyy HH:mm:ss.S");
    this.outputDateformat = outfmt;
  }
  
  protected Map<String, Object> createCopy(Map<String, Object> parameters) {
    HashMap update = new HashMap();
    for (Map.Entry<String, Object> en : parameters.entrySet()) {
      Object val = en.getValue();
      if (val != null) {
        val = val.toString().trim();
      }
      update.put(en.getKey(), val);
    }
    return update;
  }
  
  @Override
  public Map<String, Object> apply(Map<String, Object> parameters)
  {
    Log.getInstance().log(Level.FINER, "BEFORE Params: {0}", getClass(), parameters);

    Map<String, Object> copy = createCopy(parameters);
    
    copy = formatCurrencyTypes(copy);

    copy = formatDateTypes(copy);
    try
    {
      int status = formatDateinAndExpiryDate(copy);
      copy.put("status", Integer.valueOf(status));
    } catch (RuntimeException e) {
      Log.getInstance().log(Level.WARNING, null, getClass(), e);
    }
    
    copy = formatLinks(copy);
    
    copy = addDefaults(copy);
    
    Log.getInstance().log(Level.FINER, "{0}. AFTER Params: {1}", getClass(), copy);
    
    return copy;
  }
  
  protected Map<String, Object> addDefaults(Map<String, Object> parameters)
  {
    Log.getInstance().log(Level.FINER, "BEFORE adding defaults: {0}", getClass(), parameters);
    
    if ((this.defaultValues == null) || (this.defaultValues.isEmpty())) return parameters;
    Map<String, Object> addMe = new HashMap();
    Set<String> keys = this.defaultValues.keySet();
    for (String key : keys) {
      Object val = parameters.get(key);
      if (val == null) {
        addMe.put(key, this.defaultValues.get(key));
      }
    }
    parameters.putAll(addMe);
    Log.getInstance().log(Level.FINER, "AFTER adding defaults: {0}", getClass(), parameters);
    
    return parameters;
  }
  
  protected Map<String, Object> formatCurrencyTypes(Map<String, Object> parameters) {
    Log.getInstance().log(Level.FINER, "@formatPrice, params: {0}", getClass(), parameters);
    

    Object price = parameters.get("price");
    
    if (price == null) { return parameters;
    }
    Object discount = parameters.get("discount");
    
    if (discount != null)
    {
      discount = preparePriceString(discount.toString());
      parameters.put("discount", discount);
    }
    

    price = preparePriceString(price.toString());
    parameters.put("price", price);
    
    Object oval = parameters.get("currency");
    
    if (oval == null) { return parameters;
    }
    String currCode = oval.toString().trim().toUpperCase();
    
    parameters.put("currency", currCode);
    

    final String DEFAULT_CURRENCY = "NGN";
    
    if (currCode.equals("NGN")) {
      return parameters;
    }
    
    Log.getInstance().log(Level.FINER, "From: {0}, To: {1}", getClass(), currCode, "NGN");
    
    final FxRateService fxSvc = FxRateService.getAvailable().stream().findFirst().orElse(null);

    if(fxSvc == null) {
      return parameters;
    }
   
    final FxRate fxRate = fxSvc.getRate(currCode, DEFAULT_CURRENCY);
    
    if (fxRate == FxRate.NONE) {
      return parameters;
    }
    
    final float rate = fxRate.getRate();
    try
    {
      double convertedPrice = Double.parseDouble(price.toString()) * rate;
      parameters.put("price", Double.valueOf(convertedPrice));
      
      if (discount != null) {
        double convertedDiscount = Double.parseDouble(discount.toString()) * rate;
        parameters.put("discount", Double.valueOf(convertedDiscount));
      }
      
      parameters.put("currency", "NGN");
      
      Log.getInstance().log(Level.FINER, "{0} {1} updated to {2} {3}", getClass(), currCode, price, "NGN", Double.valueOf(convertedPrice));
    }
    catch (NumberFormatException e) {
      Log.getInstance().logSimple(Level.WARNING, getClass(), e);
    }
    
    return parameters;
  }
  
  protected Map formatLinks(Map parameters)
  {
    String baseURL = this.context.getConfig().getString(new Object[] { Config.Site.url, "value" });
    if (baseURL == null) {
      String s = this.context.getConfig().getString(new Object[] { Config.Site.url, Config.Site.start });
      if (s != null) {
        baseURL = com.bc.util.Util.getBaseURL(s);
      } else {
        baseURL = s;
      }
    }
    
    HashMap update = new HashMap();
    
    Set<String> keys = parameters.keySet();
    
    for (String key : keys)
    {
      Object val = parameters.get(key);
      
      if (val != null)
      {
        String sval = val.toString();
        
        if (isHtmlLink(key, sval)) {
          val = formatHtmlLink(val.toString(), baseURL);
        } else if (isLink(key, sval)) {
          val = formatDirectLink(val.toString(), baseURL);
        }
        
        update.put(key, val);
      } }
    parameters.putAll(update);
    return update;
  }
  
  protected boolean isHtmlLink(String col, String val)
  {
    val = val.trim().toLowerCase();
    if ((val.startsWith("<a ")) && 
      (val.contains(" href="))) {
      return true;
    }
    
    return false;
  }
  
  protected boolean isLink(String col, String val) {
    col = col.trim();
    return (col.startsWith("image")) || (col.equals("howToApply"));
  }
  
  protected String formatHtmlLink(String input, String baseUrl)
  {
    NodeList list = null;
    try {
      Parser p = new Parser();
      p.setResource(input);
      list = p.parse(null);
      for (int i = 0; i < list.size(); i++) {
        Node node = list.elementAt(i);
        if ((node instanceof LinkTag)) {
          LinkTag tag = (LinkTag)node;
          String link = tag.getLink();
          
          if (!link.toLowerCase().startsWith("file://"))
          {
            link = formatDirectLink(link, baseUrl);
            
            tag.setLink(link);
            
            tag.setAttribute("target", "_blank");
          }
        }
      }
    } catch (ParserException e) { Log.getInstance().log(Level.WARNING, null, getClass(), e);
    }
    return list == null ? input : list.toHtml(false);
  }
  
  protected String formatDirectLink(String link, String baseUrl)
  {
    if (link.toLowerCase().startsWith("http://")) {
      return link;
    }
    
    return UrlUtil.createURL(baseUrl, link);
  }
  
  protected String getMyHowToApply(Map parameters)
  {
    Object val = getUrl(parameters);
    Log.getInstance().log(Level.FINER, "URL: {0}", getClass(), val);
    
    if (val == null) { return getJobRequestHowToApply(parameters);
    }
    String validUrl = formatUrl(val.toString());
    
    if (validUrl != null)
    {
      StringBuilder builder = new StringBuilder("<a href=\"");
      builder.append(validUrl).append("\" target=\"_blank\">");
      builder.append("CLICK HERE TO APPLY").append("</a>");
      return builder.toString();
    }
    return getJobRequestHowToApply(parameters);
  }
  
  protected String getUrl(Map parameters)
  {
    Object val = parameters.get("extraDetails");
    Log.getInstance().log(Level.FINER, "Extra details: {0}", getClass(), val);
    if (val == null) {
      return null;
    }
    Map extraDetails = Util.getParameters(val.toString(), "&");
    val = extraDetails.get("url");
    Log.getInstance().log(Level.FINER, "URL: {0}", getClass(), val);
    return val == null ? null : val.toString();
  }
  



  private String formatUrl(String url)
  {
    if ((url.toLowerCase().startsWith("http://")) || (url.toLowerCase().startsWith("file://"))) {
      return url;
    }
    

    String fmtUrl = UrlUtil.prepareLink(url.toLowerCase());
    
    List<String> urls = UrlUtil.getBaseURLs(this.context.getConfig().getString(new Object[] { Config.Site.url, "value" }));
    
    for (String base : urls)
    {
      String complete = base + fmtUrl;
      
      try
      {
        boolean isValid = isValidUrl(complete);
        
        if (isValid) return fmtUrl;
      }
      catch (IOException e) {
        Log.getInstance().log(Level.WARNING, "Failed to confirm validity of url: " + fmtUrl, getClass(), e);
      }
    }
    


    return null;
  }
  
  private boolean isValidUrl(String urlString) throws IOException {
    try {
      URL url = new URL(urlString);
      return ConnectionManager.isValidUrl(url);
    } catch (MalformedURLException e) {
      Log.getInstance().logSimple(Level.WARNING, getClass(), e); }
    return false;
  }
  
  protected String getJobRequestHowToApply(Map parameters)
  {
    if (this.jobRequestFields == null) {
      return null;
    }
    StringBuilder url = new StringBuilder("http://www.looseboxes.com/apply.jsp?pt=jobs");
    Iterator iter = parameters.keySet().iterator();
    while (iter.hasNext()) {
      Object key = iter.next();
      if (this.jobRequestFields.contains(key.toString()))
        try {
          url.append('&').append(key).append('=').append(URLEncoder.encode(parameters.get(key).toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          Log.getInstance().log(Level.WARNING, null, getClass(), e);
        } catch (RuntimeException e) {
          Log.getInstance().log(Level.WARNING, null, getClass(), e);
        }
    }
    url.append("&itemtype=2");
    StringBuilder updatedHowToApply = new StringBuilder();
    updatedHowToApply.append("<a href=\"").append(url).append("\">").append("CLICK HERE TO APPLY").append("</a>");
    return updatedHowToApply.toString();
  }
  
  protected Map<String, Object> formatDateTypes(Map<String, Object> parameters)
  {
    Iterator<String> iter = parameters.keySet().iterator();
    
    HashMap<String, Object> dateTypes = new HashMap()
    {
      public Object put(String key, Object value) {
        if ((key == null) || (value == null)) throw new NullPointerException();
        return super.put(key, value);
      }
      
    };
    HashSet<String> failed = new HashSet();
    
    while (iter.hasNext())
    {
      String key = (String)iter.next();
      
      if (isDateType(key, null)) {
        try
        {
          String temp = parameters.get(key).toString();
          String dateStr = prepareDateString(temp);
          
          Log.getInstance().log(Level.FINER, "Before: {0}, After: {1}", getClass(), temp, dateStr);
System.out.println(this.getClass().getName()+" = o = o = o = o = o = o = o = o = o: "+key+", before: "+temp+", after: "+dateStr);          

          java.util.Date date = this.inputDateformat.parse(dateStr);
          
          String fmtVal = this.outputDateformat.format(date);
          
          if ((isDatetimeType(key)) && 
            (!fmtVal.contains(":"))) {
            fmtVal = fmtVal + " 00:00:00.0";
          }
          
          dateTypes.put(key, fmtVal);
          
          Log.getInstance().log(Level.FINER, "{0}:: input String: {1}, parsed to Date: {2}, formatted to String: {3}", getClass(), key, dateStr, date, fmtVal);

        }
        catch (ParseException e)
        {

          failed.add(key);
          
          if (Log.getInstance().isLoggable(Level.FINE, getClass())) {
            Log.getInstance().log(Level.WARNING, null, getClass(), e);
          } else {
            Log.getInstance().log(Level.WARNING, e.toString(), getClass());
          }
        }
      }
    }
    
    parameters.putAll(dateTypes);
    
    parameters.keySet().removeAll(failed);
    
    return parameters;
  }
  
  public int formatDateinAndExpiryDate(Map parameters)
  {
    Object expiryObj = parameters.get("expiryDate");
    
    Object dateinObj = parameters.get("datein");
    
    java.util.Date datein = null;
    
    if (dateinObj == null) {
      datein = new java.sql.Date(System.currentTimeMillis());
      dateinObj = this.outputDateformat.format(datein);
      parameters.put("datein", dateinObj);
    } else {
      try {
        datein = this.outputDateformat.parse(dateinObj.toString());
      } catch (ParseException e) {
        datein = new java.util.Date();
        Log.getInstance().log(Level.WARNING, null, getClass(), e);
      }
    }
    
    if (expiryObj == null)
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(datein);
      cal.add(6, 90);
      
      expiryObj = this.outputDateformat.format(cal.getTime());
      
      parameters.put("expiryDate", expiryObj);
    }
    Log.getInstance().log(Level.FINER, "Datein: {0}, Expiry date: {1}", getClass(), dateinObj, expiryObj);
    

    return getStatus(datein);
  }
  
  protected String preparePriceString(String str) {
    return removeNonePriceChars(str.trim());
  }
  



  protected String prepareDateString(String str)
  {
    return condenseSpaces(removeNoneDateChars(str));
  }
  


  private String removeNoneDateChars(String str)
  {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      if ((Character.isLetterOrDigit(ch)) || (ch == ' ') || (ch == '-') || (ch == ',') || (ch == '/') || (ch == ':') || (ch == '.'))
      {
        builder.append(ch);
      }
    }
    return builder.toString().trim();
  }
  



  private String removeNonePriceChars(String str)
  {
    int digits = 0;
    int afterDigits = 0;
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      if (Character.isDigit(ch)) {
        digits++;
        afterDigits = 0;
        builder.append(ch);
      }
      if (digits != 0)
      {

        if ((ch == '.') || (ch == ',') || (ch == ':')) {
          afterDigits++;
          builder.append(ch);
        }
      }
    }
    builder.setLength(builder.length() - afterDigits);
    
    String output = builder.toString().trim();
    
    Log.getInstance().log(Level.FINEST, "Price: {0}. After Format: {1}", getClass(), str, output);
    

    return output;
  }
  



  private String condenseSpaces(String str)
  {
    StringBuilder builder = new StringBuilder();
    boolean addWhiteSpace = true;
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      if (Character.isWhitespace(ch)) {
        if (addWhiteSpace) {
          builder.append(" ");
          addWhiteSpace = false;
        }
      } else {
        builder.append(ch);
        addWhiteSpace = true;
      }
    }
    return builder.toString().trim();
  }
  
  protected boolean isDateType(String key, Object val) {
    return key.toLowerCase().contains("date");
  }
  
  protected boolean isDatetimeType(String col) {
    return col.equals("datein");
  }
  
  private int getStatus(java.util.Date date) {
    Calendar tgt = Calendar.getInstance();
    tgt.add(6, 65176);
    if (date.before(tgt.getTime())) {
      return 2;
    }
    tgt.add(6, 240);
    if (date.before(tgt.getTime())) {
      return 4;
    }
    return 1;
  }
  

  public DateFormat getInputDateformat()
  {
    return this.inputDateformat;
  }
  
  public void setInputDateformat(DateFormat inputDateformat) {
    this.inputDateformat = inputDateformat;
  }
  
  public DateFormat getOutputDateformat() {
    return this.outputDateformat;
  }
  
  public void setOutputDateformat(DateFormat outputDateformat) {
    this.outputDateformat = outputDateformat;
  }
  
  public List<String> getJobRequestFields() {
    return this.jobRequestFields;
  }
  
  public void setJobRequestFields(ArrayList<String> jobRequestFields) {
    this.jobRequestFields = jobRequestFields;
  }
  
  public Map getDefaultValues() {
    return this.defaultValues;
  }
  
  public void setDefaultValues(Map defaultValues) {
    this.defaultValues = defaultValues;
  }
}
