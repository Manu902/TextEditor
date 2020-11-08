import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.*;
import javax.swing.border.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.text.*; // /Document is a interface.
import java.io.*;
import java.util.*;
import java.net.*;

public class EditorFrame extends JFrame implements Printable
{
	private boolean newFlag = false;
	private boolean openFlag = false;
	
	private Document document;
	private UndoHandler undoHandler = new UndoHandler();
	private UndoManager undoManager = new UndoManager();
	private UndoAction undoAction1 = null;
	private PrinterJob job = PrinterJob.getPrinterJob();
	
	private Integer zoomSize = 11;
	private int count = 0;
	private boolean zoomFlag = false;

	private JFrame fontFrame;
	private JLabel sampleLabel;
	
	private JButton okButton, cancelButton;
	private JFileChooser saveChooser;
	private boolean flag = false;
	
	private JComboBox fontFamily, fontStyle;
	private JComboBox<Integer> fontSize;
	
	private JMenuBar menubar;
	private JMenu file, edit, formate, view, help, zoomMenu;
	
	private JMenuItem newItem, newWindowItem, openItem, saveItem, saveAsItem, pageSetItem, printItem, exitItem;
	private JMenuItem undoItem, cutItem, copyItem, pasteItem, deleteItem, findItem, findNextItem,replaceItem, gotoItem, selectAllItem, timedateItem;
	private JMenuItem fontItem;
	private JCheckBox wordWrapItem;
	private JCheckBox statusBarItem;
	private JLabel show_L_C;
	private JPanel status_B_P;
	private JMenuItem viewHelpItem, sendFeedbackItem, aboutNotepadItem;
	private JTextArea textArea;
	
	private static JTextField findJtextFieldText;
	private static JDialog findDialog;
	private static String findWord;
	private static int findPos_Num;
	private static Object removeHighlighter;
	private FindFrameSetup findOb;
	
