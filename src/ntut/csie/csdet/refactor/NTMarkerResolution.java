package ntut.csie.csdet.refactor;

import ntut.csie.rleht.builder.RLMarkerAttribute;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.ui.refactoring.code.ExtractMethodWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class NTMarkerResolution implements IMarkerResolution {
	private static Logger logger = LoggerFactory.getLogger(NTMarkerResolution.class);
	
	private String label;
	
	public NTMarkerResolution(String label){
		this.label = label;
	}
	
	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void run(IMarker marker) {
		try {
			String problem = (String) marker.getAttribute(RLMarkerAttribute.RL_MARKER_TYPE);
			if (problem != null
					&& problem.equals(RLMarkerAttribute.CS_NESTED_TRY_STATEMENT)) {
				CompilationUnit root = getCompilationUnit(marker.getResource());
				ASTNode selectedNode = getBadSmellNode(root, marker);

				ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(
						root, selectedNode.getStartPosition(), selectedNode.getLength());
				ExtractMethodWizard refactoringWizard = new ExtractMethodWizard(refactoring);
				RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(refactoringWizard);
				operation.run(new Shell(), "Dialog Title");
			}
		} catch (Exception e) {
			logger.error("[NTMarkerResolution] EXCEPTION ", e);
			throw new RuntimeException(e);
		}
	}
	
	private CompilationUnit getCompilationUnit(IResource resource) {
		if (resource instanceof IFile && resource.getName().endsWith(".java")) {
			try {
				IJavaElement javaElement = JavaCore.create(resource);
	
				//Create AST to parse
				ASTParser parser = ASTParser.newParser(AST.JLS3);
				parser.setKind(ASTParser.K_COMPILATION_UNIT);

				parser.setSource((ICompilationUnit) javaElement);
				parser.setResolveBindings(true);
				CompilationUnit root = (CompilationUnit) parser.createAST(null);
				return root;			
			} catch (Exception e) {
				logger.error("[Extract Method] EXCEPTION ", e);
			}
		}
		return null;
	}

	private ASTNode getBadSmellNode(CompilationUnit astRoot, IMarker marker) {
		String strSourcePosition = marker.getAttribute(RLMarkerAttribute.RL_INFO_SRC_POS, null);
		int sourcePosition = Integer.parseInt(strSourcePosition);
		return NodeFinder.perform(astRoot, sourcePosition, 0);
	}
}
