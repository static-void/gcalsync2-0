/*
   Copyright 2007

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
package com.gcalsync.log;

/**
 * Exeption that holds some stack information (like 'cause')
 * @version $Rev: 1 $
 * @date $Date: 2007-12-18 $
 */
public class GCalException extends Exception {
    private Throwable cause;
    
    /** Creates a new instance of GCalException */
    public GCalException(String message, Throwable cause) {
        super(buildSuperMessage(message, cause));
        this.cause = cause;
    }
    
    private static String buildSuperMessage(String message, Throwable cause) {
        if(cause != null) {
            if(cause.getClass() != Exception.class && cause.getClass() != GCalException.class) {
                String className = cause.getClass().getName();
                if(className.indexOf('.') >= 0) {
                    className = className.substring(className.lastIndexOf('.'));
                }
                message += ": " + className + ( cause.getMessage() == null ? "" : (" - " + cause.getMessage()) );
            }
            else if(cause.getMessage() != null) {
                message += ": " + cause.getMessage();
            }
        }
        return message;
    }
    
    public GCalException(String className, String methodName, Throwable cause) {
        this("Error at " + className + "." + methodName, cause);
    }
    
    public GCalException(Class sourceClass, String methodName, Throwable cause) {
        this("Error at " + sourceClass.getName() + "." + methodName, cause);
    }
    
    public GCalException(String message, String className, String methodName, Throwable cause) {
        this(message + " at " + className + "." + methodName, cause);
    }
    
    public GCalException(String message) {
        this(message, (Throwable)null);
    }
    
    public GCalException(String className, String methodName) {
        this(className, methodName, (Throwable)null);
    }
    
    public GCalException(String message, String className, String methodName) {
        this(message, className, methodName, null);
    }
    
    public Throwable getCause() {
        return cause;
    }
    
}