	public EditorFrame()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());   //  this line setup the look and feel
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		setSize(1000,520);
		setLocation(120,150);
		setTitle("Untitled - Notepad");
		Image icon = new ImageIcon("d:/Project/EditorIcon.png").getImage();
		setIconImage(icon);
		
		textArea = new JTextArea();
		JScrollPane scrollpane = new JScrollPane(textArea);
		scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollpane, BorderLayout.CENTER);
		
		textArea.addCaretListener(event ->
		{
			try
			{
				int caret = textArea.getCaretPosition();
				int line = textArea.getLineOfOffset(caret);
				int start = textArea.getLineStartOffset(line);
				int column = (caret-start)+1;
				line+=1;
				
				String l = String.valueOf(line);
				String c = String.valueOf(column);
				
				show_L_C.setText("  Ln: "+l+",  Col: "+c);
			}
			catch(BadLocationException e)
			{
				e.printStackTrace();
			}
		});
		
		// Create filemenu
		menubar = new JMenuBar(); 
		setJMenuBar(menubar);
	
		file = new JMenu("File");
		menubar.add(file);
		file.setMnemonic('F');
		
		Action newAction = new ItemAction("New");
		newItem = file.add(newAction);
		AcceleratorWork accelerator = new AcceleratorWork();
		ShortCutKeyWork.makeShortCut(file, newAction, "ctrl N");
		accelerator.acceleratorSet(newItem, "ctrl N");
		newItem.addActionListener(event -> newFile());
		
		Action newWinAction = new ItemAction("New Window");
		newWindowItem = file.add(newWinAction);
		ShortCutKeyWork.makeShortCut(file, newWinAction, "ctrl shift N");
		accelerator.acceleratorSet(newWindowItem, "ctrl shift N");
		newWindowItem.addActionListener(event -> newWindow());
		
		Action openAction = new ItemAction("Open");
		openItem = file.add(openAction);
		accelerator.acceleratorSet(openItem, "ctrl O");
		ShortCutKeyWork.makeShortCut(file, openAction, "ctrl O");
		openItem.addActionListener(event ->	
		{
			if(getTitle().equals("Untitled - Notepad") && !textArea.getText().isEmpty())
			{
				openFlag = true;
				saveDialogTest();
			}
			else
				openFile();
		});
		
		Action saveAction = new ItemAction("Save");
		saveItem = file.add(saveAction);
		accelerator.acceleratorSet(saveItem, "ctrl S");
		ShortCutKeyWork.makeShortCut(file, saveAction, "ctrl S");
		saveItem.addActionListener(event ->	saveFile());
	
		Action saveAsAction = new ItemAction("Save As");
		saveAsItem = file.add(saveAsAction);
		accelerator.acceleratorSet(saveAsItem, "ctrl shift S");
		ShortCutKeyWork.makeShortCut(file, saveAsAction, "ctrl shift S");
		file.addSeparator();
		saveAsItem.addActionListener(event -> saveAsFile());
		
		pageSetItem = file.add("PageSetup...");
		pageSetItem.addActionListener(event -> pageSetup());
		
		Action printAction = new ItemAction("Print...");
		printItem = file.add(printAction);
		accelerator.acceleratorSet(printItem, "ctrl P");
		ShortCutKeyWork.makeShortCut(file, printAction, "ctrl P");
		printItem.addActionListener(event -> printTextArea());
		file.addSeparator();
		
		exitItem = file.add("Exit");
		exitItem.addActionListener(event -> System.exit(0));
		
		// Create editmenu
		edit = new JMenu("Edit");
		menubar.add(edit);
		edit.setMnemonic('E');
		
		document = textArea.getDocument();
		document.addUndoableEditListener(undoHandler);
		undoAction1 = new UndoAction();
		undoItem = edit.add(undoAction1);
		accelerator.acceleratorSet(undoItem, "ctrl Z");
		ShortCutKeyWork.makeShortCut(edit, undoAction1, "ctrl Z");
		edit.addSeparator();
		
		cutItem = edit.add("Cut");
		accelerator.acceleratorSet(cutItem, "ctrl X");
		cutItem.addActionListener(event -> textArea.cut());
		
		copyItem = edit.add("Copy");
		accelerator.acceleratorSet(copyItem, "ctrl C");
		copyItem.addActionListener(event -> textArea.copy());
		
		pasteItem = edit.add("Paste");
		accelerator.acceleratorSet(pasteItem, "ctrl V");
		pasteItem.addActionListener(event -> textArea.paste());
		
		deleteItem = edit.add("Delete");
		accelerator.acceleratorSet(deleteItem, "DELETE");
		deleteItem.addActionListener(event -> textArea.replaceSelection(""));
		edit.addSeparator();
		
		Action findAction = new ItemAction("Find");
		findItem = edit.add(findAction);
		accelerator.acceleratorSet(findItem, "ctrl F");
		ShortCutKeyWork.makeShortCut(edit, findAction, "ctcl F");
		findItem.addActionListener(event -> 
		{	
			findOb = new FindFrameSetup(textArea, EditorFrame.this);
			findOb.findFrameSetup();
			findDialog.setVisible(true);
		});
		
		Action findNextAction = new ItemAction("Find Next");
		findNextItem = edit.add(findNextAction);
		accelerator.acceleratorSet(findNextItem, "F3");
		ShortCutKeyWork.makeShortCut(edit, findNextAction, "F3");
		findNextItem.addActionListener(event ->
		{
			findOb = new FindFrameSetup(textArea, EditorFrame.this);
			findOb.findFrameSetup();
			if(findWord!=null)
			{
				findOb.paintNext(textArea, findWord, findPos_Num, removeHighlighter);
			}
			else
			{
				findDialog.setVisible(true);
			}
			
		});
		
		Action replaceAction = new ItemAction("Replace...");
		replaceItem = edit.add(replaceAction);
		ShortCutKeyWork.makeShortCut(edit, replaceAction, "ctrl R");
		accelerator.acceleratorSet(replaceItem, "ctrl R");
		replaceItem.addActionListener(event ->
		{
			findOb =  new FindFrameSetup(textArea, EditorFrame.this);
			findOb.r_Make_Dialog();
		});
		
		Action gotoAction = new ItemAction("Go To...");
		gotoItem = edit.add(gotoAction);
		ShortCutKeyWork.makeShortCut(edit, gotoAction, "ctrl G");
		accelerator.acceleratorSet(gotoItem, "ctrl G");
		gotoItem.addActionListener(event -> gotoFrame());
		edit.addSeparator();
		
		Action selectAllAction = new ItemAction("Select All");
		selectAllItem = edit.add(selectAllAction);
		ShortCutKeyWork.makeShortCut(edit, selectAllAction, "ctrl A");
		accelerator.acceleratorSet(selectAllItem, "ctrl A");
		selectAllItem.addActionListener(event -> textArea.selectAll());
		
		Action timedateAction = new ItemAction("Time/Date");
		timedateItem = edit.add(timedateAction);
		ShortCutKeyWork.makeShortCut(edit, timedateAction, "F5");
		accelerator.acceleratorSet(timedateItem, "F5");
		timedateItem.addActionListener(event -> 
		{
			Date date  = new Date();
			String d = date.toString();
			textArea.append(d);
		});
		
		// formate menu
		formate = new JMenu("Format");
		menubar.add(formate);
		formate.setMnemonic('O');
		
		wordWrapItem = new JCheckBox("Word wrap");
		formate.add(wordWrapItem);
		wordWrapItem.addActionListener(event ->
		{
			if(wordWrapItem.isSelected())
				textArea.setLineWrap(true);
			else
				textArea.setLineWrap(false);
		});
		fontItem = formate.add("Font...");
		fontItem.addActionListener(event -> fontFrameSetup());
		
		// view menu
		view = new JMenu("View");
		menubar.add(view);
		view.setMnemonic('V');
		
		zoomMenu = new JMenu("Zoom");
		view.add(zoomMenu);
		Action zoomInAction = new ItemAction("Zoom In");
		JMenuItem zoomInItem = zoomMenu.add(zoomInAction);
		accelerator.acceleratorSet(zoomInItem, "ctrl L");
		ShortCutKeyWork.makeShortCut(zoomMenu, zoomInAction, "ctrl L");
		zoomInItem.addActionListener(event -> zoomInSet());
		
		Action zoomOutAction = new ItemAction("Zoom Out");
		JMenuItem zoomOutItem = zoomMenu.add(zoomOutAction);
		accelerator.acceleratorSet(zoomOutItem, "ctrl MINUS");
		ShortCutKeyWork.makeShortCut(zoomMenu, zoomOutAction, "ctrl MINUS");
		zoomOutItem.addActionListener(event -> zoomOutSet());
		
		Action reStoreDefaultAction = new ItemAction("Restore Default Zoom");
		JMenuItem reStoreDefaultItem = zoomMenu.add(reStoreDefaultAction);
		accelerator.acceleratorSet(reStoreDefaultItem, "ctrl 0");
		ShortCutKeyWork.makeShortCut(zoomMenu, reStoreDefaultAction, "ctrl 0");
		reStoreDefaultItem.addActionListener(event -> 
		{
			zoomRestoreSet();
		});
		
		createStatus_B();		// this method create the status bar
		statusBarItem = new JCheckBox("Status Bar");
		view.add(statusBarItem);
		statusBarItem.addActionListener(event ->
		{
			if(statusBarItem.isSelected())
				status_B_P.setVisible(true);
			else
				status_B_P.setVisible(false);
		});
		
		// help menu
		JMenu help = new JMenu("Help");
		menubar.add(help);
		help.setMnemonic('H');
		
		viewHelpItem = help.add("View Help");
		sendFeedbackItem = help.add("Send Feedback");
		sendFeedbackItem.addActionListener(event -> createFeed_B());
		sendFeedbackItem.setMnemonic('F');
		help.addSeparator();
		aboutNotepadItem = help.add("About Notepad");
		aboutNotepadItem.setMnemonic('A');
		aboutNotepadItem.addActionListener(event -> aboutNotepad());
	}
	
/*================================This mthod used in anoghter class====================================*/

	public JTextArea text_A_M()
	{
		return textArea;
	}
	
	public static void findCommonText(JTextField field, JDialog dialog)
	{
		findJtextFieldText = field;
		findDialog = dialog;
		//findWord = word;
	}
	public static void find_Text(String word)
	{
		findWord = word;
	}
	public static void find_Pos(int pos_Num, Object removeHigh)
	{
		findPos_Num = pos_Num;
		removeHighlighter = removeHigh;
	}
	
