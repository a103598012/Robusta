package ntut.csie.rleht.builder;


import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;

import ntut.csie.csdet.preference.SmellSettings;
import ntut.csie.csdet.visitor.UserDefinedMethodAnalyzer;
import ntut.csie.filemaker.JavaFileToString;
import ntut.csie.filemaker.JavaProjectMaker;
import ntut.csie.filemaker.RuntimeEnvironmentProjectReader;
import ntut.csie.filemaker.exceptionBadSmells.CarelessCleanupExample;
import ntut.csie.filemaker.exceptionBadSmells.ClassImplementCloseable;
import ntut.csie.filemaker.exceptionBadSmells.ClassWithNotThrowingExceptionCloseable;
import ntut.csie.filemaker.exceptionBadSmells.UserDefinedCarelessCleanupDog;
import ntut.csie.filemaker.exceptionBadSmells.UserDefinedCarelessCleanupWeather;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RLBuilderTest {
	RLBuilder rlBuilder;
	JavaFileToString javaFile2String;
	JavaProjectMaker javaProjectMaker;
	CompilationUnit compilationUnit;
	
	String projectName;
	
	public RLBuilderTest() {
		projectName = RLBuilder.class.getSimpleName() + "Project";
	}

	@Before
	public void setUp() throws Exception {
		rlBuilder = new RLBuilder();
		javaFile2String = new JavaFileToString();
		javaProjectMaker = new JavaProjectMaker(projectName);
//		javaProjectMaker.addAgileExceptionClasses();
//		javaProjectMaker.addJarFromTestProjectToBuildPath("/lib/RL.jar");
		javaProjectMaker.setJREDefaultContainer();
		// �ھڴ����ɮ׼˥����e�إ߷s���ɮ�
		javaFile2String.read(CarelessCleanupExample.class, "test");
		javaProjectMaker.createJavaFile(
				CarelessCleanupExample.class.getPackage().getName(),
				CarelessCleanupExample.class.getSimpleName() + ".java",
				"package " + CarelessCleanupExample.class.getPackage().getName() + ";\n"
				+ javaFile2String.getFileContent());
		javaFile2String.clear();
		
		javaFile2String.read(ClassWithNotThrowingExceptionCloseable.class, "test");
		javaProjectMaker.createJavaFile(
				ClassWithNotThrowingExceptionCloseable.class.getPackage().getName(),
				ClassWithNotThrowingExceptionCloseable.class.getSimpleName() + ".java",
				"package " + ClassWithNotThrowingExceptionCloseable.class.getPackage().getName() + ";\n"
				+ javaFile2String.getFileContent());
		javaFile2String.clear();
		
		javaFile2String.read(ClassImplementCloseable.class, "test");
		javaProjectMaker.createJavaFile(
				ClassImplementCloseable.class.getPackage().getName(),
				ClassImplementCloseable.class.getSimpleName() + ".java",
				"package " + ClassImplementCloseable.class.getPackage().getName() + ";\n"
				+ javaFile2String.getFileContent());
		javaFile2String.clear();
		
		/* ���ըϥΪ̳]�wPattern�ɭԨϥ� */
		javaFile2String.read(UserDefinedCarelessCleanupWeather.class, "test");
		javaProjectMaker.createJavaFile(
				UserDefinedCarelessCleanupWeather.class.getPackage().getName(),
				UserDefinedCarelessCleanupWeather.class.getSimpleName() + ".java",
				"package " + UserDefinedCarelessCleanupWeather.class.getPackage().getName() + ";\n"
				+ javaFile2String.getFileContent());
		javaFile2String.clear();
		
		javaFile2String.read(UserDefinedCarelessCleanupDog.class, "test");
		javaProjectMaker.createJavaFile(
				UserDefinedCarelessCleanupDog.class.getPackage().getName(),
				UserDefinedCarelessCleanupDog.class.getSimpleName() + ".java",
				"package " + UserDefinedCarelessCleanupDog.class.getPackage().getName() + ";\n"
				+ javaFile2String.getFileContent());
		javaFile2String.clear();
		
//		Path ccExamplePath = new Path(
//				projectName	+ "/src/ntut/csie/filemaker/exceptionBadSmells/CarelessCleanupExample.java");
//		//Create AST to parse
//		ASTParser parser = ASTParser.newParser(AST.JLS3);
//		parser.setKind(ASTParser.K_COMPILATION_UNIT);
//		// �]�w�n�Q�إ�AST���ɮ�
//		parser.setSource(
//				JavaCore.createCompilationUnitFrom(
//						ResourcesPlugin.getWorkspace().
//						getRoot().getFile(ccExamplePath)));
//		parser.setResolveBindings(true);
//		compilationUnit = (CompilationUnit) parser.createAST(null);
		
		/** �����ª��]�w�� */
		String oldSettingsContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<CodeSmellDetect>\r\n  <CarelessCleanUp>\r\n    <rule detusermethod=\"Y\" />\r\n    <librule EH_STAR.lib=\"Y\" aaa.EH_STAR=\"Y\" />\r\n  </CarelessCleanUp>\r\n  <DetectSmell>\r\n    <rule detectall=\"Y\" Ignore_Checked_Exception=\"Y\" Dummy_Handler=\"Y\" Nested_Try_Block=\"Y\" Unprotected_Main_Program=\"Y\" Careless_CleanUp=\"Y\" Over_Logging=\"Y\" />\r\n  </DetectSmell>\r\n  <DummyHandler>\r\n    <rule eprintstacktrace=\"Y\" systemoutprint=\"Y\" apache_log4j=\"N\" java_Logger=\"N\" />\r\n    <librule report.EH_STAR=\"Y\" />\r\n  </DummyHandler>\r\n  <OverLogging>\r\n    <rule apache_log4j=\"Y\" java_Logger=\"Y\" />\r\n    <librule />\r\n    <exrule detectionTransException=\"Y\" />\r\n  </OverLogging>\r\n</CodeSmellDetect>\r\n\r\n";
		FileWriter fw = null;
		fw = new FileWriter(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + File.separator + "CSPreference.xml");
		fw.write(oldSettingsContents);
		fw.close();
		
		/** ���ͷs���]�w�� */
		SmellSettings smellSettings = new SmellSettings(UserDefinedMethodAnalyzer.SETTINGFILEPATH);
		smellSettings.addCarelessCleanupPattern(UserDefinedCarelessCleanupWeather.class.getName() + ".bark", true);
		smellSettings.writeXMLFile(UserDefinedMethodAnalyzer.SETTINGFILEPATH);
	}

	@After
	public void tearDown() throws Exception {
		File cspre = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + File.separator + "CSPreference.xml");
		if(cspre.exists())
			assertTrue(cspre.delete());
		
		File smellSettings = new File(UserDefinedMethodAnalyzer.SETTINGFILEPATH);
		if(smellSettings.exists())
			assertTrue(smellSettings.delete());
	}

	@Test
	public void testCheckBadSmells() throws Exception {	
		IResource resource = RuntimeEnvironmentProjectReader.getIResource(
				projectName, CarelessCleanupExample.class.getPackage().getName(),
				CarelessCleanupExample.class.getSimpleName());

		Method getDetectSettings = RLBuilder.class.getDeclaredMethod("getDetectSettings");
		getDetectSettings.setAccessible(true);
		getDetectSettings.invoke(rlBuilder);
		
		Method checkBadSmells = RLBuilder.class.getDeclaredMethod("checkBadSmells", IResource.class);
		checkBadSmells.setAccessible(true);
		checkBadSmells.invoke(rlBuilder, resource);
		
		IFile iFile = (IFile)resource;
		for (int i = 0; i < 24; i++) {
			IMarker im = iFile.getMarker(Long.valueOf(i));
			assertTrue("��" + String.valueOf(i) + "��Marker�����D�C", im.exists());
		}
	}
}