package ntut.csie.analyzer.careless.closingmethod;

import static org.junit.Assert.assertEquals;

import java.util.List;

import ntut.csie.analyzer.UserDefinedMethodAnalyzer;
import ntut.csie.analyzer.careless.CloseResourceMethodInvocationVisitor;
import ntut.csie.csdet.preference.SmellSettings;
import ntut.csie.filemaker.TestEnvironmentBuilder;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CloseResourceMethodInvocationVisitorTest {
	
	private TestEnvironmentBuilder environmentBuilder;
	private CloseResourceMethodInvocationVisitor visitor;
	
	@Before
	public void setUp() throws Exception {
		environmentBuilder = new TestEnvironmentBuilder("testCloseResourceProject");
		environmentBuilder.createTestEnvironment();

		environmentBuilder.loadClass(CloseResourceMethodInvocationExample.class);
		environmentBuilder.loadClass(ClassCanCloseButNotImplementCloseable.class);
		environmentBuilder.loadClass(ClassImplementCloseable.class);
		environmentBuilder.loadClass(UserDefinedCarelessCleanupMethod.class);
		environmentBuilder.loadClass(UserDefinedCarelessCleanupClass.class);
		environmentBuilder.loadClass(ClassImplementCloseableWithoutThrowException.class);
		environmentBuilder.loadClass(ResourceCloser.class);
	}

	@After
	public void tearDown() throws Exception {
		environmentBuilder.cleanTestEnvironment();
	}
	
	@Test
	public void testExampleWithoutAnyExtraRule() throws Exception {
		List<MethodInvocation> miList = 
				visitCompilationAndGetSmellList(CloseResourceMethodInvocationExample.class);
		assertEquals(6, miList.size());
	}

	@Test
	public void testExampleWithWithUserDefinedMethodClose() throws Exception {
		// Create setting file with user defined
		SmellSettings smellSettings = new SmellSettings(UserDefinedMethodAnalyzer.SETTINGFILEPATH);
		smellSettings.addExtraRule(SmellSettings.SMELL_CARELESSCLEANUP, SmellSettings.EXTRARULE_CARELESSCLEANUP_DETECTISRELEASEIOCODEINDECLAREDMETHOD);
		smellSettings.addCarelessCleanupPattern("*.close", true);
		smellSettings.writeXMLFile(UserDefinedMethodAnalyzer.SETTINGFILEPATH);

		List<MethodInvocation> miList = 
				visitCompilationAndGetSmellList(CloseResourceMethodInvocationExample.class);
		
		assertEquals(9, miList.size());
	}
	
	@Test
	public void testExampleWithWithUserDefinedMethodShine() throws Exception {
		// Create setting file with user defined
		SmellSettings smellSettings = new SmellSettings(UserDefinedMethodAnalyzer.SETTINGFILEPATH);
		smellSettings.addExtraRule(SmellSettings.SMELL_CARELESSCLEANUP, SmellSettings.EXTRARULE_CARELESSCLEANUP_DETECTISRELEASEIOCODEINDECLAREDMETHOD);
		smellSettings.addCarelessCleanupPattern("*.Shine", true);
		smellSettings.writeXMLFile(UserDefinedMethodAnalyzer.SETTINGFILEPATH);

		List<MethodInvocation> miList = 
				visitCompilationAndGetSmellList(CloseResourceMethodInvocationExample.class);
		
		assertEquals(11, miList.size());
	}
	
	@Ignore
	public void testGetCloseMethodInvocationListWithUserDefiendLibs() throws Exception {
		SmellSettings smellSettings = new SmellSettings(UserDefinedMethodAnalyzer.SETTINGFILEPATH);
		smellSettings.addCarelessCleanupPattern(UserDefinedCarelessCleanupMethod.class.getName() + ".*", true);
		smellSettings.writeXMLFile(UserDefinedMethodAnalyzer.SETTINGFILEPATH);

		environmentBuilder.accept(Object.class, visitor);
		assertEquals(42, visitor.getCloseMethodInvocations().size());
	}
	
	@Ignore
	public void testGetCloseMethodInvocationListWithUserDefinedMethods() throws Exception {
		SmellSettings smellSettings = new SmellSettings(UserDefinedMethodAnalyzer.SETTINGFILEPATH);
		smellSettings.addCarelessCleanupPattern("*.bark", true);
		smellSettings.writeXMLFile(UserDefinedMethodAnalyzer.SETTINGFILEPATH);

		environmentBuilder.accept(Object.class, visitor);
		assertEquals(38, visitor.getCloseMethodInvocations().size());
	}
	
	@Ignore
	public void testGetCloseMethodInvocationListWithUserDefinedFullQualifiedMethods() throws Exception {
		SmellSettings smellSettings = new SmellSettings(UserDefinedMethodAnalyzer.SETTINGFILEPATH);
		smellSettings.addCarelessCleanupPattern(UserDefinedCarelessCleanupMethod.class.getName() + ".bark", true);
		smellSettings.writeXMLFile(UserDefinedMethodAnalyzer.SETTINGFILEPATH);

		environmentBuilder.accept(Object.class, visitor);
		assertEquals(37, visitor.getCloseMethodInvocations().size());
	}

	private List<MethodInvocation> visitCompilationAndGetSmellList(Class clazz)
			throws JavaModelException {
		CompilationUnit compilationUnit = environmentBuilder
				.getCompilationUnit(clazz);
		visitor = new CloseResourceMethodInvocationVisitor(compilationUnit);
		compilationUnit.accept(visitor);
		List<MethodInvocation> miList = visitor.getCloseMethodInvocations();
		return miList;
	}
	
}