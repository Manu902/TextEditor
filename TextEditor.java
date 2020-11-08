import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class TextEditor
{	
	public static void main(String[] args)
	{
		EventQueue.invokeLater(() ->
		{
			EditorFrame frame = new EditorFrame();
			frame.setVisible(true);
			
			frame.addWindowListener(new WindowAdapter()       //  use of anonimous class
			{
				public void windowClosing(WindowEvent event)
				{	
					JTextArea text_A = frame.text_A_M();
					boolean flag = false;
					
					if(frame.getTitle().equals("Untitled - Notepad") && !text_A.getText().isEmpty())
					{
						frame.saveDialogTest();
						flag = true;
					}
					if(!frame.getTitle().equals("Untitled - Notepad") && flag == false || frame.getTitle().equals("Untitled - Notepad") && text_A.getText().isEmpty())
					{
						int num = CountNum.t_C_N();
						if(num == 0)
							frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
}