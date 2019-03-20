package comp557.a1;

import javax.swing.JTextField;
import javax.vecmath.Vector3d;

import mintools.parameters.BooleanParameter;

public class CharacterCreator {

	static public String name = "Elias Al Homsi 260797449";
	
	// TODO: Objective 6: change default of load from file to true once you start working with xml
	static BooleanParameter loadFromFile = new BooleanParameter( "Load from file (otherwise by procedure)", true );
	static JTextField baseFileName = new JTextField("src/comp557/a1/a1data/character");
	static { baseFileName.setName("what is this?"); }
	
	/**
	 * Creates a character, either procedurally, or by loading from an xml file
	 * @return root node
	 */
	static public DAGNode create() {
		
		if ( loadFromFile.getValue() ) {
			// TODO: Objectives 6: create your character in the character.xml file 
			return Parser.load( baseFileName.getText() + ".xml");
		} else {
			FreeJoint fj = new FreeJoint("elias", new Vector3d(1.0, 1.0, 1.0), new Vector3d(1.0, 1.0, 1.0));
			// TODO: Objective 1,2,3,4: test DAG nodes by creating a small DAG in the CharacterCreator.create() method 
			// Use this for testing, but ultimately it will be more interesting
			// to create your character with an xml description (see example).
			
			// Here we just return null, which will not be very interesting, so write
			// some code to create a test or partial charcter and return the root node.

			return fj;
		}
	}
}
