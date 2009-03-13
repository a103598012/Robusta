package ntut.csie.rleht.caller;

import net.java.amateras.uml.action.SaveAsImageAction;
import ntut.csie.rleht.RLEHTPlugin;
import ntut.csie.rleht.common.ConsoleLog;
import ntut.csie.rleht.common.EditorUtils;
import ntut.csie.rleht.common.RLUtils;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.PrintAction;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.CallLocation;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class CallersView extends ViewPart implements IDoubleClickListener, ICheckStateListener {
	private static Logger logger = LoggerFactory.getLogger(CallersView.class);

	public static final String ID = "ntut.csie.rleht.caller.CallersView"; //$NON-NLS-1$

	public static final boolean ACT_EDITOR_ON_SELECT = true;

	// private CallersEventHandler eventHandler;

	// -------------------------------------------------------------------------
	// Business Object
	// -------------------------------------------------------------------------

	// private ITextEditor actEditor;
	//
	// private IDocument actDocument;
	//
	// private boolean changeDocument = false;

	private CheckboxTreeViewer treeviewer = null;

	// private String lastMethodName = null;

	private CallersViewAction viewActions = null;

	private Menu treeContextMenu;

	private boolean showCaller = false;

	private IMethod lastMethod = null;

	/**
	 * The constructor.
	 */
	public CallersView() {

	}

	public void init(IViewSite site) throws PartInitException {
		super.setSite(site);

		logger.debug("========[CallersView]=========");

		// if (this.eventHandler == null) {
		// this.eventHandler = new CallersEventHandler(this);
		// site.getWorkbenchWindow().getSelectionService().addPostSelectionListener(eventHandler);
		// site.getPage().addPartListener(eventHandler);
		// FileBuffers.getTextFileBufferManager().addFileBufferListener(eventHandler);
		// }
	}

	/**
	 * Create contents of the view part
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		this.createTreeViewer(parent);

		this.createActions();

		this.createTreePopupMenu();

		this.contributeToActionBars();
	}

	private void createTreeViewer(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		int style = SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE;
		treeviewer = new CheckboxTreeViewer(composite, style);
		treeviewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

		treeviewer.setContentProvider(new CallersContentProvider());
		treeviewer.setLabelProvider(new CallersLabelProvider());

		Tree tree = treeviewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
		column1.setText("呼叫階層");
		column1.setWidth(400);
		treeviewer.addFilter(new CallersViewerFilter());

		TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
		column2.setText("@RL{Level,Exception}");
		column2.setWidth(250);

		TreeColumn column3 = new TreeColumn(tree, SWT.LEFT);
		column3.setText("例外");
		column3.setWidth(350);

		treeviewer.addDoubleClickListener(this);
		treeviewer.addCheckStateListener(this);

	}

	private void createActions() {
		viewActions = new CallersViewAction(this);
	}

	private void createTreePopupMenu() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});

		this.treeContextMenu = menuMgr.createContextMenu(this.treeviewer.getControl());
		this.treeviewer.getControl().setMenu(this.treeContextMenu);

		getSite().registerContextMenu(menuMgr, this.treeviewer);
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(new GroupMarker(CallersViewAction.MENU_GROUP_ID));

		viewActions.setContext(new ActionContext(this.treeviewer.getSelection()));
		viewActions.fillContextMenu(manager);
		viewActions.setContext(null);

		manager.add(new GroupMarker(IContextMenuConstants.GROUP_ADDITIONS));
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		viewActions.fillActionBars(bars);
	}

	// *************************************************************************

	@Override
	public void setFocus() {
		// Set the focus
	}

	public void dispose() {
		if ((treeContextMenu != null) && !treeContextMenu.isDisposed()) {
			treeContextMenu.dispose();
		}
		super.dispose();

	}

	// *************************************************************************
	// 事件處理
	// *************************************************************************

	public void handleSelectionChanged4Editor() {

		try {
			IEditorPart editor = getSite().getPage().getActiveEditor();
			ISelection selection = editor.getEditorSite().getSelectionProvider().getSelection();
			if ((selection != null) && selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;
				IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
				ICompilationUnit cu = manager.getWorkingCopy(editor.getEditorInput());

				if (cu != null) {
					IMethod method = getMethodAt(cu, textSelection.getOffset());
					if (method != null) {

						// logger.debug("$#$#$#=(handleSelectionChanged4Editor)--->"+method);
						// if (lastMethodName != null &&
						// method.toString().equals(lastMethodName)) {
						// return;
						// }
						// lastMethodName = method.toString();
						//取得使用者所選擇要Call Hierarchy的method
						lastMethod = method;
						
						this.updateView(method);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("[handleSelectionChanged4Editor] EXCEPTION ", ex);
		}
	}

	public void updateView() {
		this.updateView(this.lastMethod);
	}

	protected void updateView(IMethod method) {
		if (method != null) {
			//依據showCaller來決定是往上或往下做Call Hierarchy
			//getCallerRoot:由下往上call,getCalleeRoot:由上往下call
			MethodWrapper mw = showCaller ? new CallHierarchy().getCallerRoot(method) : new CallHierarchy().getCalleeRoot(method);
			//不論是由上往下或由下往上的Call Hierarchy最多都先只展開兩層而已
			//防止memory一次用太多,容易memory leak
			int expand = showCaller ? 2 : 2;

			CallersRoot root = new CallersRoot(mw);
			treeviewer.setInput(root);
			if (root != null) {
				treeviewer.expandToLevel(expand);
				treeviewer.getTree().setFocus();
				treeviewer.getTree().setSelection(new TreeItem[] { treeviewer.getTree().getItems()[0] });
			}
		}
	}

	@SuppressWarnings("deprecation")
	private IMethod getMethodAt(ICompilationUnit cu, int offset) {
		if (cu != null) {
			synchronized (cu) {
				try {
					cu.reconcile();
				} catch (JavaModelException ex) {
					logger.error("[getMethodAt] EXCEPTION ", ex);
					return null;
				}
			}

			try {
//				System.out.println("=====CU====="+cu.getElementName());
				IType[] types = cu.getAllTypes();
				for (int i = 0, size = types.length; i < size; i++) {
					IType type = types[i];
//					System.out.println("=====Type"+i+"====="+type.getElementName());
					IMethod[] methods = type.getMethods();
					
					for (int j = 0; j < methods.length; j++) {
//						System.out.println("=====Method"+j+"====="+methods[j].getElementName());
						if (isWithinMethodRange(offset, methods[j])) {
							return methods[j];
						}
					}

				}
			} catch (JavaModelException ex) {
				logger.error("[getMethodAt] EXCEPTION ", ex);

				return null;
			}
		}

		return null;
	}

	private boolean isWithinMethodRange(int offset, IMethod method) throws JavaModelException {
		ISourceRange range = method.getSourceRange();

		return ((offset >= range.getOffset()) && (offset <= (range.getOffset() + range.getLength())));
	}

	// *************************************************************************
	// 事件處理
	// *************************************************************************

	public void gotoSelection(ISelection selection) {
		ConsoleLog.debug("[gotoSelection]selection=" + selection);
		try {
			if ((selection != null) && selection instanceof IStructuredSelection) {
				Object structuredSelection = ((IStructuredSelection) selection).getFirstElement();

				if (structuredSelection instanceof MethodWrapper) {
					MethodWrapper methodWrapper = (MethodWrapper) structuredSelection;
					CallLocation firstCall = methodWrapper.getMethodCall().getFirstCallLocation();

					if (firstCall != null) {
						gotoLocation(firstCall);
					} else {
						gotoMethod((IMethod) methodWrapper.getMember());
					}
				} else if (structuredSelection instanceof CallLocation) {
					gotoLocation((CallLocation) structuredSelection);
				}
			}
		} catch (Exception e) {
			RLEHTPlugin.logError("Double click ERROR!!", e);
		}
	}

	private void gotoMethod(IMethod method) {
		if (method != null) {
			try {
				IEditorPart methodEditor = RLUtils.openInEditor(method, ACT_EDITOR_ON_SELECT);
				JavaUI.revealInEditor(methodEditor, (IJavaElement) method);
			} catch (JavaModelException e) {
				RLEHTPlugin.logError("取得資源錯誤！", e);
			} catch (PartInitException e) {
				RLEHTPlugin.logError("開啟編輯器錯誤！", e);
			}
		}
	}

	private void gotoLocation(CallLocation callLocation) {
		try {
			IEditorPart methodEditor = RLUtils.openInEditor((IMethod) callLocation.getMember(), ACT_EDITOR_ON_SELECT);

			if (methodEditor instanceof ITextEditor) {
				ITextEditor editor = (ITextEditor) methodEditor;
				editor.selectAndReveal(callLocation.getStart(), (callLocation.getEnd() - callLocation.getStart()));
			}
		} catch (PartInitException ex) {
			RLEHTPlugin.logError("開啟編輯器錯誤！", ex);
		} catch (JavaModelException ex) {
			RLEHTPlugin.logError("取得資源錯誤！", ex);
		} catch (Exception ex) {
			RLEHTPlugin.logError("其它錯誤！", ex);
		}
	}

	public void doubleClick(DoubleClickEvent event) {
		this.gotoSelection(event.getSelection());

	}

	// *************************************************************************
	// ICheckStateListener 事件處理 BEGIN
	// *************************************************************************

	public void checkStateChanged(CheckStateChangedEvent event) {
		logger.debug("[checkStateChanged] BEGIN --->");
		// if (event.getChecked()) {

		// showCheckData(this.treeviewer.getTree().getItems());

		Object obj = event.getElement();

		if (obj instanceof MethodWrapper) {
			TreeItem[] selection = this.treeviewer.getTree().getSelection();
			//logger.debug("~~~~=>" + selection.length + " :" + selection[0]);

			TreeItem item = this.findCheckedItem(selection, obj);
			if (item != null) {
				if(showCaller){
					checkMultiChoice(item);	
				}				
				//如果使用者勾選Tree中較下層的選項,則會幫他連上層的選項都勾選
				if (item.getChecked()) {
					item = item.getParentItem();
					while (item != null) {
						if (!item.getChecked()) {
							item.setChecked(true);
						}
						item = item.getParentItem();
					}
				} else {
					//假設call chain 為A->B->C,使用者將B取消掉的話,要將C同時取消掉
					disableChildCheckData(item);
				}
				
			}

		}

		logger.debug("[checkStateChanged] END <---");
	}

	private TreeItem findCheckedItem(TreeItem[] items, Object selectedData) {
		logger.debug("\t---->findCheckedItem BEGIN");
		for (int i = 0, size = items.length; i < size; i++) {
			TreeItem item = items[i];
			//selectedData : 被勾選的項目
			logger.debug("\t---->"+ i+" >> " +item.getItemCount()+":"+ selectedData + ":" + item.getData() + " = " + (selectedData == item.getData()) + " = " + (selectedData.equals(item.getData())));
			if (selectedData == item.getData()) {
				return item;
			}

			if (item.getItemCount() >= 1) {
				item = findCheckedItem(item.getItems(), selectedData);
				if (item != null) {
					return item;
				}
			}
		}
		return null;
	}

	private void checkMultiChoice(TreeItem item){
//		System.out.println("【Tree Item】====>"+item.getText());
		boolean checked = false;
		TreeItem parentItem = item.getParentItem();
		if(parentItem != null){
			TreeItem[] items = parentItem.getItems();
			for(int i = 0; i<items.length; i++){
				if(items[i].getChecked() && !(items[i].getData().equals(item.getData())) ){
//					System.out.println("【========Find Items========】");
//					System.out.println("【Item Name】=====>"+items[i].getText());
					EditorUtils.showMessage("一次只能選擇一條路徑");
					item.setChecked(false);
					break;
				}
			}	
		}
		
		
	}
	
	private void disableChildCheckData(TreeItem actitem) {
//		 logger.debug("\t---->disableChildCheckData BEGIN");

		actitem.setChecked(false);
		TreeItem[] items = actitem.getItems();

		if (items != null) {
			for (int i = 0, size = items.length; i < size; i++) {
				TreeItem item = items[i];
				if (item.getChecked()) {
					item.setChecked(false);
				}

				if (item.getItemCount() >= 1) {
					disableChildCheckData(item);
				}
			}
		}
	}

	private void showCheckData(TreeItem[] items) {
		for (int xxx = 0, xsize = items.length; xxx < xsize; xxx++) {
			TreeItem item = items[xxx];

			if (item.getData() instanceof MethodWrapper) {
				MethodWrapper mwobj = (MethodWrapper) item.getData();
				logger.debug(xxx + ") " + item.getItemCount() + " : " + mwobj.getName() + ":" + mwobj.getLevel());
			}

			if (item.getItemCount() >= 1) {
				showCheckData(item.getItems());
			}

			// // ------------------------------------------------------
			// TreeItem[] items2 = item.getItems();
			// for (int yyy = 0, ysize = items2.length; yyy < ysize; yyy++) {
			// TreeItem item2 = items2[yyy];
			// if (item2.getChecked()) {
			// MethodWrapper mwobj = (MethodWrapper) item2.getData();
			// logger.debug(xxx + ") " + item2.getItemCount() + " : " +
			// mwobj.getName() + ":" + mwobj.getLevel());
			// }
			// }
			// // ------------------------------------------------------

		}
	}

	// *************************************************************************
	// ICheckStateListener 事件處理 END
	// *************************************************************************

	public void handleGenSeqDiagram(boolean isShowCallerType) {
		new CallersSeqDiagram().draw(this.getSite(), this.treeviewer.getTree().getItems(),isShowCallerType);

	}

	public void handleAddRLAnnotation() {
		Object[] checkedItems = this.treeviewer.getCheckedElements();
		for (int i = 0; i < checkedItems.length; i++) {
			logger.debug("###===>" + checkedItems[i]);
		}

	}

	/**
	 * 利用GEF列印產生出來的循序圖
	 * @param editor
	 */
	public void printSequenceDiagram(IWorkbenchPart editor){		
		PrintAction printAction = new PrintAction(editor);
		printAction.run();
	}
	
	public void saveSequenceDiagram(IWorkbenchPart editor){
		SaveAsImageAction saveSDAction = new SaveAsImageAction((GraphicalViewer)editor);
		saveSDAction.run();
	}
	
	public void handleChangeShowView() {
		this.updateView(this.lastMethod);
	}

	// *************************************************************************
	// Setter / Getter
	// *************************************************************************

	public void setShowType(boolean showCaller) {
		this.showCaller = showCaller;
	}

	public boolean isShowCallerType() {
		return this.showCaller;
	}

}
