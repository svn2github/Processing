package writers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;

public class BaseWriter {
	// Some utilities
	static ArrayList<String> writtenElements;
	public BaseWriter() {

	}
	
	protected static boolean needsWriting(ProgramElementDoc doc){
		if( Shared.i().isWebref(doc) )
		{
			return hasXMLDocument( doc );
		}
		return false;
	}
	
	protected static BufferedWriter makeWriter(String anchor) throws IOException
	{
		return makeWriter(anchor, false);
	}

	protected static BufferedWriter makeWriter(String anchor, Boolean isLocal) throws IOException {
		FileWriter fw;
		if (!isLocal) {
			fw = new FileWriter(Shared.i().getOutputDirectory() + "/" + anchor );
		} else 
		{
			fw = new FileWriter(Shared.i().getLocalOutputDirectory() + anchor );
		}
		return new BufferedWriter(fw);
	}
	
	protected static String getAnchor(ProgramElementDoc doc)
	{
		String ret = getAnchorFromName(getName(doc));
		
		if(doc.containingClass() != null && !Shared.i().isRootLevel(doc.containingClass())){
			ret = doc.containingClass().name() + "_" + ret;
		}
		
		if(!Shared.i().isCore(doc)){
			//add package name to anchor
			String[] parts = doc.containingPackage().name().split("\\."); 
			String pkg = parts[parts.length-1] + "/";
			ret = "libraries/" + pkg + ret;
		}		
		
		return ret;
	}
	
	protected static String getLocalAnchor(ProgramElementDoc doc)
	{
		String ret = getAnchorFromName(getName(doc));
		if(doc.containingClass() != null){
			ret = doc.containingClass().name() + "_" + ret;
		}
		
		return ret;
	}
	
	protected static String getReturnTypes(MethodDoc doc){
		String ret = importedName(doc.returnType().toString());
		if(doc.containingClass() != null){
			for(MethodDoc m : doc.containingClass().methods()){
				if(m.name().equals(doc.name()) && m.returnType() != doc.returnType()){
					String name = importedName(m.returnType().toString());
					if( ! ret.contains( name ))
					{						
						ret += " or " + name;
					}
				}
			}
		} 
		return ret;
	}
	
	protected static String getName(Doc doc) { // handle
		String ret = doc.name();
		if(doc instanceof MethodDoc)
		{
			ret = ret.concat("()");
		} else if (doc.isField()){
			// add [] if needed
			FieldDoc d = (FieldDoc) doc;
			ret = ret.concat(d.type().dimension());
		}
		return ret;
	}

	protected static String getAnchorFromName(String name){
		if( name.endsWith("()") ){
			//functions look like functionName_.html
			name = name.replace("()", "_");
		} else if( name.contains("(") && name.contains(")") ){
			//get the name in parentheses
			int start = name.indexOf("(") + 1;
			int end = name.indexOf(")");
			name = name.substring(start, end);
		} else if( name.endsWith("[]")){
			//strip off the array indicators for the name
			name = name.replaceAll("\\[\\]", "");
		}
		return name.replace(" ", "").concat(".html");
	}
	
	//

	static protected String getBasicDescriptionFromSource(ProgramElementDoc doc) {
		return getBasicDescriptionFromSource(longestText(doc));
	}
	
	static protected String getBriefDescriptionFromSource(ProgramElementDoc doc) {
		Tag[] sta = doc.tags("brief");
		if(sta.length > 0){
			return sta[0].text();
		}
		return getBasicDescriptionFromSource(doc);
	}
	
	static protected String longestText(ProgramElementDoc doc){
		if(Shared.i().isWebref(doc)){
			//override longest rule if the element has an @webref tag
			return doc.commentText();
		}
		
		String s = doc.commentText();
		if( doc.isMethod() ){
			for(ProgramElementDoc d : doc.containingClass().methods()){
				if(d.name().equals(doc.name() ) ){
					if(d.commentText().length() > s.length()){
						s = d.commentText();
					}
				}
			}			
		} else if(doc.isField()){			
			for(ProgramElementDoc d : doc.containingClass().fields()){
				if(d.name().equals(doc.name() ) ){
					if(d.commentText().length() > s.length()){
						s = d.commentText();
					}
				}
			}
		}
		return s;
	}
	