/*=====================================================================================================*/
	//@override
	public int print(Graphics g, PageFormat pf, int page) throws PrinterException
	{
		Graphics2D g2 = (Graphics2D) g;
		
		if(page> 0)
		{
			return NO_SUCH_PAGE;
		}
		
		// String textAreaString = textArea.getText();
		/*if(zoomFlag == true)
		{
			int i = 0;
			String fontName = (String) fontFamily.getSelectedItem();
			String font_S_Name = (String) fontStyle.getSelectedItem();
			Integer size = (Integer) fontSize.getSelectedItem();
			if(font_S_Name.equals("Plain"))
				i = 0;
			else if(font_S_Name.equals("Bold"))
				i = 1;
			else if(font_S_Name.equals("Italic"))
				i = 2;
			else
				i = 3;
			font = new Font(fontName, i, size);
			g2.setFont(font);
		}
		else
		{
			font = new Font("Consolas", Font.PLAIN, 11);
			g2.setFont(font);
			g2.setFont(font);
		}
		
		FontRenderContext context = g2.getFontRenderContext();
		Rectangle2D bounds = font.getStringBounds(textAreaString, context);
		
		double x = (getWidth() - bounds.getWidth()) /2;
		double y = (getHeight() - bounds.getHeight()) /2;
		
		double ascent = -bounds.getY();
		double baseY = y+ascent;
		g2.drawString(textAreaString,(int) x, (int) y);*/
		
		g2.translate(pf.getImageableX(), pf.getImageableY());
		textArea.paint(g2);
		
		return PAGE_EXISTS;
	}
	
