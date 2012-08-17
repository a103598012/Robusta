package ntut.csie.csdet.preference;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import agile.exception.RL;
import agile.exception.Robustness;

public class SmellSettings {
	public final static String SETTING_FILENAME = "SmellSetting.xml";
	public final static String TAG_ROOT = "CodeSmells";
	public final static String TAG_SMELLTYPE4DETECTING = "SmellTypes";
	public final static String TAG_PATTERN = "pattern";
	public final static String TAG_EXTRARULE = "extraRule";
	public final static String ATTRIBUTE_NAME = "name";
	public final static String ATTRIBUTE_ISDETECTING = "isDetecting";	
	
	public final static String SMELL_IGNORECHECKEDEXCEPTION = "IgnoreCheckedException";
	public final static String SMELL_DUMMYHANDLER = "DummyHandler";
	public final static String SMELL_NESTEDTRYBLOCK = "NestedTryBlock";
	public final static String SMELL_UNPROTECTEDMAINPROGRAM = "UnprotectedMainProgram";
	public final static String SMELL_OVERLOGGING = "OverLogging";
	public final static String SMELL_CARELESSCLEANUP = "CarelessCleanup";
	/** �ҥ~�૬���~�򰻴� */
	public final static String EXTRARULE_OVERLOGGING_DETECTWRAPPINGEXCEPTION = "DetectWrappingExcetion";
	/** ��������귽���{���X�O�_�b�禡�� */
	public final static String EXTRARULE_CARELESSCLEANUP_DETECTISRELEASEIOCODEINDECLAREDMETHOD = "DetectIsReleaseIOCodeInDeclaredMethod";
	
	public final static String EXTRARULE_ePrintStackTrace = "printStackTrace";
	public final static String EXTRARULE_SystemOutPrint = "System.out.print";
	public final static String EXTRARULE_SystemOutPrintln = "System.out.println";
	public final static String EXTRARULE_SystemErrPrint = "System.err.print";
	public final static String EXTRARULE_SystemErrPrintln = "System.err.println";
	public final static String EXTRARULE_OrgApacheLog4j = "org.apache.log4j";
	public final static String EXTRARULE_JavaUtilLoggingLogger = "java.util.logging.Logger";
	private Document settingDoc;
	
	public SmellSettings() {
		settingDoc = new Document(new Element(TAG_ROOT));
	}
	
