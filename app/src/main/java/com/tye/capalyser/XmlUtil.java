package com.tye.capalyser;


import java.util.*;
import org.w3c.dom.*;

/*
 * Wie kann eine Nodeliste iteriert werden ("ForEach", statt "For i=1 bis...")
 *
 * 		https://stackoverflow.com/questions/19589231/can-i-iterate-through-a-nodelist-using-for-each-in-java
 *
 * Allerdings:
 * 		Diesen Teil hab ich nicht ganz geschafft, muss also aufrufen XMlUti.asList()
 * 			Once you have added this utility class to your project
 * 			and added a __static import__(!) for the XmlUtil.asList method to your source code
 * 			you can use it like this:	for (Node n:aslList(myNodeList)) { }
 *
 */

public final class XmlUtil {
  private XmlUtil(){}

  public static List<Node> asList(NodeList n) {
    return n.getLength()==0		? 	Collections.<Node>emptyList()	: 	new NodeListWrapper(n);
  }
  static final class NodeListWrapper extends AbstractList<Node>
  implements RandomAccess {
    private final NodeList list;
    NodeListWrapper (NodeList l) { list=l; }
    public Node get	(int index) { return list.item(index); }
    public int size () 			{ return list.getLength(); }
  }

  public static String getFirstLevelTextContent(Node node) {
	  String textContent = "";
	  for (Node child:XmlUtil.asList(node.getChildNodes())) {
		  if (child.getNodeType() == Node.TEXT_NODE)
			  textContent += child.getTextContent();
	  }
	  return textContent.trim();
  }

}