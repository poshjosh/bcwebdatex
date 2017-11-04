package com.bc.webdatex.filter;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;
import org.htmlparser.tags.ImageTag;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 3, 2016 10:08:59 AM
 */
public class ImageNodeFilter implements NodeFilter {
    
  private final Filter<String> imageSrcFilter;

  public ImageNodeFilter(String baseUrl) {
      this(new ImageSrcFilter(baseUrl));
  }
  
  public ImageNodeFilter(String baseUrl, String regexToAccept, String regexToReject) {
      this(new ImageSrcFilter(baseUrl, regexToAccept, regexToReject));
  }
  
  public ImageNodeFilter(Filter<String> imageSrcFilter) {
      this.imageSrcFilter = imageSrcFilter;
  }

  @Override
  public boolean accept(Node node) {
    if ((node instanceof Tag)) {
      Tag tag = (Tag)node;
      if (((tag instanceof ImageTag)) || ("IMG".equalsIgnoreCase(tag.getTagName()))) {
        Attribute attr = tag.getAttribute("src");
        if (attr == null) {
          return false;
        }
        String value = attr.getValue();
        if (value == null) {
          return false;
        }
        if(imageSrcFilter == null) {
            return true;
        }else{
            boolean accepted = imageSrcFilter.test(value);
            return accepted;
        }
      }
      return false;
    }

    return false;
  }

  public final Filter<String> getImageSrcFilter() {
    return imageSrcFilter;
  }
}
