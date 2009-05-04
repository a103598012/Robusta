package ntut.csie.rleht.caller;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

//����Sequence Diagram���]�w����
public class SDDialog extends Dialog
{
	//�O�_�n���package��RadioButton
	private Button packageRadBtn;
	//�u���class��RadioButton	
	private Button classRadBtn;
	//�O�_�n����ܩҦ���package�W�r��RadioButton
	private Button allRadBtn;
	//���Package�ƥت����s(�ѳ̤W�h���U��)
	private Button topDownRadBtn;
	private Spinner topDownSpinner;
	//���Package�ƥت����s(�ѳ̤U�h���W��)
	private Button buttonUpRadBtn;
	private Spinner buttonUpSpinner;
	//�O�_������package
	private boolean isPackage;
	//�O�_��ܥ���package�����
	private boolean isShowAll;
	//�O�_��ܥѤW��U�A�_�h�ѤW��W
	private boolean isTopDown;
	//�����package�ƪ��ܼ�
	private int packageCount;
	//�O�_���Cancel
	private boolean isCancel = false;

	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public SDDialog(Shell parentShell)
	{
		super(parentShell);
	}
	
	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);

		packageRadBtn = new Button(container, SWT.RADIO);
		packageRadBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		packageRadBtn.setText("���Package�MClass�W�١G");
		new Label(container, SWT.NONE);

		final Composite composite = new Composite(container, SWT.NONE);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		composite.setLayout(gridLayout_1);

		allRadBtn = new Button(composite, SWT.RADIO);
		allRadBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		allRadBtn.setText("������Package�W�ٳ����");

		topDownRadBtn = new Button(composite, SWT.RADIO);
		topDownRadBtn.setText("���Package�ƥ�(�ѳ̤W�h���U��)");
		topDownSpinner = new Spinner(composite, SWT.BORDER);
		
		buttonUpRadBtn = new Button(composite, SWT.RADIO);
		buttonUpRadBtn.setText("���Package�ƥ�(�ѳ̤U�h���W��)");
		buttonUpSpinner = new Spinner(composite, SWT.BORDER);

		classRadBtn = new Button(container, SWT.RADIO);
		classRadBtn.setLayoutData(new GridData());
		classRadBtn.setText("�u���Class�W��");
		new Label(container, SWT.NONE);
		
		//�Y���U���package���s�A����ܩҦ�package�M���package�ƫ��s�]�����
		packageRadBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(final SelectionEvent e)
			{
				//��package���s�]��true
				setPackageRadBtn(true);
				//�Y�O��ܭn���package�B��ܨ���package�ƥءA��ƦrSpinner���}
				if (topDownRadBtn.getSelection())
					topDownSpinner.setEnabled(true);
				if (buttonUpRadBtn.getSelection())
					buttonUpSpinner.setEnabled(true);
			}
		});
		//�Y���U�u���class���s�A����ܩҦ�package�M���package�ƫ��s�]�������
		classRadBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(final SelectionEvent e)
			{
				//��package���s�]��false
				setPackageRadBtn(false);
				topDownSpinner.setEnabled(false);
				buttonUpSpinner.setEnabled(false);
			}
		});
		//�Y���U��ܩҦ�package�A����package�ƪ�Spinner�ܦ������
		allRadBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(final SelectionEvent e)
			{
				topDownSpinner.setEnabled(false);
				buttonUpSpinner.setEnabled(false);
			}
		});
		//�Y���UTopDown���s�A��TopDown��Spinner�]���i��BButtonUp��Spinner�]�������
		topDownRadBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(final SelectionEvent e)
			{
				topDownSpinner.setEnabled(true);
				buttonUpSpinner.setEnabled(false);
			}
		});
		//�Y���UButtonUp���s�A��ButtonUp��Spinner�]���i��BTopDown��Spinner�]�������
		buttonUpRadBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e)
			{
				topDownSpinner.setEnabled(false);
				buttonUpSpinner.setEnabled(true);
			}
		});
		
		//��l���A
		packageRadBtn.setSelection(true);
		allRadBtn.setSelection(true);
		topDownSpinner.setEnabled(false);
		buttonUpSpinner.setEnabled(false);
		
		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,true);
		createButton(parent, IDialogConstants.CANCEL_ID,IDialogConstants.CANCEL_LABEL, false);
	}

	protected void buttonPress(int buttonId){
		super.buttonPressed(buttonId);		
	}

	@Override
	protected void okPressed()
	{
		// ....collect data here
		isPackage = packageRadBtn.getSelection();
		isShowAll = allRadBtn.getSelection();
		isTopDown = topDownRadBtn.getSelection();
		//packageCount�ѿ��TopDown��Button��Spinner�ӨM�w
		if (isTopDown)
			packageCount = topDownSpinner.getSelection();
		else
			packageCount = buttonUpSpinner.getSelection();
		super.okPressed();
	}
	@Override
	protected void cancelPressed()
	{
		//�Y���U������A�O���_��
		isCancel = true;
		super.cancelPressed();
	}
	
	//�ǥX�O�_Cancel
	public boolean getIsCancel(){
		return isCancel;
	}
	//�ǥX�O�_���"�n���package"
	public boolean getIsPackage(){
		return isPackage;
	}
	//�ǥX�O�_���"��Ҧ�package�����"
	public boolean getIsShowAll(){
		return isShowAll;
	}
	//�ǥX�O�_���"�ѤW���U���"
	public boolean getIsTopDown(){
		return isTopDown;
	}
	//�ǥX"�n���package�����h��"
	public int getPackageCount(){
			return packageCount;
	}
	//�]�wPackage�t�C���s��true��false
	private void setPackageRadBtn(boolean trueOrfalse) {
		allRadBtn.setEnabled(trueOrfalse);
		topDownRadBtn.setEnabled(trueOrfalse);
		buttonUpRadBtn.setEnabled(trueOrfalse);
	}
	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize()
	{
		return new Point(427, 210);
	}
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText("Sequence Diagram Model");
	}
}