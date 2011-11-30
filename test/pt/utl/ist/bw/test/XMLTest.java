package pt.utl.ist.bw.test;


import java.io.File;
import java.util.List;

import javax.validation.constraints.AssertTrue;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.bw.utils.SpecUtils;
import pt.utl.ist.bw.utils.StringUtils;

public class XMLTest {
		
	private Element root = null;

	@Before
	public void setUp() throws Exception {
		File file = new File("/tmp/MedicalEpisode.xml");
		String fileStr = StringUtils.fileToString(file);
		Document doc;
		doc = StringUtils.stringToDoc(fileStr);
		
		Assert.assertTrue(doc != null);
		this.root = doc.getRootElement();
	}
	
	@Test
	public void testRootElement() {
		System.out.println("Root name: " + root.getName());
		
		Assert.assertTrue(true);
	}
	
	@Test
	public void testNamespace() {
		System.out.println("Root namespace: " + root.getNamespace());
		
		Assert.assertTrue(true);
	}
	
	@Test
	public void testRootChildren() {
		@SuppressWarnings("unchecked")
		List<Element> children = (List<Element>) root.getChildren();
		
		for (Element element : children) {
			System.out.println("Child of root: " + element.getName());
		}
		
		//Assert.assertTrue(root.getChild("goalSpec", root.getNamespace()) != null);
	}
	
	@Test
	public void testGetGoalSpecID() {
		String id = root.getChild("goalSpec", root.getNamespace()).getAttributeValue("id");
		Assert.assertTrue(id != null);
		Assert.assertTrue(id.equals("Medical_Episode_GSpec_0"));
	}
	
	@Test
	public void testGetConditionsSpecID() {
		String id = root.getChild("conditionsSpec", root.getNamespace()).getAttributeValue("id");
		Assert.assertTrue(id != null);
		Assert.assertTrue(id.equals("Medical_Episode_CondSpec_0"));
	}
	
	@Test
	public void testGetChildText() {
		Namespace bwNs = root.getNamespace();
		Element goalTree = root.getChild("goalSpec", root.getNamespace()).getChild("goalTree", root.getNamespace());
		
		for(int i=0; i < goalTree.getChildren().size(); i++) {
			Element goal = (Element) goalTree.getChildren().get(i);
			if(goal.getName().equals("goal")) {
				String goalDefinition = goal.getChildText("definition", bwNs);
				Assert.assertTrue(goalDefinition.equals("bw:definition"));
			}
		}
	}
	
	@Test
	public void testToString() {
		System.out.println("== root to string ==");
		System.out.println(root.toString());
		
		Assert.assertTrue(true);
	}
}