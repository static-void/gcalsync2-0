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
		final StringItem lblVersion = new StringItem("Version", null);
		final StringItem lblBuildDate = new StringItem("Build date", null);
		final StringItem lblProjectHomepage = new StringItem("Project homepage", null);
		final StringItem lblDisclaimer = new StringItem("Disclaimer", null);
		final Font labelFont = Font.getFont(Font.FACE_PROPORTIONAL, 
											Font.STYLE_BOLD,
											Font.SIZE_SMALL);
		String str;

		str = this.midlet.getAppProperty("MIDlet-Name");
		if (str != null)
			str = "About " + str;
		else
			str = "About";

		this.form = new Form(str);

		this.form.addCommand(CMD_OK);
		this.form.setCommandListener(this);

		try 
		{
			str = this.midlet.getAppProperty("MIDlet-Version");
			if (str != null)
			{
				lblVersion.setFont(labelFont);
				this.form.append(lblVersion);
				this.form.append(str + " (ßeta)");
				this.form.append(new Spacer(getDisplayable().getWidth(), 5));
			}

			str = this.midlet.getAppProperty("Build-Date");
			if (str != null)
			{
				lblBuildDate.setFont(labelFont);
				this.form.append(lblBuildDate);
				this.form.append(str);
				this.form.append(new Spacer(getDisplayable().getWidth(), 5));
			}

			str = this.midlet.getAppProperty("MIDlet-Info-URL");
			if (str != null)
			{
				lblProjectHomepage.setFont(labelFont);
				this.form.append(lblProjectHomepage);
				this.form.append(str);
				this.form.append(new Spacer(getDisplayable().getWidth(), 5));
			}
		}
		catch (Exception e) {}

		lblDisclaimer.setFont(labelFont);
		this.form.append(lblDisclaimer);
		this.form.append("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.");
    }

	/**
    * Processes menu commands
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


