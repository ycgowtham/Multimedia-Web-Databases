package test;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class PasswordDialog extends JDialog
{
	private static final long serialVersionUID = 1653005418988771116L;
	private JPasswordField passwordField;
	public PasswordDialog()
	{
		JPanel panel = new JPanel(new GridLayout(2, 2));
		this.setSize(new Dimension(200, 200));
		panel.add(new JLabel("DB Password: "));
		
		passwordField = new JPasswordField();

		JDialog self = this;

		passwordField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyChar() == '\r' || e.getKeyChar() == '\n')
				{
					self.setVisible(false);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				//no op
			}

			@Override
			public void keyReleased(KeyEvent e) {
				//no op
			}
			
		});
		panel.add(passwordField);
		
		JButton submit = new JButton("submit");
		submit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals(submit))
				{
					self.setVisible(false);
				}
			}
			
		});
		panel.add(submit);
		
		this.add(panel);
		
		self.setVisible(true);
	}
	
	public String getPassword()
	{
		return String.copyValueOf(this.passwordField.getPassword());
	}
}
