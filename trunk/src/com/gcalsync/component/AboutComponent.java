/*
   Copyright 2007 batcage@gmail.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.gcalsync.component;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

/**
 * Public class responsible for drawing the About screen, fetching items to display 
 * on the screen, and setting up the OK command button. User accesses this screen 
 * by clicking the Menu button on the Login screen and selecting "About" from the menu. 
 * This class uses the MVCComponent base class to retrieve, draw, and update screens.  
 *   
 * @author 
 * @author 
 * @version 
 * @date 
 */
public class AboutComponent extends MVCComponent 
{
	static final Command CMD_OK = new Command("OK", Command.OK, 1);

    Form form;
	MIDlet midlet;

	/**
    * Constructor
	*/
	public AboutComponent(MIDlet midlet)
	{
		this.midlet = midlet;
	}

	/**
    * Gets the form for this component
    *
    * @returns <code>form</code>
	*/
    public Displayable getDisplayable() {return this.form;}

	/**
	* Updates the view after it is created
	*/
	protected void updateView() {}

	/**
	* Initializes the model before creating the view
	*/
    protected void initModel() {}

	/**
    * Creates the view
	*/
    protected void createView() 
	{
                //items that display on the page
		final StringItem lblVersion = new StringItem("Version", null);
		final StringItem lblBuildDate = new StringItem("Build date", null);
		final StringItem lblProjectHomepage = new StringItem("Project homepage", null);
		final StringItem lblDisclaimer = new StringItem("Disclaimer", null);
		final Font labelFont = Font.getFont(Font.FACE_PROPORTIONAL, 
											Font.STYLE_BOLD,
											Font.SIZE_SMALL);
		String str;
                //Gets midlet properties to display "About" and Midlet name 
                //as screen title 
                //fetches the midlet name and assigns to str variable for display 
		str = this.midlet.getAppProperty("MIDlet-Name");
                //if str variable is not empty, display "About" + Midlet Name for screen title
		if (str != null)
			str = "About " + str;
                //if str variable is empty, display "About" only for screen title
		else
			str = "About";
                //sets the value of str to the title of the form
		this.form = new Form(str);
                //add command listener for OK button
		this.form.addCommand(CMD_OK);
		this.form.setCommandListener(this);

		try 
		{
                        //Gets midlet properties to display application version, 
                        //build date, and project URL on form
                        //fetches midlet version and assigns to str variable
			str = this.midlet.getAppProperty("MIDlet-Version");
                        //if str variable is not empty
			if (str != null)
			{
				lblVersion.setFont(labelFont);
				this.form.append(lblVersion);
                                //display Midlet Version number + "Beta" on the form 
				this.form.append(str + " (ßeta)");
				this.form.append(new Spacer(getDisplayable().getWidth(), 5));
			}
                        //fetches midlet build date and assigns to str variable
			str = this.midlet.getAppProperty("Build-Date");
                        //if str variable is not empty
			if (str != null)
			{
				lblBuildDate.setFont(labelFont);
				this.form.append(lblBuildDate);
                                //display build date on form
				this.form.append(str);
				this.form.append(new Spacer(getDisplayable().getWidth(), 5));
			}
                        //fetches midlet project URL and assigns to str variable
			str = this.midlet.getAppProperty("MIDlet-Info-URL");
                        //if str variable is not empty
			if (str != null)
			{
				lblProjectHomepage.setFont(labelFont);
				this.form.append(lblProjectHomepage);
                                //display Midlet project URL on form
				this.form.append(str);
				this.form.append(new Spacer(getDisplayable().getWidth(), 5));
			}
		}
		catch (Exception e) {}

		lblDisclaimer.setFont(labelFont);
                //displays Disclaimer text on form by default
		this.form.append(lblDisclaimer);
		this.form.append("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.");
    }

	/**
    * Processes menu commands
    * When user clicks the "OK" button, application displays the login screen
    *
    * @param c command to execute
    * @param displayable the form from which <code>command</code>
    *                    originates
	*/
    public void commandAction(Command c, Displayable displayable) 
	{
		Components.login.showScreen(false);
    }
}