/*===================================Methods for file menu=============================================*/
	// method for open new Window
	
	void newWindow()
	{
		int num = CountNum.t_C_N();
		num+= 1;
		CountNum.count_N(num);
		EventQueue.invokeLater(()->
		{
			EditorFrame frame = new EditorFrame();
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.setVisible(true);
			frame.addWindowListener(new WindowAdapter()       ///  use of anonimous class
			{
				public void windowClosing(WindowEvent event)
				{
					JFrame n_F = (JFrame) event.getSource();
					boolean flag = false;
					JTextArea text_A = null;
					Component[] comp = n_F.getContentPane().getComponents();
					for(int i = 0; i<comp.length; i++)
					{	
						if(comp[i] instanceof JScrollPane)
						{
							JScrollPane pane = (JScrollPane) comp[i];
							text_A = (JTextArea) pane.getViewport().getView();
						}
					}
					if(frame.getTitle().equals("Untitled - Notepad") && !text_A.getText().isEmpty())
					{
						frame.saveDialogTest(frame);
						flag = true;
					}
					
					if(!frame.getTitle().equals("Untitled - Notepad") && flag == false || frame.getTitle().equals("Untitled - Notepad") && text_A.getText().isEmpty())
					{
						int num = CountNum.t_C_N();
						if(num == 0)
							System.exit(0);
						else
						{
							num-= 1;
							CountNum.count_N(num);
							frame.dispose();
						}
					}
				}
			});
		});
	}

	// method for print text.
	void printTextArea()
	{
		job.setPrintable(EditorFrame.this);
		boolean doPrint = job.printDialog();
		
		if(doPrint)
		{
			try
			{
				job.print();
			}
			catch(PrinterException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	// method for pageSetup
	void pageSetup()
	{
		PageFormat pf = job.pageDialog(job.defaultPage());
	}
	
	// method for newFile
	int nunbs = 0;
	void newFile()
	{
		nunbs+=1; 
		if(getTitle().equals("Untitled - Notepad") && !textArea.getText().isEmpty())
		{
			newFlag = true;
			saveDialogTest();
		}
		else
		{
			setTitle("Untitled - Notepad");
			textArea.setText("");
			saveChooser = null;
			flag = false; 
		}
	}
	
	// method for openFile
	void openFile()
	{		
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		FileFilter filter = new FileNameExtensionFilter("Text Documents(*.txt)", "txt");
		chooser.setFileFilter(filter);
		int result = chooser.showOpenDialog(EditorFrame.this);
		if(result == JFileChooser.APPROVE_OPTION)
		{
			textArea.setText("");
			int i = 0;
			File file = chooser.getSelectedFile();
			String name = file.getName();
			int l_N = name.lastIndexOf(".");
			String title = name.substring(0, l_N);
			setTitle(title+" - Notepad");
			try(FileInputStream fis = new FileInputStream(file))
			{
				while((i=fis.read()) != -1)
				{
					textArea.append(String.valueOf((char)i));
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	// method for saveFile
	void saveFile()
	{
		int result;
		
		if(flag == false)
		{
			saveChooser = new JFileChooser();
			saveChooser.setCurrentDirectory(new File("."));
			FileFilter filter = new FileNameExtensionFilter("Text Documents(*.txt)", "txt");
			saveChooser.setFileFilter(filter);
			result = saveChooser.showSaveDialog(EditorFrame.this);
			if(result == 0)
				flag = true;
		}
		
		if(flag == true)
		{
			File pathName = saveChooser.getSelectedFile();
			String textAreaString = textArea.getText();
			String name = pathName.getName();
			String f_Name = pathName.toString();
			int i = name.lastIndexOf(".");
			if(i != -1)
			{
				String title = name.substring(0, i);
				setTitle(title+" - Notepad");
			}
			else
			{
				setTitle(name+" - Notepad");
				f_Name = f_Name+".txt";
			}
			try(FileOutputStream fos = new FileOutputStream(f_Name))
			{
				byte[] b = textAreaString.getBytes();
				fos.write(b);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	// method for saveAsFile
	void saveAsFile()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		FileFilter filter = new FileNameExtensionFilter("Text Documents(*.txt)", "txt");
		chooser.setFileFilter(filter);
		int result = chooser.showSaveDialog(EditorFrame.this);
		if(result == JFileChooser.APPROVE_OPTION)
		{
			File pathName = chooser.getSelectedFile();
			String name = pathName.getName();
			String f_Name = pathName.toString();
			int i = name.lastIndexOf(".");
			if(i != -1)
			{
				String title = name.substring(0, i);
				setTitle(title+" - Notepad");
			}
			else
			{
				setTitle(name+" - Notepad");
				f_Name = f_Name+".txt";
			}
			try(FileOutputStream fos = new FileOutputStream(f_Name))
			{
				String textString = textArea.getText();
				byte[] b = textString.getBytes();
				fos.write(b);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
/*======================================Methods for font work==========================================*/
	// font frame
	void fontFrameSetup()
	{
		fontFrame = new JFrame();
		fontFrame.setSize(500,450);
		fontFrame.setTitle("Font");
		fontFrame.setLocationRelativeTo(EditorFrame.this);
		fontFrame.setLayout(null);
		
		JLabel fontLabel = new JLabel("Font:");
		fontLabel.setBounds(50, 35, 30, 10);
		fontFrame.add(fontLabel);
		
		JLabel styleLabel = new JLabel("Font Style:");
		styleLabel.setBounds(270, 35, 100, 10);
		fontFrame.add(styleLabel);
		
		JLabel sizeLabel = new JLabel("Size:");
		sizeLabel.setBounds(390, 35, 30, 10);
		fontFrame.add(sizeLabel);
		
		String[] fontFamilyName = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		fontFamily = new JComboBox(fontFamilyName);
		fontFamily.setBounds(50, 50, 200, 25);
		fontFrame.add(fontFamily);
		
		String[] fontStyleName = {"Plain", "Bold", "Italic", "Bold+Italic"};
		fontStyle = new JComboBox(fontStyleName);
		fontStyle.setBounds(270, 50, 100, 25);
		fontFrame.add(fontStyle);
		
		Integer[] num = new Integer[33];
		int n = 8;
		for(int i = 0; i<33; i++)
		{
			num[i] = n;
			n+=2;
		}
		fontSize = new JComboBox(num);
		fontSize.setBounds(390, 50, 80, 25);
		fontFrame.add(fontSize);
		
		sampleLabel = new JLabel("AaBbYyZz", SwingConstants.CENTER);
		sampleLabel.setBounds(260, 200, 220, 80);
		
		Border etched = BorderFactory.createEtchedBorder();
		Border b = BorderFactory.createTitledBorder(etched, "Sample");
		sampleLabel.setBorder(b);
		fontFrame.add(sampleLabel);
		
		okButton = new JButton("Ok");
		okButton.setBounds(300, 350, 80, 30);
		fontFrame.getRootPane().setDefaultButton(okButton);
		okButton.setSelected(true);
		okButton.addActionListener(event -> fontSetup());
		fontFrame.add(okButton);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setBounds(390, 350, 80, 30);
		cancelButton.addActionListener(event -> fontFrame.setVisible(false));
		fontFrame.add(cancelButton);
	
		fontFamily.addActionListener(event -> fontSetup_sampleLabel());
		fontStyle.addActionListener(event -> fontSetup_sampleLabel());
		fontSize.addActionListener(event -> fontSetup_sampleLabel());
		
		fontFrame.setVisible(true);
	}	
	// method for fonSetp
	void fontSetup()
	{
		zoomFlag = true;
		
		String fontName = (String) fontFamily.getSelectedItem();
		String font_S_Name = (String) fontStyle.getSelectedItem();
		int i = 0;
		if(font_S_Name.equals("Plain"))
			i = 0;
		else if(font_S_Name.equals("Bold"))
			i = 1;
		else if(font_S_Name.equals("Italic"))
			i = 2;
		else
			i = 3;
		
		Integer size = (Integer) fontSize.getSelectedItem();
		Font font = new Font(fontName, i, size);
		textArea.setFont(font);
		fontFrame.setVisible(false);
		
	}
	// method for setFont on sampleLable
	void fontSetup_sampleLabel()
	{
			String fontName = (String) fontFamily.getSelectedItem();
			String font_S_Name = (String) fontStyle.getSelectedItem();
			int i = 0;
			if(font_S_Name.equals("Plain"))
				i = 0;
			else if(font_S_Name.equals("Bold"))
				i = 1;
			else if(font_S_Name.equals("Italic"))
				i = 2;
			else
				i = 3;
			
			Integer size = (Integer) fontSize.getSelectedItem();
			Font font = new Font(fontName, i, size);
			sampleLabel.setFont(font);
	}
/*========================================MakeGotoFrame and its method=================================*/	
	// gotoFrame
	void gotoFrame()
	{
		JDialog dialog = new JDialog(EditorFrame.this, "Go To Line", true);
		dialog.setSize(300,140);
		dialog.setLayout(null);
		dialog.setLocation(135,210);
		
		JLabel label = new JLabel("Line number:");
		label.setBounds(10, 10, 100, 25);
		dialog.add(label);
		
		JTextField gotoTextField = new JTextField();
		gotoTextField.setBounds(10, 37, 260, 20);
		gotoTextField.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		dialog.add(gotoTextField);
		
		JButton gotoButton = new JButton("Go To");
		gotoButton.setBounds(100, 65, 80, 25);
		gotoButton.setSelected(true);
		dialog.getRootPane().setDefaultButton(gotoButton);
		gotoButton.addActionListener(event -> findLine(gotoTextField, dialog));
		dialog.add(gotoButton);
		
		JButton cancel = new JButton("Cancel");
		cancel.setBounds(190, 65, 80, 25);
		cancel.addActionListener(event -> dialog.dispose());
		dialog.add(cancel);
		
		dialog.setVisible(true);
	}
	
	void findLine(JTextField tf, JDialog f)
	{
		boolean flag = false;
		if(!tf.getText().isEmpty())
		{
			int l_No = Integer.parseInt(tf.getText());
			int l_Numbers = textArea.getLineCount();
			for(int i = 1; i<=l_Numbers; i++)
			{
				if(i == l_No)
				{
					textArea.setCaretPosition(textArea.getDocument().getDefaultRootElement().getElement(l_No-1).getStartOffset());
					textArea.requestFocusInWindow();
					f.dispose();
					flag = true;
					break;
				}
			}
			if(flag == false)
				JOptionPane.showMessageDialog(EditorFrame.this, "The line number can not find in the total number of lines.");
	
		}
		else
			JOptionPane.showMessageDialog(EditorFrame.this, "The line number can not find in the total number of lines.");
	}
	
/*===================================Methods for setZoom work=========================================*/
	// method for zoomInWork
	void zoomInSet()
	{
		String fontName = "Manu";
		boolean flag = false;
		int i = 0;
		
		if(zoomFlag == true)
		{
			fontName = (String) fontFamily.getSelectedItem();
			String font_S_Name = (String) fontStyle.getSelectedItem();
			if(font_S_Name.equals("Plain"))
				i = 0;
			else if(font_S_Name.equals("Bold"))
				i = 1;
			else if(font_S_Name.equals("Italic"))
				i = 2;
			else
				i = 3;
			flag = true;
			count+=1;
		}
		
		if(count == 1)
			zoomSize = (Integer) fontSize.getSelectedItem();
			
		zoomSize+= 4;
		if(flag == true)
		{
			Font font = new Font(fontName, i, zoomSize);
			textArea.setFont(font);
		}
		else
		{	
			Font font = new Font("Consolas", Font.PLAIN, zoomSize);
			textArea.setFont(font);
		}
		
	}
	
	// method for zoomOutSet
	void zoomOutSet()
	{
		if(zoomFlag == true)
		{
			String fontName = (String) fontFamily.getSelectedItem();
			String font_S_Name = (String) fontStyle.getSelectedItem();
			int i = 0;
			if(font_S_Name.equals("Plain"))
				i = 0;
			else if(font_S_Name.equals("Bold"))
				i = 1;
			else if(font_S_Name.equals("Italic"))
				i = 2;
			else
				i = 3;
			
			if(zoomSize>11)
				zoomSize -= 4;
			Font font = new Font(fontName, i, zoomSize);
			textArea.setFont(font);
		}
		else
		{
			if(zoomSize>11)
				zoomSize -= 4;
			Font font = new Font("Consolas", Font.PLAIN, zoomSize);
			textArea.setFont(font);
		}
	}
	
	// method for zoomRestoreSet
	void zoomRestoreSet()
	{
		if(zoomFlag == true)
		{
			String fontName = (String) fontFamily.getSelectedItem();
			String font_S_Name = (String) fontStyle.getSelectedItem();
			int i = 0;
			if(font_S_Name.equals("Plain"))
				i = 0;
			else if(font_S_Name.equals("Bold"))
				i = 1;
			else if(font_S_Name.equals("Italic"))
				i = 2;
			else
				i = 3;
			
			Integer size = (Integer) fontSize.getSelectedItem();
			Font font = new Font(fontName, i, size);
			textArea.setFont(font);
			zoomSize = size;
		}
		else
		{
			Font font = new Font("Consolas", Font.PLAIN, 11);
			textArea.setFont(font);
			zoomSize = 11;
		}
	}
	
/*=====================================================================================================*/	
	// Method for create status bar
	void createStatus_B()
	{
		status_B_P = new JPanel();
		status_B_P.setLayout(new GridLayout(1, 5));
		
		JLabel l = new JLabel("          ");
		l.setBorder(BorderFactory.createEtchedBorder());
		status_B_P.add(l);
	
		show_L_C = new JLabel("  Ln: 1  Col: 1  ");
		show_L_C.setBorder(BorderFactory.createEtchedBorder());
		status_B_P.add(show_L_C);

		JLabel l3 = new JLabel("100%");
		l3.setBorder(BorderFactory.createEtchedBorder());
		status_B_P.add(l3);
		
		JLabel l4 = new JLabel("Windows(CRLF)");
		l4.setBorder(BorderFactory.createEtchedBorder());
		status_B_P.add(l4);
		
		JLabel l5 = new JLabel("UTF-8");
		l5.setBorder(BorderFactory.createEtchedBorder());
		status_B_P.add(l5);
		
		add(status_B_P, BorderLayout.SOUTH);
		status_B_P.setVisible(false);
	}
	
	
	void print(int  value)
	{
		System.out.println(value);
	}

/*=========================Method for(open dialog) ask user save or not================================*/	
	void saveDialogTest()
	{	
		JDialog saveCheckDialog = new JDialog(EditorFrame.this, "Notepad", true);
		saveCheckDialog.setSize(350, 150);
		saveCheckDialog.setLocationRelativeTo(EditorFrame.this);
		saveCheckDialog.setLayout(null);
		
		Font font = new Font("Arial", Font.BOLD,15);
		JLabel label = new JLabel();
		label.setFont(font);
		label.setForeground(Color.BLUE);
		label.setText("  Do you want save changes Untiled?");
		label.setBounds(30,30, 300, 15);
		saveCheckDialog.add(label);
		
		JButton saveButton = new JButton("Save");
		saveButton.setBounds(75,80,60,25);
		saveButton.addActionListener(event ->
		{
			if(newFlag == true || openFlag == true)
			{
				saveCheckDialog.dispose();
				saveFile();
				newFlag = false;
				openFlag = false;
			}	
			else
			{
				saveFile();
				setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				saveCheckDialog.dispose();
				newFlag = false;
			}
		});
		saveCheckDialog.add(saveButton);
		JButton notSaveButton = new JButton("Don't Save");
		notSaveButton.setBounds(140, 80, 100, 25);
		notSaveButton.addActionListener(event -> 
		{
			if(newFlag == true)
			{
				textArea.setText("");
				saveCheckDialog.dispose();
				newFlag = false;
			}
			else if(openFlag == true)
			{
				saveCheckDialog.dispose();
				openFile();
				openFlag = false;
			}
			else
			{
				int num = CountNum.t_C_N();
				if(num == 0)
					System.exit(0);
				else
				{
					num-= 1;
					CountNum.count_N(num);
					dispose();
				}
			}
		});
		saveCheckDialog.add(notSaveButton);
		
		JButton cancel_Button = new JButton("Cancel");
		cancel_Button.addActionListener(event ->
		{
			if(newFlag == true || openFlag == true)
			{
				saveCheckDialog.dispose();
				newFlag = false;
				openFlag = false;
			}
			else
			{
				setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				saveCheckDialog.dispose();
				newFlag = false;
			}
		});
		cancel_Button.setBounds(245,80,80,25);
		saveCheckDialog.add(cancel_Button);

		saveCheckDialog.setVisible(true);
	}

	// method overloading 
	void saveDialogTest(JFrame frame)
	{
		JDialog saveCheckDialog = new JDialog(EditorFrame.this, "Notepad", true);
		saveCheckDialog.setSize(350, 150);
		saveCheckDialog.setLocationRelativeTo(EditorFrame.this);
		saveCheckDialog.setLayout(null);
		
		Font font = new Font("Arial", Font.BOLD,15);
		JLabel label = new JLabel();
		label.setFont(font);
		label.setForeground(Color.BLUE);
		label.setText("  Do you want save changes Untiled?");
		label.setBounds(30,30, 300, 15);
		saveCheckDialog.add(label);
		
		JButton saveButton = new JButton("Save");
		saveButton.setBounds(75,80,60,25);
		saveButton.addActionListener(event ->
		{
			saveFile();
			saveCheckDialog.dispose();
				
		});
		saveCheckDialog.add(saveButton);
		JButton notSaveButton = new JButton("Don't Save");
		notSaveButton.setBounds(140, 80, 100, 25);
		notSaveButton.addActionListener(event -> 
		{
			saveCheckDialog.dispose();
			int num = CountNum.t_C_N();
			if(num == 0)
				System.exit(0);
			else
			{
				num-= 1;
				CountNum.count_N(num);
				frame.dispose();
			}	
		});
		saveCheckDialog.add(notSaveButton);
		
		JButton cancel_Button = new JButton("Cancel");
		cancel_Button.addActionListener(event ->
		{
			saveCheckDialog.dispose();
		});
		cancel_Button.setBounds(245,80,80,25);
		saveCheckDialog.add(cancel_Button);

		saveCheckDialog.setVisible(true);
	}
	
/*===============================inner classes for set undo===========================================*/
	class UndoHandler implements UndoableEditListener
	{
		public void undoableEditHappened(UndoableEditEvent event)
		{
			undoManager.addEdit(event.getEdit());
			undoAction1.update();
		}
	}

	class UndoAction extends AbstractAction
	{
		public UndoAction()
		{
			super("Undo");
			setEnabled(false);
		}
		
		public void actionPerformed(ActionEvent event)
		{
			try
			{
				undoManager.undo();
			}
			catch(CannotUndoException e)
			{
				e.printStackTrace();
			}
			update();
		}
	
		protected void update()
		{
			if(undoManager.canUndo())
			{
				setEnabled(true);
				putValue(Action.NAME, "Undo");
			}
			else
			{
				setEnabled(false);
				putValue(Action.NAME, "Undo");
			}
		}
		
	}

/*==================================== methods for help menu===========================================*/
	// method for aboutNotepad
	void aboutNotepad()
	{
		JDialog aboutDialog = new JDialog(EditorFrame.this, "About Notepad", true);
		aboutDialog.setSize(450,350);
		aboutDialog.setLocationRelativeTo(EditorFrame.this);
		aboutDialog.setLayout(null);
		JLabel l = new JLabel("Created By");
		l.setFont(new Font("SandSeriff", Font.BOLD, 35));
		l.setForeground(Color.RED);
		l.setBounds(130, 50, 220, 40);
		aboutDialog.add(l);
		
		JLabel l1 = new JLabel("Manu verma");
		l1.setFont(new Font("SandSeriff", Font.BOLD+Font.ITALIC, 25));
		l1.setForeground(Color.BLUE);
		l1.setBounds(150, 100, 150, 30);
		aboutDialog.add(l1);
		
		JLabel l2 = new JLabel("Date :");
		l2.setFont(new Font("SandSeriff", Font.BOLD, 25));
		l2.setForeground(Color.GRAY);
		l2.setBounds(30, 250, 100, 30);
		aboutDialog.add(l2);
	
		JLabel l3 = new JLabel("25 Oct 2020");
		l3.setFont(new Font("SandSeriff", Font.BOLD, 25));
		l3.setForeground(Color.PINK);
		l3.setBounds(120, 250, 200, 30);
		aboutDialog.add(l3);
		
		JButton ok = new JButton("Ok");
		ok.setBounds(340, 275, 80, 25);
		aboutDialog.getRootPane().setDefaultButton(ok);
		ok.addActionListener(event -> aboutDialog.setVisible(false));
		aboutDialog.add(ok);
		
		aboutDialog.setVisible(true);
	}
	
	// method for sendFeedback
	void createFeed_B()
	{
		JDialog dialog = new JDialog(EditorFrame.this, "Feedback Hub", true);
		dialog.setSize(450, 350);
		dialog.setLayout(null);
		dialog.setLocationRelativeTo(EditorFrame.this);
		
		JLabel l = new JLabel("Please send Feedback");
		l.setFont(new Font("SandSeriff", Font.BOLD, 25));
		l.setForeground(Color.RED);
		l.setBounds(80, 50, 300, 25);
		dialog.add(l);
		
		JLabel l1 = new JLabel("Gmail  :");
		l1.setFont(new Font("SandSeriff", Font.BOLD, 22));
		l1.setForeground(Color.BLUE);
		l1.setBounds(25, 130, 100, 22);
		dialog.add(l1);
		
		JLabel l2 = new JLabel("in  :");
		l2.setFont(new Font("SandSeriff", Font.BOLD, 25));
		l2.setForeground(Color.BLUE);
		l2.setBounds(55, 190, 100, 25);
		dialog.add(l2);
		
		JLabel gm_B = new JLabel("manu277503@gmail.com");
		gm_B.setFont(new Font("SandSeriff", Font.BOLD, 15));
		gm_B.setForeground(Color.BLUE.darker());
		gm_B.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		gm_B.setBounds(125, 130, 200, 22);
		
		dialog.add(gm_B);
		
		JLabel in_B = new JLabel("https://www.linkedin.com/in/manu-verma-a449921b3");
		in_B.setForeground(Color.BLUE.darker());
		in_B.setFont(new Font("SandSeriff", Font.BOLD, 12));
		in_B.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		in_B.setBounds(125, 192, 300, 22);
		in_B.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				try
				{
					Desktop.getDesktop().browse(new URI("https://www.linkedin.com/in/manu-verma-a449921b3"));
				}
				catch(URISyntaxException | IOException ex)
				{
					ex.printStackTrace();
				}
			}
			
			public void mouseEntered(MouseEvent e)
			{
				in_B.setForeground(Color.RED);
			}
			
			public void mouseExited(MouseEvent e)
			{
				in_B.setForeground(Color.BLUE.darker());
			}
		});
		dialog.add(in_B);
		
		dialog.setVisible(true);
	}
}

/*====================================classes for set accelerator,action,shortcut======================*/
class ItemAction extends AbstractAction
{	
	public ItemAction(String name)
	{
		putValue(Action.NAME, name);
	}
	public void actionPerformed(ActionEvent event)
	{
		
	}
}

class AcceleratorWork
{
	public void acceleratorSet(JMenuItem item, String keystroke)
	{
		item.setAccelerator(KeyStroke.getKeyStroke(keystroke));
	}
}

class ShortCutKeyWork
{
	public static void makeShortCut(JMenu menu, Action action,  String keystroke)
	{
		InputMap imap = menu.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		imap.put(KeyStroke.getKeyStroke(keystroke), "discription");
		
		ActionMap amap = menu.getActionMap();
		amap.put("discription", action);
	}
}

/* ================================== classes for find text ==========================================*/
class MyHighliter extends DefaultHighlighter.DefaultHighlightPainter
{
	public MyHighliter(Color color)
	{
		super(color);
	}
}

class FindFrameSetup extends MouseAdapter
{
	Highlighter.HighlightPainter  myHighlightPainter = new MyHighliter(Color.BLUE);
	int pos = 0;
	int rePos = 0;
	
	int count = 0;
	String firstText;
	JCheckBox wrap_ar;
	boolean firstFlag = true;
	boolean c_M_Flag = false;
	JTextArea textArea;
	
	JFrame editorFrame;
	Object remove_highliter;
	Highlighter highlter;
	
	JTextField f_textField ;
	JDialog findDialog;
	
	int p_B_Last = 0;
	int h_N_p = 0;
	int repos_Count = 1;
	JCheckBox r_wrap_ar;
	boolean replaceFlag = false;
	String replaceText;
	boolean r_One_Flag = false;
	boolean r_All_Flag = false;
	boolean r_All_In_Flag = false;
	
	public FindFrameSetup(JTextArea textArea, JFrame editorFrame)
	{
		this.textArea = textArea;
		this.editorFrame = editorFrame;
		textArea.addMouseListener(this);
	}
	
	void findFrameSetup()
	{
		replaceFlag = false;
		
		findDialog = new JDialog(editorFrame, "Find", false);
		findDialog.setSize(385, 180);
		findDialog.setLocation(140,350);
		findDialog.setLayout(null);
		
		wrap_ar = new JCheckBox("Wrap around");
		wrap_ar.setSelected(true);
		wrap_ar.setBounds(6, 90, 100, 25);
		findDialog.add(wrap_ar);
		
		JLabel findLabel = new JLabel("Find what:");
		findLabel.setBounds(10, 10, 60, 25);
		findDialog.add(findLabel);
		
		JTextField f_textField = new JTextField();
		f_textField.setBounds(75, 10, 200, 25);
		f_textField.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		findDialog.add(f_textField);
		
		JButton findNext = new JButton("Find Next");
		findNext.setSelected(true);
		findDialog.getRootPane().setDefaultButton(findNext);
		findNext.setBounds(285, 10, 80, 25);
		findNext.addActionListener(event ->
		{
			c_M_Flag = true;
			
			String text = f_textField.getText();
			if(pos == -1 && !wrap_ar.isSelected() && firstText.equals(text))
				JOptionPane.showMessageDialog(editorFrame, "Cannot find "+text);
			else if(wrap_ar.isSelected())
				findText(textArea, text);
			else
				findText(textArea, text);
		});
		
		findDialog.add(findNext);
		
		JButton cancel = new JButton("Cancel");
		cancel.setBounds(285, 40, 80, 25);
		cancel.addActionListener(event -> findDialog.setVisible(false));
		findDialog.add(cancel);
		EditorFrame.findCommonText(f_textField , findDialog);
	}
	
	public void findText(JTextArea comp, String pattern)
	{
		boolean flag = false;
		
		try
		{
			highlter = comp.getHighlighter();
			Document document = comp.getDocument();
			String word = document.getText(0, document.getLength());
			
			if(c_M_Flag == true && r_One_Flag == true)
			{
				pos = p_B_Last;
				flag = true;
				c_M_Flag = false;
			}
			if(flag == false && !r_All_Flag == true)
			pos = word.toUpperCase().indexOf(pattern.toUpperCase(),pos);
			
			if(firstFlag == true)
			{
				rePos = pos;
				firstText = pattern;
				firstFlag = false;
			}	
			if(!firstText.equals(pattern))
			{
				if(rePos != -1 && !r_All_Flag == true && r_All_In_Flag == false)
					highlter.removeHighlight(remove_highliter);
				rePos = pos;
				firstText = pattern;
				count = 0;
				repos_Count = 1;
			}
			if(count != 0 && pos != -1)
				highlter.removeHighlight(remove_highliter);
			
			if(r_One_Flag == true)
			{
				int rWord_L = 0;
				int bWord_L = 0;
				if(pos>=0)
				{
					h_N_p = pos; 
					h_N_p+= pattern.length();
					h_N_p = word.toUpperCase().indexOf(pattern.toUpperCase(),h_N_p);
					if(h_N_p>=0)
						remove_highliter = highlter.addHighlight(h_N_p, h_N_p+pattern.length(), myHighlightPainter);
					comp.replaceRange(replaceText, pos, pos+pattern.length());
					if((rWord_L = replaceText.length())< (bWord_L = pattern.length()))
						pos+= replaceText.length();
					else
						pos+= pattern.length();
					
					EditorFrame.find_Text(pattern);
					EditorFrame.find_Pos(pos, remove_highliter);
					count = 1;
				}	
				else
				{
					if(rePos == -1)
						JOptionPane.showMessageDialog(editorFrame, "Cannot find "+pattern);
					else
					{
						if(repos_Count==1)
						{
							if(r_wrap_ar.isSelected())
							{
								pos = rePos;
								repos_Count = 2;
							}
							else
								JOptionPane.showMessageDialog(editorFrame, "Cannot find "+pattern);
						}
						else
							JOptionPane.showMessageDialog(editorFrame, "Cannot find "+pattern);
						
					}	
				}
					r_One_Flag = false;
			}
			else if(r_All_Flag == true)
			{
				String t_A_text = comp.getText();
				String text1 = t_A_text.replace(pattern, replaceText);
				comp.setText(" ");
				comp.setText(text1);
				
				/* ========================== Aslo below line done this work.=========================== 
				int rWord_L =  replaceText.length();
				int bWord_L = pattern.length();
				try
				{
					while((pos = word.toUpperCase().indexOf(pattern.toUpperCase(),pos)) != -1)
					{
						comp.replaceRange(replaceText, pos, pos+pattern.length());
						if(rWord_L < bWord_L)
							pos+= replaceText.length();
						else
							pos+= pattern.length();
						
						document = comp.getDocument();
						word = document.getText(0, document.getLength());
					}
				}
				catch(Exception	e)
				{
					e.printStackTrace();
				}*/
				
				r_All_In_Flag = true;
				r_All_Flag = false;
			}
			else
			{
				if(pos>=0)
				{
					remove_highliter = highlter.addHighlight(pos, pos+pattern.length(), myHighlightPainter);
					p_B_Last = pos;
					pos+= pattern.length();
					EditorFrame.find_Text(pattern);
					EditorFrame.find_Pos(pos, remove_highliter);
					count = 1;
				}	
				else
				{
					if(rePos == -1)
						JOptionPane.showMessageDialog(editorFrame, "Cannot find "+pattern);
					else
					{
						if(replaceFlag == true)
						{
							if(r_wrap_ar.isSelected())
								pos = rePos;
							else
								JOptionPane.showMessageDialog(editorFrame, "Cannot find "+pattern);
						}
						else
						{
							if(wrap_ar.isSelected() && firstText.equals(pattern))
								pos = rePos;
							else
								JOptionPane.showMessageDialog(editorFrame, "Cannot find "+pattern);
						}
					}
					
				}	
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	void paintNext(JTextComponent comp, String pattern, int pos1, Object removeHighlighter_Word)
	{	
		try
		{	
			highlter = comp.getHighlighter();
			Document document = comp.getDocument();
			String word = document.getText(0, document.getLength());
			
			
			highlter.removeHighlight(removeHighlighter_Word);
			pos1 = word.toUpperCase().indexOf(pattern.toUpperCase(),pos1);
			if(pos1>=0)
			{				
				remove_highliter = highlter.addHighlight(pos1, pos1+pattern.length(), myHighlightPainter);
				pos1+= pattern.length();
				EditorFrame.find_Pos(pos1, remove_highliter);
			}
			else
			{
				JOptionPane.showMessageDialog(editorFrame, "Cannot find "+pattern);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void mousePressed(MouseEvent event)
	{
		if(remove_highliter != null)
			highlter.removeHighlight(remove_highliter);
	}

/*=====================================Methods for replace word=======================================*/
	// make replace Dialog
	void r_Make_Dialog()
	{
		replaceFlag = true;
		
		JDialog replaceDialog = new JDialog(editorFrame, "Replace", false);
		replaceDialog.setSize(370, 200);
		replaceDialog.setLocation(140,300);
		replaceDialog.setLayout(null);
		
		JLabel label1 = new JLabel("Find what:");
		label1.setBounds(10, 10, 60, 20);
		replaceDialog.add(label1);
		
		JLabel label2 = new JLabel("Replace with:");
		label2.setBounds(10, 35, 80, 20);
		replaceDialog.add(label2);
		
		JTextField replaceFind = new JTextField();
		replaceFind.setBounds(80, 10, 155, 20);
		replaceFind.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		replaceDialog.add(replaceFind);
		
		JTextField replaceWith = new JTextField();
		replaceWith.setBounds(80, 35, 155, 20);
		replaceWith.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		replaceDialog.add(replaceWith);
		
		JButton f_Next_B = new JButton("Find next");
		f_Next_B.setBounds(250, 10, 90, 25);
		replaceDialog.getRootPane().setDefaultButton(f_Next_B);
		f_Next_B.setSelected(true);
		f_Next_B.addActionListener(event ->
		{
			c_M_Flag = true;
			
			String text = replaceFind.getText();
			if(pos == -1 && !r_wrap_ar.isSelected() && firstText.equals(text))
				JOptionPane.showMessageDialog(editorFrame, "Cannot find "+text);
			else if(r_wrap_ar.isSelected())
				findText(textArea, text);
			else
				findText(textArea, text);
		});
		replaceDialog.add(f_Next_B);
		
		JButton replaceButton = new JButton("Replace");
		replaceButton.setBounds(250, 40, 90, 25);
		replaceButton.addActionListener(event ->
		{
			r_One_Flag = true;
			
			String text = replaceFind.getText();
			replaceText = replaceWith.getText();
			if(pos == -1 && !r_wrap_ar.isSelected() && firstText.equals(text))
				JOptionPane.showMessageDialog(editorFrame, "Cannot find "+text);
			else if(r_wrap_ar.isSelected())
				findText(textArea, text);
			else
				findText(textArea, text);
		});
		replaceDialog.add(replaceButton);
		
		JButton r_All_B = new JButton("Replace All");
		r_All_B.setBounds(250, 70, 90, 25);
		r_All_B.addActionListener(event ->
		{
			pos = 0;
			r_All_Flag  = true;
			r_All_In_Flag = false;
			
			String text = replaceFind.getText();
			replaceText = replaceWith.getText();			
			findText(textArea, text);
		});
		replaceDialog.add(r_All_B);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBounds(250, 100, 90, 25);
		cancelButton.addActionListener(event -> replaceDialog.setVisible(false));
		replaceDialog.add(cancelButton);
		
		r_wrap_ar = new JCheckBox("Wrap around");
		r_wrap_ar.setSelected(true);
		r_wrap_ar.setBounds(6, 100, 90, 25);
		replaceDialog.add(r_wrap_ar);
		
		replaceDialog.setVisible(true);
	}
}
/*=================================This class use to count the no. of new window======================*/
class CountNum
{
	private static int c_value= 0;
	
	public static void count_N(int value)
	{
		c_value = value;
	}
	
	public static int t_C_N()
	{
		return c_value;
	}
}
