package kdk.program;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;
import java.io.File;
import java.awt.event.*;


/** Control of user input.<br />
  Controls the reactions to user input like button clicks and
  turn buttons, menu and clicks on the field.<br />
  On loading the class, the folder kdk/robots will be searched for .class
  fiels that inherit from Robot1. */

public class KdKFeldSteuerung
{

/** Name of the path containing the robot classes. */
	static String KLASSENPFAD = "kdk/roboter";
	private static String[] roboter;
/** Searches the classpath for potential robot classes.
 * @see KdKFeldSteuerung#KLASSENPFAD */
	static {
		File dir = new File(KLASSENPFAD);
		if (!dir.isDirectory())
		{
			System.out.println("Verzeichnis "+KLASSENPFAD+
				" nicht gefunden, keine Roboterklassen !");
			System.exit(1);
		}
		String[] namen = dir.list();
		int anzahl = 0;
		for (int i=0; i<namen.length; i++)
			if (namen[i].endsWith(".class"))
				anzahl++;
		roboter = new String[anzahl];
		if (anzahl == 0)
		{
			System.out.println("Keine Roboterklassen gefunden !");
			System.exit(1);
		}
		int j = 0;
		for (int i=0; i<namen.length; i++)
			if (namen[i].endsWith(".class"))
				roboter[j++] = namen[i].substring(0,namen[i].length()-6);
	}

	private Vector asw = new Vector();
	private KdKFeld kf;
        private boolean schritte = false;
        private SchnelleSchritteThread sst;
        private AddRobotDialog ard;


/** Creates a new instance of the FeldSteuerung.<br />
  The constructor instanciates also the field.  This must taken by the 
  main window using the getFeld() getter.
 * @see kdk.program.KdKFeldSteuerung#getFeld() */

	public KdKFeldSteuerung(JFrame parent, JTextArea log)
	{
		kf = new KdKFeld(this,log);
		ard = new AddRobotDialog(parent);
	}


/** Returns the field on which this control instance acts. */

	public KdKFeld getFeld()
	{
		return kf;
	}


/** Opens a dialog which asks the user about classes and position.
  Parameters are the initial values of the position textfields.
  The dialog class then adds the robot after close.
/* @see kdk.program.KdKFeld#addRobot(int, int, Robot1)*/

	public void openAddRobotDialog(int x, int y)
	{
		ard.setVisible(x,y);
	}


/** Verifies from the user if a robot really should be deleted.
 * @return user answer */

	public boolean openKillRobotDialog()
	{
		return (JOptionPane.showConfirmDialog(kf,
                        "Soll der Roboter gelöscht werden ?",
                        "Roboter löschen",
			JOptionPane.YES_NO_OPTION)
                                        == JOptionPane.YES_OPTION);
	}


/** Handles the ActionCommand of each event in the main window.
 * @see kdk.KriegdKerne */

	public void perform(String s)
	{
		if (s.equals("hinein"))
                {
			int x = 7;
			int y = 7;
			int t = 0;
			boolean gefunden = false;
			while (!gefunden)
			{
				x =  (int)Math.round(Math.random()*KdKFeld.xgroesse);
				y =  (int)Math.round(Math.random()*KdKFeld.ygroesse);
				if ((kf.getFeld(x,y) == null) ||
				    (t++ > KdKFeld.xgroesse*KdKFeld.ygroesse))
					gefunden = true;
			}
                        openAddRobotDialog(x,y);
                }
                else if (s.equals("schonwieder"))
                {
                        kf.clearAll();
                        for (int i=0; i<asw.size(); i++)
                        {
                                RobotTemp einrobot = (RobotTemp)asw.elementAt(i);
                                Robot1 r = einrobot.s();
                                kf.addRobot(einrobot.x, einrobot.y,r);
                        }
                }
                else if (s.equals("alleraus"))
                {
                        kf.clearAll();
                        asw = new Vector();
                }
                else if (s.equals("einzel"))
                        einzelSchritt();
                else if (s.startsWith("schnell"))
		{
			s = s.substring(8,s.length());
			int delay = Integer.parseInt(s);
                        schnelleSchritte(delay);
		}
	}


/** Executes a single turn or stops the quick moves thread. */

	public void einzelSchritt()
        {
                boolean dummi = schritte;
                schritte = false;
                if (sst != null)
                        try
                        {
                                sst.join();
                        }
                        catch(InterruptedException ie) {}
                if (!dummi) kf.schritt();
        }


/** Starts the quick moves thread with given delay (in 100th of a second). */

        public void schnelleSchritte(int delay)
        {
                if (!schritte)
                {
                        schritte = true;
                        sst = new SchnelleSchritteThread(delay);
                        sst.start();
                }
        }

private class SchnelleSchritteThread extends Thread
{
	private int delay = 50;
	private int allezehn = 0;

	public SchnelleSchritteThread(int d)
	{
		delay = d * 10;
	}

