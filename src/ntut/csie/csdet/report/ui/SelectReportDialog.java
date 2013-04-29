package ntut.csie.csdet.report.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import ntut.csie.csdet.report.ReportModel;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


/**
 * 
 * @author Shiau
 */
public class SelectReportDialog  extends Dialog {
	//Delete代碼
	private final int DELETE_SELECTION = 3337;

	private Combo projectCombo;
	private Table reportTable;
	
	//使用者所選擇的Report Path
	private String filePath;
	//全部的Project
	private List<String> projectList = new ArrayList<String>();
	//特定專案底下內全部的Report Path
	private List<File> fileList = new ArrayList<File>();
	
	private ResourceBundle resource = ResourceBundle.getBundle("robusta", new Locale("en", "US"));
	
	private ReportModel model = new ReportModel();
	
	public SelectReportDialog(Shell parentShell, List<String> projctList, ReportModel data) {
		super(parentShell);

		this.projectList = projctList;
		filePath = "";
		this.model = data;
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(resource.getString("report.list"));
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);		
		composite.setLayout(new GridLayout(1,false));

		Label label = new Label(composite,SWT.None);
		label.setText(resource.getString("project.name"));

		//建置Table
		buildTable(composite);

		return composite;
	}
	
	/**
	 * 建置Table
	 * @param composite
	 */
	private void buildTable(Composite composite) {
		///ProjectCombo///
		projectCombo = new Combo(composite,SWT.READ_ONLY);
		projectCombo.setLayoutData(new GridData());
		projectCombo.addSelectionListener(new SelectionAdapter(){		
			public void widgetSelected(SelectionEvent e) {
				updateTable();
			}
		});

		//把Project名稱加入至ProjectCombo
		for (String projectName : projectList)
			projectCombo.add(projectName);

		//ProjectList預設為第一個
		if (projectList.size() >= 0)
			projectCombo.select(0);

		///Report List Table///
		reportTable = new Table(composite, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		reportTable.setLinesVisible(true);
	    reportTable.setHeaderVisible(true);

	    //table column 1 title
	    TableColumn column1 = new TableColumn(reportTable, SWT.NONE);
	    column1.setText(resource.getString("time"));
	    column1.setWidth(200);
	    
	    //table column 2 title
	    TableColumn column2 = new TableColumn(reportTable, SWT.NONE);
	    column2.setText("DescriptionContent");
	    column2.setWidth(200);

	    //set layout
	    GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
	    data.heightHint = 200;
	    data.widthHint = 300;
	    reportTable.setLayoutData(data);
	    
	    //add listener
	    reportTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				//若點選到Item兩下，如同按下確定
				if (reportTable.getSelectionIndex() != -1)
					okPressed();
			}
	    });

	    //更新Table內容
	    updateTable();
	}
	/**
	 * 更新Table
	 */
	private void updateTable() {	
		//clear old item
		reportTable.clearAll();
		reportTable.setItemCount(0);
		fileList.clear();
		
		//set Report List
		getFileList();

		for (File file : fileList) {
			String fileName = file.getName();
			//在Table內加入新的Item
			TableItem tableItem = new TableItem(reportTable, SWT.NONE);
			//取得報表名稱的日期
			int index = fileName.indexOf("_");		
			Date date = new Date(Long.parseLong(fileName.substring(0,index)));
			tableItem.setText(date.toString());		
			
			//取得註解的內容

			BufferedReader br = null;
			FileReader fr = null;
			//FileWriter fw = null;
			String getStr = null;
			StringBuffer SB = null;
			
			//String secondfile = file.getName()+"a";			
			try {
				//System.out.println(file.getPath());
				fr = new FileReader(file.getPath());
				br = new BufferedReader(fr);
				SB = new StringBuffer();
				boolean isDesExist = false;			
				
				//取每一行文字
				while((getStr = br.readLine()) != null){
					SB.append(getStr);
					System.out.println(getStr);
				}				
				
				fr.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	

			//* readLine 迴圈 備份 	*/
//			while((getStr = br.readLine()) != null){
//				//fw = new FileWriter(file);
//				//是描述的註解 
//				if(getStr.equals("<!--Des")){
//					//String editStr = "helloEdit";
//					//fw.write(getStr);
//					//br.readLine();
//					//fw.write(editStr);
//					
//					isDesExist = true;
//				}
//				//不是
//				else{						
//					//fw.write(getStr);
//				}
//				System.out.println(getStr);
//			}		
//			if(!isDesExist){
//				//fw.write("<!--Des\n\n-->");			
//			}							
			
			tableItem.setText(1,"ADS");		
		}
	}		
	/**
	 * 取得Project內的Report資訊
	 * @return
	 */
	public void getFileList() {
		//取得使用者使選擇的Project Name
		String projectName = projectList.get(projectCombo.getSelectionIndex());
		//取得WorkSpace
		String workPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		
		//Report目錄
		File directory = new File(workPath + "/" + projectName + "/" + projectName + "_Report/");
		
		//取得目錄內每一個資料夾路徑
		File[] allFolder = directory.listFiles();
		
		//若Project未建立Report路徑
		if (allFolder == null)
			return;

		for (File folder: allFolder) {
			if (folder.isDirectory()) {
				//取得副檔名為.html的檔案
				File[] files = folder.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".html");
					}
				});
				//把Report資訊記錄
				for (File file : files)
					fileList.add(file);
			}
		}
	}
	
	/**
	 * 定義按鍵
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
	    createButton(parent, DELETE_SELECTION, resource.getString("delete"), true);
	}
	
	/**
	 * 若按下Delete，刪除使用者所選擇之Report
	 */
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		//若按下Delete
		if(buttonId == DELETE_SELECTION){ //delete by selection
			int[] selectIdx = reportTable.getSelectionIndices();
			
			//刪除所有選取的Report
			if (selectIdx.length != 0) {
				for (int index : selectIdx) {
					//取得Report資料夾
					File reportFolder = fileList.get(index).getParentFile();

					//刪除資料夾內所有檔案
					File[] allFile = reportFolder.listFiles();
					for (File file: allFile)
						file.delete();

					//刪除資料夾
					reportFolder.delete();
				}
				updateTable();
			}
		}
	}

	/**
	 * 若按下OK鍵，記錄使用者所選取的Report路徑
	 */
	protected void okPressed() {	
		int index = reportTable.getSelectionIndex();
		//html file 's path
		//System.out.println(fileList.get(index).getAbsolutePath());
		if (index != -1)
			filePath = fileList.get(index).getAbsolutePath();
		
		
//		try {
//			System.out.println(fileList.get(index).getAbsolutePath());
//			//new changeHtml(fileList.get(index).getAbsolutePath()).change();
//			
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
//		
		
		super.okPressed(); 
	}
	
	/**
	 * 取得使用者所選取的Report名稱
	 * @return
	 */
	public String getReportPath() {
		return filePath;
	}
}
