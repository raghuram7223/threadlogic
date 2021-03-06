/**
 * Copyright (c) 2012 egross, sabha.
 * 
 * ThreadLogic - parses thread dumps and provides analysis/guidance
 * It is based on the popular TDA tool.  Thank you!
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */
package com.oracle.ateam.threadlogic.utils;

import java.io.IOException;

/**
 * helper class for launching the default browser
 */
public class Browser {

  /**
   * Starts the default browser for the current platform.
   * 
   * @param url
   *          The link to point the browser to.
   */
  public static void open(String url) throws InterruptedException, IOException {
    String cmd = null;

    if (isWindows()) {
      cmd = ("rundll32 url.dll,FileProtocolHandler " + maybeFixupURLForWindows(url));
      Runtime.getRuntime().exec(cmd);
    } else {
      if (System.getenv("BROWSER") != null) {
        cmd = System.getenv("BROWSER") + " " + url;
      } else {
        cmd = "firefox -remote openURL(" + url + ")";
      }
      Process p = Runtime.getRuntime().exec(cmd);
      int exitcode = p.waitFor();
      if (exitcode != 0) {
        cmd = "firefox " + url;
        Runtime.getRuntime().exec(cmd);
      }
    }
  }

  /**
   * If the default browser is Internet Explorer 5.0 or greater, the URL.DLL
   * program fails if the url ends with .htm or .html . This problem is
   * described by Microsoft at
   * http://support.microsoft.com/support/kb/articles/Q283/2/25.ASP Of course,
   * their suggested workaround is to use the classes from the microsoft Java
   * SDK, but fortunately another workaround does exist. If you alter the url
   * slightly so it no longer ends with ".htm", the URL can launch successfully.
   * The logic here appends a null query string onto the end of the URL if none
   * is already present, or a bogus query parameter if there is already a query
   * string ending in ".htm"
   */
  private static String maybeFixupURLForWindows(String url) {
    // plain filenames (e.g. c:\some_file.html or \\server\filename) do
    // not need fixing.
    if (url == null || url.length() < 2 || url.charAt(0) == '\\' || url.charAt(1) == ':')
      return url;
    String lower_url = url.toLowerCase();
    int i = badEndings.length;
    while (i-- > 0)
      if (lower_url.endsWith(badEndings[i]))
        return fixupURLForWindows(url);
    return url;
  }

  static final String[] badEndings = { ".htm", ".html", ".htw", ".mht", ".cdf", ".mhtml", ".stm" };

  private static String fixupURLForWindows(String url) {
    if (url.indexOf('?') == -1)
      return url + "?";
    else
      return url + "&workaroundStupidWindowsBug";
  }

  /**
   * Checks if the OS is windows.
   * 
   * @return true if it is, false if it's not.
   */
  public static boolean isWindows() {
    if (System.getProperty("os.name").indexOf("Windows") != -1) {
      return true;
    } else {
      return false;
    }
  }
}