	static protected String getBasicDescriptionFromSource(String s){
		String[] sa = s.split("(?i)(<h\\d>Advanced:?</h\\d>)|(=advanced)");
		if (sa.length != 0)
			s = sa[0];
		return s;
	}
	
	static protected String getAdvancedDescriptionFromSource(ProgramElementDoc doc) {
		return getAdvancedDescriptionFromString(longestText(doc));
	}
	static private String getAdvancedDescriptionFromString(String s) {
		String[] sa = s.split("(?i)(<h\\d>Advanced:?</h\\d>)|(=advanced)");
		if (sa.length > 1)
			s = sa[1];
		return s;
	}
	
	
	
	//
	
	protected static String getXMLPath(ProgramElementDoc doc) {
		String path = Shared.i().getXMLDirectory();
		String name = doc.name();
		String suffix = ".xml";
		if(doc.containingClass() != null){
			if(Shared.i().isRootLevel(doc.containingClass())){
				//inside PApplet or other root-level class
				if(doc instanceof FieldDoc){
					//if there is a method of the same name, append _var
					for( Doc d : doc.containingClass().methods()){
						if(d.name().equals(doc.name())){
							suffix = "_var" + suffix;
							break;	//don't append multiple times
						}
					}
				}
			} else {
				name = doc.containingClass().name() + "_" + name;				
			}
		}
		
		if( !Shared.i().isCore(doc)){
			//if documentation is for a library element
			String[] pkg = doc.containingPackage().name().split("\\.");
			path = path + "LIB_" + pkg[pkg.length-1] + "/";
		}
		
		return path + name + suffix;
	}
	
	static protected String getExamples(ProgramElementDoc doc) throws IOException{
		return getExamples(getXMLPath(doc));
	}
	
