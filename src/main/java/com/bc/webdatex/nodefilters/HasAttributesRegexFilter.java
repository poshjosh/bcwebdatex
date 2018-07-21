package com.bc.webdatex.nodefilters;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.filters.HasAttributeFilter;

public class HasAttributesRegexFilter
  extends HasAttributeFilter
{
    private transient static final Logger LOG = Logger.getLogger(HasAttributesRegexFilter.class.getName());
  private Pattern pattern;
  
  public HasAttributesRegexFilter(String attribute, String regex)
  {
    super(attribute, regex);
    if (regex != null) {
      this.pattern = Pattern.compile(regex, 2);
    }
  }

  @Override
  public boolean accept(Node node)
  {
    if (!(node instanceof Tag)) { return false;
    }
    Attribute attribute = ((Tag)node).getAttribute(this.mAttribute);
    
    boolean accept = false;
    
    if ((attribute != null) && (this.pattern != null)) {
      accept = this.pattern.matcher(attribute.getValue()).find();
    }
    

    if(LOG.isLoggable(Level.FINEST)){
      LOG.log(Level.FINEST, "{0}={1}, accept: {2}, Node: {3}",  
              new Object[]{this.mAttribute,  this.pattern.pattern(),  Boolean.valueOf(accept),  node});
    }
    
    return accept;
  }
}