	@Robustness(value = { @RL(level = 1, exception = java.lang.RuntimeException.class) })
	public SmellSettings(File xmlFile) {
		this();
		if(!xmlFile.exists()) {
			return;
		}
		SAXBuilder builder = new SAXBuilder();
		try {
			settingDoc = builder.build(xmlFile);
		} catch (JDOMException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Robustness(value = { @RL(level = 1, exception = java.lang.RuntimeException.class) })
	public SmellSettings(String xmlFilepath) {
		this(new File(xmlFilepath));
	}
	
	public boolean isDetectingSmell(String badSmellName) {
		Element root = settingDoc.getRootElement();
		List<?> elements = root.getChildren(TAG_SMELLTYPE4DETECTING);
	
		for (Object s : elements) {
			Element smellTypeElement = (Element)s;
			if(smellTypeElement.getAttributeValue(ATTRIBUTE_NAME).equals(badSmellName)) {
				return Boolean.parseBoolean(smellTypeElement.getAttributeValue(ATTRIBUTE_ISDETECTING));
			}
		}
		return false;
	}

	/**
	 * �p�G�Q���o��bad smell name���s�b�A
	 * �N�|�۰ʲ��ͥH�o��bad smell name���ݩ�name���`�I�A
	 * �åB�]�wisDetecting�ݩʬ�true�C
	 * @param badSmellName
	 * @return
	 */
	public Element getSmellType(String badSmellName) {
		Element root = settingDoc.getRootElement();
		List<?> elements = root.getChildren(TAG_SMELLTYPE4DETECTING);
		Element badSmellElement = null;
		
		for (Object s : elements) {
			Element smellTypeElement = (Element)s;
			if(smellTypeElement.getAttribute(ATTRIBUTE_NAME).getValue().equals(badSmellName)) {
				badSmellElement = smellTypeElement;
				return badSmellElement;
			}
		}
					
		if(badSmellElement==null) {
			badSmellElement = new Element(TAG_SMELLTYPE4DETECTING);
			badSmellElement.setAttribute(ATTRIBUTE_NAME, badSmellName);
			badSmellElement.setAttribute(ATTRIBUTE_ISDETECTING, String.valueOf(true));
			root.addContent(badSmellElement);
		}
		
		return badSmellElement;
	}
	
	public void setSmellTypeAttribute(String badSmellName, String attributeName, String attributeValue) {
		Element badSmellElement = getSmellType(badSmellName);
		badSmellElement.setAttribute(attributeName, attributeValue);
	}
	
	public void addDummyHandlerPattern(String patternName, boolean isDetecting) {
		addPattern(SMELL_DUMMYHANDLER, patternName, isDetecting);
	}
	
	public void addOverLoggingPattern(String patternName, boolean isDetecting) {
		addPattern(SMELL_OVERLOGGING, patternName, isDetecting);
	}
	
	public void addCarelessCleanupPattern(String patternName, boolean isDetecting) {
		addPattern(SMELL_CARELESSCLEANUP, patternName, isDetecting);
	}
	
	/**
	 * ��X���w��bad smell���Ҧ��ҥΪ�patterns
	 * �S���ҥΪ�pattern�N���|�Q�[�Jlist��
	 * @param smellName
	 * @return
	 */
	public List<String> getAllDetectingPatterns(String smellName) {
		Element root = settingDoc.getRootElement();
		List<?> childrenElements = root.getChildren(TAG_SMELLTYPE4DETECTING);
		Element badSmellElement = null;
		for (Object object : childrenElements) {
			Element e = (Element) object;
			if(e.getAttributeValue(ATTRIBUTE_NAME).equals(smellName)) {
				badSmellElement = e;
				break;
			}
		}

		if(badSmellElement == null) {
			return new ArrayList<String>();
		}
		
		List<String> patternList = new ArrayList<String>();
		List<?> patternElements = badSmellElement.getChildren(TAG_PATTERN);
		for (Object object : patternElements) {
			Element e = (Element) object;
			if(e.getAttributeValue(ATTRIBUTE_ISDETECTING).equals(String.valueOf(true))) {
				patternList.add(e.getAttributeValue(ATTRIBUTE_NAME));
			}
		}
		return patternList;
	}
	
	/**
	 * ���ϥΪ̿�ܭn�M�䪺Pattern�OClass�BMethod�B�άOClass+Method
	 * @param smellName
	 * @param type
	 * @return
	 */
	public List<String> getDetectingPatterns(String smellName, UserDefinedConstraintsType type) {
		List<String> adoptedPatterns = new ArrayList<String>();
		
		for(String pattern : getAllDetectingPatterns(smellName)) {
			if(pattern.indexOf(".*") != -1) {
				if (type == UserDefinedConstraintsType.Library) {
					adoptedPatterns.add(pattern);
				}
			} else if (pattern.indexOf("*.") != -1) {
				if (type == UserDefinedConstraintsType.Method) {
					adoptedPatterns.add(pattern);
				}
			} else if (pattern.indexOf(".") != -1) {
				if (type == UserDefinedConstraintsType.FullQulifiedMethod) {
					adoptedPatterns.add(pattern);
				}
			} else {
				if(type == UserDefinedConstraintsType.Method) {
					adoptedPatterns.add(pattern);
				}
			}
		}
		return adoptedPatterns;
	}
	
	/**
	 * ���^�S�wsmell�Ҧ�Pattern���]�w��
	 * @param smellName
	 * @return
	 */
	public TreeMap<String, Boolean> getSemllPatterns(String smellName) {
		TreeMap<String, Boolean> result = new TreeMap<String, Boolean>();
		Element root = settingDoc.getRootElement();
		List<?> childrenElements = root.getChildren(TAG_SMELLTYPE4DETECTING);
		Element badSmellElement = null;
		for (Object object : childrenElements) {
			Element e = (Element) object;
			if(e.getAttributeValue(ATTRIBUTE_NAME).equals(smellName)) {
				badSmellElement = e;
				break;
			}
		}
		
		if(badSmellElement == null) {
			return result;
		}
		
		List<?> patternElements = badSmellElement.getChildren(TAG_PATTERN);
		for (Object object : patternElements) {
			Element e = (Element) object;
			result.put(e.getAttributeValue(ATTRIBUTE_NAME), Boolean.parseBoolean(e.getAttributeValue(ATTRIBUTE_ISDETECTING)));
		}
		
		return result;
	}
	
	/**
	 * pattern�O�ϥΪ̦ۦ��J���{���X�A�o�ǵ{���X�|�Q�O���_�ӡA
	 * �M��Τ@��isDetecting�ӨM�w�ˬdbad smell���ɭԭn���n�@�_���ˬd�C
	 * �u���b�ϥΪ̧R���ۦ��J���{���X�ɡApattern node�~�|�Q�R���C
	 * @param badSmellName
	 * @param patternContent
	 * @param isDetecting
	 */
	private void addPattern(String badSmellName, String patternContent, boolean isDetecting) {	
		Element badSmellElement = getSmellType(badSmellName);
		
		// ���F�o�̰��ˬd�ʧ@�A����ϥΪ̥[�J���ƪ�pattern�A�e�ݤ]�n�O�o�ˬd
		List<?> patternElements = badSmellElement.getChildren(TAG_PATTERN);
		for (Object object : patternElements) {
			Element pattern = (Element) object;
			if(pattern.getAttribute(ATTRIBUTE_NAME).getValue().equals(patternContent)) {
				pattern.setAttribute(ATTRIBUTE_ISDETECTING, String.valueOf(isDetecting));		
				return;
			}
		}
		// �T�wpattern���s�b�A�N�[�J�s��node
		Element pattern = new Element(TAG_PATTERN);
		pattern.setAttribute(ATTRIBUTE_NAME, patternContent);
		pattern.setAttribute(ATTRIBUTE_ISDETECTING, String.valueOf(isDetecting));

		badSmellElement.addContent(pattern);
	}
	
	public boolean removePatterns(String smellName) {
		Element badSmellElement = getSmellType(smellName);
		return badSmellElement.removeChildren(TAG_PATTERN);
	}
	
	/**
	 * extraRule�O�ڭ̴��ѵ��ϥΪ̤Ŀ諸�ﶵ�A�ҥH���ϥΪ̦��Ŀ�ɡA
	 * �o��extraRule��node�~�|�X�{�A�ϥΪ̦p�G�����Ŀ�A�N�|�R���o��node�C
	 * @param badSmellName
	 * @param ruleName
	 */
	public void addExtraRule(String badSmellName, String ruleName) {	
		Element badSmellElement = getSmellType(badSmellName);
		
		List<?> patternElements = badSmellElement.getChildren(TAG_EXTRARULE);
		for (Object object : patternElements) {
			Element pattern = (Element) object;
			if(pattern.getAttribute(ATTRIBUTE_NAME).getValue().equals(ruleName)) {
				return;
			}
		}

		Element extraRule = new Element(TAG_EXTRARULE);
		extraRule.setAttribute(ATTRIBUTE_NAME, ruleName);	
		badSmellElement.addContent(extraRule);
	}
	
	public boolean isExtraRuleExist(String badSmellName, String ruleName) {
		Element badSmellElement = getSmellType(badSmellName);
		
		// �p�G�`�I�b�A�h��X�O�_���o��extra rule
		List<?> extraRules = badSmellElement.getChildren();
		for(Object object : extraRules) {
			Element extraRule = (Element) object;
			if(extraRule.getAttribute(ATTRIBUTE_NAME).getValue().equals(ruleName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param badSmellName
	 * @param ruleName
	 * @return true�N��Rule�s�b�A�åB�������\�C<br />
	 * 		   false�i��O�������ѡA�]�i��ORule�q�Ӥ��s�b�C
	 */
	public boolean removeExtraRule(String badSmellName, String ruleName) {
		Element badSmellElement = getSmellType(badSmellName);
		List<?> extraRules = badSmellElement.getChildren(TAG_EXTRARULE);
		for(Object object : extraRules) {
			Element extraRule = (Element) object;
			if(extraRule.getAttribute(ATTRIBUTE_NAME).getValue().equals(ruleName)) {
				return badSmellElement.removeContent(extraRule);
			}
		}
		return false;
	}
	
	public TreeMap<String, UserDefinedConstraintsType> getSmellSettings(String badSmellName) {
		TreeMap<String, UserDefinedConstraintsType> libMap = new TreeMap<String, UserDefinedConstraintsType>();
		Element badSmellElement = getSmellType(badSmellName);
		// �����������bad smell�A�h��������Ū���ʧ@
		if(!Boolean.parseBoolean(badSmellElement.getAttributeValue(ATTRIBUTE_ISDETECTING))) {
			return libMap;
		}
		// add extra rules to libMap
		List<?> extraRules = badSmellElement.getChildren(TAG_EXTRARULE);
		for(Object object : extraRules) {
			Element extraRule = (Element) object;
			String rule = extraRule.getAttribute(ATTRIBUTE_NAME).getValue();
			if(	rule.equals(EXTRARULE_SystemOutPrint) || rule.equals(EXTRARULE_SystemOutPrintln) ||
				rule.equals(EXTRARULE_SystemErrPrint) || rule.equals(EXTRARULE_SystemErrPrintln)) {
				libMap.put("java.io.PrintStream" + rule.substring(rule.lastIndexOf(".")), UserDefinedConstraintsType.FullQulifiedMethod);
				continue;
			}
			if(rule.equals(EXTRARULE_OrgApacheLog4j) || rule.equals(EXTRARULE_JavaUtilLoggingLogger)) {
				libMap.put(rule, UserDefinedConstraintsType.Library);
				continue;
			}
			if(rule.equals(EXTRARULE_ePrintStackTrace)) {
				libMap.put(rule, UserDefinedConstraintsType.Method);
				continue;
			}
			if(rule.equals(EXTRARULE_CARELESSCLEANUP_DETECTISRELEASEIOCODEINDECLAREDMETHOD)) {
				libMap.put(rule, UserDefinedConstraintsType.FullQulifiedMethod);
				continue;
			}
		}
		// add patterns to libMap
		List<String> patterns = getAllDetectingPatterns(badSmellName);
		for(String pattern : patterns) {
			if(pattern.indexOf(".*") != -1) {
				int pos = pattern.indexOf(".*");
				libMap.put(pattern.substring(0, pos), UserDefinedConstraintsType.Library);
			} else if(pattern.indexOf("*.") != -1) {
				libMap.put(pattern.substring(2), UserDefinedConstraintsType.Method);
			} else if(pattern.lastIndexOf(".") != -1) {
				libMap.put(pattern, UserDefinedConstraintsType.FullQulifiedMethod);
			} else {
				libMap.put(pattern, UserDefinedConstraintsType.Method);
			}
		}
		
		return libMap;
	}

	@Robustness(value = { @RL(level = 1, exception = java.lang.RuntimeException.class) })
	public void writeXMLFile(String path) {
		FileWriter fw = null;
		XMLOutputter out = new XMLOutputter();
		try {
			fw = new FileWriter(path);
			out.output(settingDoc, fw);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			close(fw);
		}
	}

	private void close(Closeable ioInstance) {
		if(ioInstance != null) {			
			try {
				ioInstance.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static enum UserDefinedConstraintsType {
		Library,
		Method,
		FullQulifiedMethod
	}
}