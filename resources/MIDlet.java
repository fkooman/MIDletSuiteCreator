MIDLET_PKG

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;

public class MIDLET_NAME extends MIDlet implements CommandListener {
	private Form form;
	
	public MIDLET_NAME() {
		form = new Form("MIDLET_NAME");
		form.addCommand(new Command("Exit", Command.EXIT, 0));
                form.append("Hello World!\n");
		form.setCommandListener(this);
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

	protected void startApp() throws MIDletStateChangeException {
		Display.getDisplay(this).setCurrent(form);
	}

	public void commandAction(Command c, Displayable d) {
		notifyDestroyed();
	}
}

