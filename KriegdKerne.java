package kdk;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import kdk.program.*;


/** KriegdKerne is the executable class of the game "CoreWars". */

public class KriegdKerne extends JFrame implements ActionListener
{

	private JTextField delay = new JTextField("50",4);
	private JTextArea logbuch = new JTextArea();
	private KdKFeldSteuerung kfs = new KdKFeldSteuerung(this, logbuch);


/** Starts the CoreWars. It only constructs the class. */

	public static void main(String[] args)
	{
		new KriegdKerne();
	}


/** Creates a new instance of the class, which starts the game. */
	public KriegdKerne()
	{
		super("Krieg der Kerne");
		setLocation(80,40);
		KdKFeld kf = kfs.getFeld();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) { dispose(); }
			public void windowClosed(WindowEvent we) { System.exit(0); } });

		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu("Spiel");
		menu.add(makeMenuItem("Roboter einfuegen",'r',"hinein"));
		menu.add(makeMenuItem("Startwerte",'s',"schonwieder"));
		menu.add(makeMenuItem("Alle Roboter loeschen",'a',"alleraus"));
		menu.add(makeMenuItem("Beenden",'b',"schlussjetzt"));
		menubar.add(menu);
		setJMenuBar(menubar);

		JButton einzelschrittbutton = new JButton("Einzelschritt");
		einzelschrittbutton.setActionCommand("einzel");
		einzelschrittbutton.addActionListener(this);
		JButton schnellschrittbutton = new JButton("schnelle Schritte");
		schnellschrittbutton.setActionCommand("schnell");
		schnellschrittbutton.addActionListener(this);
		logbuch.setEditable(false);
		JScrollPane sp = new JScrollPane(logbuch);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	
		JPanel temp;	 
		Container content = getRootPane().getContentPane();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		content.setLayout(gbl);

		c.fill = GridBagConstraints.BOTH;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.gridwidth = 3;
		c.weightx = 2.0;
		c.weighty = 1.0;
		temp = new JPanel(new BorderLayout());
		temp.add(kf);
		gbl.setConstraints(temp,c);
		content.add(temp);
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		temp = new JPanel();
		temp.add(einzelschrittbutton);
		gbl.setConstraints(temp,c);
		content.add(temp);
		temp = new JPanel();
		temp.add(schnellschrittbutton);
		gbl.setConstraints(temp,c);
		content.add(temp);
		c.fill = GridBagConstraints.BOTH;
		temp = new JPanel(new FlowLayout());
		temp.add(new JLabel("1/100 sek "));
		temp.add(delay);
		gbl.setConstraints(temp,c);
		content.add(temp);
		c.weighty = 1.0;
		temp = new JPanel(new BorderLayout());
		temp.setBorder(BorderFactory.createCompoundBorder
 			 (BorderFactory.createTitledBorder
			 (BorderFactory.createEtchedBorder()," Meldungen "),
			 BorderFactory.createEmptyBorder(5,5,2,2)));
		temp.add(sp);
		gbl.setConstraints(temp,c);
		content.add(temp);

		pack();
		setVisible(true);
	}

	private JMenuItem makeMenuItem(String n, char c, String a)
	{
		JMenuItem result = new JMenuItem(n);
		result.addActionListener(this);
		result.setActionCommand(a);
		result.setMnemonic(c);
		return result;
	}

	private int getDelay()
	{
		int result = 50;
		try
		{
			result = Integer.parseInt(delay.getText());
		}
		catch(NumberFormatException nfe)
		{
			delay.setText("50");
		}
		return result;
	}


/** Implements reactions to user events. Most are proxied to FeldSteuerung (field controller). Only pressing the <i>end</i> menu entry is executed in case of the button <i>fast steps</i> appends the content of the speed textfield to the event name. */

	public void actionPerformed(ActionEvent ae)
	{
		String s = ae.getActionCommand();
		if (s.equals("schlussjetzt"))
			System.exit(0);
		if (s.equals("schnell"))
			s = s.concat(" "+getDelay());
		kfs.perform(s);
	}

}
