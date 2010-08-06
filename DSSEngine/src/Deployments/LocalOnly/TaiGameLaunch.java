package Deployments.LocalOnly;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 * Basically just handles logging, for now.
 * @author Benjamin
 */
public abstract class TaiGameLaunch {
	/**
	 * Class to launch is the first argument.
	 */
	public static void main(String[] args){
		String classToLoad = args[0];
		launch(classToLoad);
	}
	private ThreadGroup group;
	private static void launch(String classToLoad){
		try {
			ClassLoader cl = TaiGameLaunch.class.getClassLoader();
			Class child = cl.loadClass(classToLoad);
			final TaiGameLaunch k = (TaiGameLaunch)child.newInstance();
			setErrorLog(k);
			k.group = new ThreadGroup(k.getGameName()+"ThreadGroup"){
				public void uncaughtException(Thread t, Throwable e) {
					death0(e,k.getGameName(),k.logFile,true,k.myGUI); //These will kill the game
				}
			};			
			new Thread(k.group, "TaiGameLaunch"){
				public void run(){
					k.launch();
				}
			}.start();
		} catch (Throwable e) {
			death0(e,"TaiGameLaunch","N/A", true, null); //Couldn't get error handling in time.
		}
	}
	public abstract String getGameName();
	public abstract void launch();
	private Component myGUI;
	/**
	 * So that the error window pops up correctly.
	 */
	public void setMainGUIElement(Component e){
		myGUI = e;
	}
	public void death(Throwable e){
		death0(e, getGameName(), logFile, true, myGUI);
	}
	private static void death0(Throwable e, String name, String logFile, boolean die, Component GUIElement){
		e.printStackTrace(); //Goes to logger
		JTextArea txtarea = new JTextArea();
		String txt = "";
		if(e.getCause()!=null){
			txt+=e.getCause().getMessage()+"\r\n";
			txt+="(Was child exception of "+e.getClass().getSimpleName()+")\r\n";
		} else {
			txt+=e.getMessage()+"\r\n";
		}
		txt+="See the logfile: "+logFile;
		txtarea.setText(txt);
		JOptionPane.showMessageDialog(GUIElement, txtarea, "Fatal Error in "+name, JOptionPane.ERROR_MESSAGE);
		if (die) System.exit(1);
	}
	/**
	 * Called before launch!!!
	 */
	private String logFile;
	private static void setErrorLog(final TaiGameLaunch k){
		System.setErr(new PrintStream(System.err){
			PrintStream actOut;
			{
				try {
					DateFormat df = new SimpleDateFormat( "yyyy-MM-dd" ) ;
					k.logFile = "log"+df.format(new Date())+".txt";
					actOut = new PrintStream(new FileOutputStream(new File(k.logFile),true));	
				} catch (Throwable e){
					k.death(e);
				}
			}
			public void write(byte[] k, int s, int e){
				actOut.write(k,s,e);
				super.write(k,s,e);
			}

		});
		System.err.println("////////////////////////////////////");
		System.err.println("Logging enabled "+new Date());

		Properties p = System.getProperties();
		String lineFormat = "%-10s%20s||%-10s%50s";
		System.err.printf(lineFormat,
				"vm/java",
				p.getProperty("java.vm.vendor")+" /"+p.getProperty("java.specification.version"),
				"OS",
				p.getProperty("os.arch")+" arch of "+p.getProperty("os.name"));
		System.err.println();
		System.err.println("java.library.PATH:"+p.getProperty("java.library.path"));
		System.err.println("-cp "+p.getProperty("java.class.path"));	}
	public static void dbgPt() {
		System.out.println("///////DDDDEEEEEBBBBUUUUGGGG");
	}
}
