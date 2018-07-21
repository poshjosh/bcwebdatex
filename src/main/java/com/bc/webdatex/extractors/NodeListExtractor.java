package com.bc.webdatex.extractors;

import com.bc.task.StoppableTask;
import java.util.Map;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

public interface NodeListExtractor 
        extends DataExtractor<NodeList>, StoppableTask<Map>, NodeVisitor {
    
    @Override
    Map extractData(NodeList paramNodeList) throws ParserException;
}