	public void run()
	{
		while (schritte)
 	{
			schritte = kf.schritt();
			try
			{
				if (++allezehn >= 10)
				{
					allezehn = 0;
					sleep(50);
				}
				sleep(delay);
			}
			catch(InterruptedException ie) {}
		}
	}
}


/** Allows addition of a robot instance into the field.
  Available classes are those found in the classpath. 
 * @see KdKFeldSteuerung#KLASSENPFAD */
 
public class AddRobotDialog extends JDialog
{

	private JTextField xpo = new JTextField(3);
	private JTextField ypo = new JTextField(3);
	private JList klana = new JList();
	private JFrame parent;

	public AddRobotDialog(JFrame parent)
	{
		super(parent);
		setTitle("Roboter hinzufuegen");
		setModal(true);
// MinimumSize = 333,155
		this.parent = parent;

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent ae)
			  {
				if (bestaetigung())
					setVisible(false);
			  } } );
		JButton cancelButton = new JButton("Abbruch");
		cancelButton.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent ae)
			  {
				setVisible(false);
			  } } );
		klana.setListData(roboter);
		klana.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		klana.setSelectedIndex(0);
		JScrollPane sp  = new JScrollPane(klana);
		sp.setPreferredSize(new Dimension(150,200));

		Container content = getRootPane().getContentPane();
		JPanel temp;
		GridBagLayout gbl = new GridBagLayout();
		content.setLayout(gbl);
		GridBagConstraints c = new GridBagConstraints();
		temp = new JPanel(new BorderLayout());
		temp.setBorder(BorderFactory.createCompoundBorder
				(BorderFactory.createTitledBorder
				(BorderFactory.createEtchedBorder(),
				" Roboterklassen "),
				BorderFactory.createEmptyBorder(5,5,2,2)));
		temp.add(sp);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.gridwidth = 3;
		gbl.setConstraints(temp,c);
		content.add(temp);
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		temp = new JPanel(new BorderLayout());
		temp.setBorder(BorderFactory.createCompoundBorder
				(BorderFactory.createTitledBorder
				(BorderFactory.createEtchedBorder(),
				" Position "),
				BorderFactory.createEmptyBorder(5,5,2,2)));
		JPanel temp2 = new JPanel();
		temp2.add(new JLabel("X-Position : "));
		temp2.add(xpo);
		temp.add(temp2, BorderLayout.NORTH);
		temp2 = new JPanel();
		temp2.add(new JLabel("Y-Position : "));
		temp2.add(ypo);
		temp.add(temp2,BorderLayout.SOUTH);
		gbl.setConstraints(temp,c);
		content.add(temp);
		c.fill = GridBagConstraints.NONE;
		c.weighty = 1.0;
		temp = new JPanel();
		temp.add(okButton);
		temp.add(cancelButton);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.SOUTH;
		gbl.setConstraints(temp,c);
		content.add(temp);

		pack();
	}

	public void setVisible(boolean b)
	{
		Point p = parent.getLocation();
		setLocation(p.x+30,p.y+50);
		super.setVisible(b);
	}


/** Opens the dialog and sets given parameters in the position text fields. */

	public void setVisible(int x, int y)
	{
		xpo.setText(new Integer(x).toString());
		ypo.setText(new Integer(y).toString());
		setVisible(true);
	}


/** Validates class and position. */
	public boolean bestaetigung()
	{
		String s = "kdk.roboter.";
		try
		{
			s = s.concat((String)klana.getSelectedValue());
			Class c = Class.forName(s);
//			Method m = c.getMethod("instantiiere",null);
//			Robot1 r = (Robot1)m.invoke(c,null);
			Robot1 r = (Robot1)c.newInstance();
			int x = Integer.parseInt(xpo.getText());
			int y = Integer.parseInt(ypo.getText());
			if (!(kf.addRobot(x,y,r)))
				throw new NumberFormatException("Ungültige Position");
			boolean posfrei = true;
			for (int b = 0; b<asw.size(); b++)
			{
				RobotTemp einrobot = (RobotTemp)asw.elementAt(b);
				if ((einrobot.x == x) && (einrobot.y == y))
					posfrei = false;
			}
			if (posfrei) asw.add(new RobotTemp(x,y,s));
		}
		catch (NumberFormatException nfe)
		{
			JOptionPane.showMessageDialog(this,
				new JLabel("Positionierungsfehler"),
				"Fehler", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(this,
				new JLabel("Fehler beim Laden der Klasse "+s),
				"Fehler", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		return true;
	}
}


private class RobotTemp {
	
	public int x;
	public int y;
	private String ss;
	
	public RobotTemp(int px, int py, String ps)
	{
		x = px;
		y = py;
		ss = ps;
	}
	
	public Robot1 s()
	{
		try
		{
			return (Robot1)((Class.forName(ss)).newInstance());
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
		return null;
	}
}

}
