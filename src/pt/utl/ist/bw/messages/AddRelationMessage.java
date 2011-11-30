package pt.utl.ist.bw.messages;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import pt.utl.ist.bw.utils.StringUtils;

public class AddRelationMessage {
	
	private String cardinalityOne;
	private String cardinalityTwo;
	
	private String entityOne;
	private String entityTwo;
	
	private boolean isOneKeyEntity;
	private boolean isTwoKeyEntity;
	
	public String getEntityOne() { return this.entityOne; }
	public String getEntityTwo() { return this.entityTwo; }
	
	public boolean isOneKeyEntity() { return this.isOneKeyEntity; }
	public boolean isTwoKeyEntity() { return this.isTwoKeyEntity; }
	
	public String getCardinalityOne() { return this.cardinalityOne; }
	public String getCardinalityTwo() { return this.cardinalityTwo; }
	
	public void isOneKeyEntity(boolean oneKey) { this.isOneKeyEntity = oneKey; }
	public void isTwoKeyEntity(boolean twoKey) { this.isTwoKeyEntity = twoKey; }
	public void setEntityOne(String entityOne) { this.entityOne = entityOne; }
	public void setEntityTwo(String entityTwo) { this.entityTwo = entityTwo; }
	public void setCardinalityOne(String card) { this.cardinalityOne = card; }
	public void setCardinalityTwo(String card) { this.cardinalityTwo = card; }
	
	public String toXMLString() {
		Element relation = new Element("relation");
		relation.addContent(new Element("cardinalityOne").setText(this.cardinalityOne));
		relation.addContent(new Element("cardinalityTwo").setText(this.cardinalityTwo));
		relation.addContent(new Element("entityOne").setText(this.entityOne));
		relation.addContent(new Element("entityTwo").setText(this.entityTwo));
		relation.addContent(new Element("isOneKeyEntity").setText(Boolean.toString(this.isOneKeyEntity)));
		relation.addContent(new Element("isTwoKeyEntity").setText(Boolean.toString(this.isTwoKeyEntity)));
		
		return new XMLOutputter().outputString(relation);
	}
	
	public AddRelationMessage fromXMLString(String xml) {
		Document doc = StringUtils.stringToDoc(xml);
		Element root = doc.getRootElement();
		
		if(root.getChild("cardinalityOne") == null ||
				root.getChild("cardinalityTwo") == null ||
				root.getChild("entityOne") == null ||
				root.getChild("entityTwo") == null ||
				root.getChild("isOneKeyEntity") == null ||
				root.getChild("isTwoKeyEntity") == null) {
			return null;
		}
		
		this.cardinalityOne = root.getChild("cardinalityOne").getText();
		this.cardinalityTwo = root.getChild("cardinalityTwo").getText();
		this.entityOne = root.getChild("entityOne").getText();
		this.entityTwo = root.getChild("entityTwo").getText();
		this.isOneKeyEntity = Boolean.parseBoolean(root.getChild("isOneKeyEntity").getText());
		this.isTwoKeyEntity = Boolean.parseBoolean(root.getChild("isTwoKeyEntity").getText());
		
		return this;
	}
}
