package comp557.a1;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;

import javax.vecmath.Tuple2d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A factory class to generate a DAG from XML definition. 
 */
public class Parser {

	public static DAGNode load( String filename ) {
		try {
			InputStream inputStream = new FileInputStream(new File(filename));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(inputStream);
			return createScene( null, document.getDocumentElement() ); // we don't check the name of the document elemnet
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load simulation input file.", e);
		}
	}
	
	/**
	 * Load a DAG subtree from a XML node.
	 * Returns the root on the call where the parent is null, but otherwise
	 * all children are added as they are created and all other deeper recursive
	 * calls will return null.
	 */
	public static DAGNode createScene( DAGNode parent, Node dataNode ) {
        NodeList nodeList = dataNode.getChildNodes();
        for ( int i = 0; i < nodeList.getLength(); i++ ) {
            Node n = nodeList.item(i);
            // skip all text, just process the ELEMENT_NODEs
            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
            String nodeName = n.getNodeName();
            DAGNode dagNode = null;
            if ( nodeName.equalsIgnoreCase( "node" ) ) {
            	dagNode = Parser.createJoint( n );
            } else if ( nodeName.equalsIgnoreCase( "geom" ) ) {        		
        		dagNode = Parser.createGeom( n ) ;            
            }
            // recurse to load any children of this node
            createScene( dagNode, n );
            if ( parent == null ) {
            	// if no parent, we can only have one root... ignore other nodes at root level
            	return dagNode;
            } else {
            	parent.add( dagNode );
            }
        }
        return null;
	}
	
	/**
	 * Create a joint
	 * 
	 * TODO: Objective 5: Adapt commented code in createJoint() to create your joint nodes when loading from xml
	 */
	public static DAGNode createJoint( Node dataNode ) {
		String type = dataNode.getAttributes().getNamedItem("type").getNodeValue();
		String name = dataNode.getAttributes().getNamedItem("name").getNodeValue();
		JointNode joint;
		if ( type.equals("freejoint") ) {
			joint = new FreeJoint(name,getTuple3dAttr(dataNode,"center"), getTuple3dAttr(dataNode,"rotation"));
			return joint;
		} else if ( type.equals("ballxyz") ) {
			joint = new BallJoint(name,getTuple3dAttr(dataNode,"center"), getTuple3dAttr(dataNode,"rotation"), getTuple2dAttr(dataNode,"xlimits"), getTuple2dAttr(dataNode,"ylimits"), getTuple2dAttr(dataNode,"zlimits"));
			return joint;
			
		} else if ( type.equals("hinge") ) {
			joint = new HingeJoint(name, getTuple3dAttr(dataNode,"center"), getTuple3dAttr(dataNode,"axis"), 0, getTuple2dAttr(dataNode,"limits"));
			return joint;
		}
		throw new RuntimeException(String.format("Joint of name : %s is not implemented!", type));
	}

	/**
	 * Creates a geometry DAG node 
	 */
	public static DAGNode createGeom( Node dataNode ) {
		String type = dataNode.getAttributes().getNamedItem("type").getNodeValue();
		String name = dataNode.getAttributes().getNamedItem("name").getNodeValue();
		GeomNode geom;
		if ( type.equals("box" ) ) {
			geom = new CubeNode(name);
			parseGeomAttributes( dataNode,  geom);
			return geom;
		} else if ( type.equals( "sphere" )) {
			geom = new SphereNode(name);
			parseGeomAttributes( dataNode,  geom);
			return geom;
		} else if (type.equals("cone")) {
			geom = new ConeNode(name);
			parseGeomAttributes( dataNode,  geom);
			return geom;
		}
		throw new RuntimeException(String.format("Object of name : %s is not implemented!", type));
	}

	private static void parseGeomAttributes(Node dataNode, GeomNode geom) {
		Tuple3d t;
		if ( (t=getTuple3dAttr(dataNode,"center")) != null ) geom.setCenter( t );
		if ( (t=getTuple3dAttr(dataNode,"rotation")) != null ) geom.setRotation( t );
		if ( (t=getTuple3dAttr(dataNode,"scale")) != null ) geom.setScale( t );
		if ( (t=getTuple3dAttr(dataNode,"color")) != null ) geom.setColor( t );
	}
	
	/**
	 * Loads tuple3d attributes of the given name from the given node.
	 * @param dataNode
	 * @param attrName
	 * @return null if attribute not present
	 */
	public static Tuple3d getTuple3dAttr( Node dataNode, String attrName ) {
		Node attr = dataNode.getAttributes().getNamedItem( attrName);
		Tuple3d tuple = null;
		if ( attr != null ) {
			Scanner s = new Scanner( attr.getNodeValue() );
			tuple = new Vector3d( s.nextDouble(), s.nextDouble(), s.nextDouble() );			
			s.close();
		}
		return tuple;
	}

	/**
	 * Loads tuple2d attributes of the given name from the given node.
	 * @param dataNode
	 * @param attrName
	 * @return null if attribute not present
	 */
	public static Tuple2d getTuple2dAttr( Node dataNode, String attrName ) {
		Node attr = dataNode.getAttributes().getNamedItem( attrName);
		Tuple2d tuple = null;
		if ( attr != null ) {
			Scanner s = new Scanner( attr.getNodeValue() );
			tuple = new Vector2d( s.nextDouble(), s.nextDouble());			
			s.close();
		}
		return tuple;
	}

}