	static private Document getXMLDocument(String path) throws IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.out.println( "Failed to load XML: " + e.getMessage());
		}
		
		if( builder != null )
		{
			try {
				doc = builder.parse(path);
			} catch (SAXException e) {
				System.out.println( "Failed to parse XML: " + e.getMessage() );
			} catch (IOException e) {
				System.out.println( "Failed to parse XML: " + e.getMessage() );
			}
		}
		
		
		return doc;
	}
	
	private static boolean hasXMLDocument( ProgramElementDoc doc )
	{
		String path = getXMLPath( doc );
		File f = new File( path );
		if( f.exists() )
		{
			return true;
		}
		return false;
	}
	
	static protected String getExamples(String path) throws IOException {				
		Document doc = getXMLDocument(path);
		if( doc != null )
			return getExamples(doc);
		else
		{
			System.out.println("Unable to get examples from " + path + "; returning an empty string.");
			return "";
		}
	}
	
	protected static String getExamples(Document doc) throws IOException{
		//Parse the examples from an XML document
		TemplateWriter templateWriter = new TemplateWriter();
		ArrayList<HashMap<String, String>> exampleList = new ArrayList<HashMap<String, String>>();
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		try {
			XPathExpression expr = xpath.compile("//example");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList examples = (NodeList) result;

			for (int i = 0; i < examples.getLength(); i++) {
				HashMap<String, String> example = new HashMap<String, String>();

				expr = xpath.compile("image");
				String img = (String) expr.evaluate(examples.item(i),
						XPathConstants.STRING);
				expr = xpath.compile("code");
				String code = (String) expr.evaluate(examples.item(i),
						XPathConstants.STRING);
				
				example.put("image", Shared.i().getImageDirectory()
						+ img);
				if(img.equals(""))
				{
					example.put("image", img);
				}
				example.put("code", code);

				exampleList.add(example);
			}

		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		String exampleInner = templateWriter.writeLoop("/Example.partial.html", exampleList);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("examples", exampleInner);
		return templateWriter.writePartial("Examples.partial.html", map);
	}
	
	protected static String getXMLDescription(ProgramElementDoc doc) throws IOException {
		Document xmlDoc = getXMLDocument(getXMLPath(doc));
		if( xmlDoc != null )
			return getXMLDescription(xmlDoc);
		else {
			System.out.println("Unable to get description from " + getXMLPath(doc) + "; returning an empty string.");
			return "";
		}
	}
	
	protected static String getXMLDescription(Document doc) {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		
		String desc = "";
		
		for( String component : Shared.i().getDescriptionSets() )
		{
			try{
				XPathExpression expr = xpath.compile(component);
				String result = (String) expr.evaluate(doc, XPathConstants.STRING);
				desc += result;
			} catch ( XPathExpressionException e){
				System.out.println("Error getting description from xml with expression: //" + component);
				e.printStackTrace();
			}
		}
		
		return desc;
	}

	protected static String getTimestamp() {
		Calendar now = Calendar.getInstance();
		Locale us = new Locale("en");
		
		return now.getDisplayName(Calendar.MONTH, Calendar.LONG, us)
				+ " "
				+ now.get(Calendar.DAY_OF_MONTH)
				+ ", "
				+ now.get(Calendar.YEAR)
				+ " "
				+ FileUtils.nf(now.get(Calendar.HOUR), 2)
				+ ":"
				+ FileUtils.nf(now.get(Calendar.MINUTE), 2)
				+ ":"
				+ FileUtils.nf(now.get(Calendar.SECOND), 2)
				+ now.getDisplayName(Calendar.AM_PM, Calendar.SHORT, us)
						.toLowerCase()
				+ " "
				+ TimeZone.getDefault().getDisplayName(
						TimeZone.getDefault().inDaylightTime(now.getTime()),
						TimeZone.SHORT, us);
	}
	
	/*
	 * Get all the syntax possibilities for a method
	 */
	protected static ArrayList<HashMap<String, String>> getSyntax(MethodDoc doc, String instanceName) throws IOException
	{
		TemplateWriter templateWriter = new TemplateWriter();
		ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String,String>>();
		
		for( MethodDoc methodDoc : doc.containingClass().methods() )
		{
			if(Shared.i().shouldOmit(methodDoc)){
				continue;
			}
			if( methodDoc.name().equals(doc.name() ))
			{	
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("name", methodDoc.name());
				map.put("object", instanceName);
				
				ArrayList<HashMap<String, String>> parameters = new ArrayList<HashMap<String,String>>();
				for( Parameter p : methodDoc.parameters() )
				{
					HashMap<String, String> paramMap = new HashMap<String, String>();
					paramMap.put("parameter", p.name());
					parameters.add(paramMap);
				}
				String params = templateWriter.writeLoop("Method.Parameter.partial.html", parameters, ", ");
				
				map.put("parameters", params);
				if(! ret.contains(map)){
					//don't put in duplicate function syntax
					ret.add(map);					
				}
			}
		}
		return ret;
	}
	
	protected static String importedName(String fullName){
		if(fullName.contains(".")){
			return fullName.substring(fullName.lastIndexOf(".")+1);
		}
		return fullName;
	}
	
	protected static String getUsage(ProgramElementDoc doc){
		Tag[] tags = doc.tags("usage");
		if(tags.length != 0){
			return tags[0].text();
		}
		tags = doc.containingClass().tags("usage");
		if(tags.length != 0){
			return tags[0].text();
		}
		// return empty string if no usage is found
		return "";
	}
	
	protected static String getInstanceName(ProgramElementDoc doc){
		Tag[] tags = doc.containingClass().tags("instanceName");
		if(tags.length != 0){
			return tags[0].text().split("\\s")[0];
		}
		return "";
	}
	
	protected static String getInstanceDescription(ProgramElementDoc doc){
		Tag[] tags = doc.containingClass().tags("instanceName");
		if(tags.length != 0){
			String s = tags[0].text();
			return s.substring(s.indexOf(" "));
		}
		return "";
	}
	
	protected static String getParameters(MethodDoc doc) throws IOException{		
		//get parent
		ClassDoc cd = doc.containingClass();
		ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String,String>>();
		
		if(!Shared.i().isRootLevel(cd)){
			//add the parent parameter if this isn't a function of PApplet
			HashMap<String, String> parent = new HashMap<String, String>();
			parent.put("name", getInstanceName(doc));
			parent.put("description", cd.name() + ": " + getInstanceDescription(doc));
			ret.add(parent);			
		}
		
		//get parameters from this and all other declarations of method
		for( MethodDoc m : cd.methods() ){
			if(Shared.i().shouldOmit(m)){
				continue;
			}
			if(m.name().equals(doc.name())){
				ret.addAll(parseParameters(m));
			}
		}
		
		removeDuplicateParameters(ret);
		
		
		TemplateWriter templateWriter = new TemplateWriter();
		return templateWriter.writeLoop("Parameter.partial.html", ret);
	}
	
	protected static String getParameters(ClassDoc doc) throws IOException{
		ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String,String>>();
		for( ConstructorDoc m : doc.constructors() ){
			if(Shared.i().shouldOmit(m)){
				continue;
			}
			ret.addAll(parseParameters(m));			
		}
		removeDuplicateParameters(ret);
		
		TemplateWriter templateWriter = new TemplateWriter();
		return templateWriter.writeLoop("Parameter.partial.html", ret);
	}
	
	protected static void removeDuplicateParameters(ArrayList<HashMap<String, String>> ret){
		//combine duplicate parameter names
		for(HashMap<String, String> map : ret){
			String desc = map.get("description");
			if(!desc.endsWith(": ")){
				for(HashMap<String, String> map2 : ret){
					String desc2 = map2.get("description");
					if(desc2.endsWith(": ") && map2.get("name").equals(map.get("name"))){
						if(! desc.contains(desc2.substring(0, desc2.indexOf(": ")))){
							String newDescription = desc2.replace(":", ",") + desc;
							map.put("description", newDescription);							
						}
					}
				}
			}
		}
		//remove parameters without descriptions
		for(int i=ret.size()-1; i >= 0; i-- ){
			if(ret.get(i).get("description").endsWith(": ")){
				ret.remove(i);
			}
		}
	}
	
	protected static ArrayList<HashMap<String, String>> parseParameters(ExecutableMemberDoc doc){
		ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String,String>>();
		for( Parameter param : doc.parameters()){
			String type = importedName(param.type().toString()).concat(": "); 
			String name = param.name();
			String desc = "";

			for( ParamTag tag : doc.paramTags() ){
				if(tag.parameterName().equals(name)){			
					desc = desc.concat( tag.parameterComment() );
				}
			}
			
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("name", name);
			map.put("description", type + desc);
			ret.add(map);
		}
		return ret;
	}
	
	protected static String getRelated(ProgramElementDoc doc) throws IOException{
		TemplateWriter templateWriter = new TemplateWriter();
		ArrayList<HashMap<String, String>> vars = new ArrayList<HashMap<String,String>>();
		for( SeeTag tag : doc.seeTags() ){
			HashMap<String, String> map = new HashMap<String, String>();
			//add the referenced member or the referenced class if no member exists
			if(tag.referencedMember() != null){
				map.put("name", getName(tag.referencedMember()));
				map.put("anchor", getAnchor(tag.referencedMember()));				
			} else {
				map.put("name", getName(tag.referencedClass()));
				map.put("anchor", getAnchor(tag.referencedClass()));
			}
			vars.add(map);
		}
		return templateWriter.writeLoop("Related.partial.html", vars);
	}
	
	protected static String getEvents(ProgramElementDoc doc){
		return "";
	}
	
}
