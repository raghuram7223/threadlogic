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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.ateam.threadlogic.advisories;

import com.oracle.ateam.threadlogic.HealthLevel;
import com.oracle.ateam.threadlogic.ThreadInfo;
import com.oracle.ateam.threadlogic.ThreadState;
import java.util.regex.Pattern;


/**
 *
 * @author saparam
 */
public class SOAThreadGroup extends CustomizedThreadGroup {  
  
  protected int bpelInvokeThreads, b2bExecutorThreads, bpelEngineThreads,
      soaJMSConsumerThreads;
  
  public SOAThreadGroup(String grpName) {
    super(grpName);
  }
  
  public void runGroupAdvisory() {
    
    for (ThreadInfo ti : this.threads) {

      String content = ti.getContent();
      String threadNameLowerCase = ti.getFilteredName().toLowerCase();

      if (content.contains("b2b.engine.ThreadWorkExecutor"))
        ++this.b2bExecutorThreads;
      else if (threadNameLowerCase.contains("orabpel.engine"))
        ++this.bpelEngineThreads;
      else if (threadNameLowerCase.contains("orabpel.invoke"))
        ++this.bpelInvokeThreads;
      else if (content.contains("adapter.jms.inbound.JmsConsumer.run"))
        ++this.soaJMSConsumerThreads;
      
      if (isIdle(ti)) {
        ThreadAdvisory soaIdleThreadAdvisory = ThreadAdvisory.lookupThreadAdvisory(ThreadLogicConstants.SOA_IDLE_THREADS);
        ti.addAdvisory(soaIdleThreadAdvisory);
        ti.setHealth(HealthLevel.IGNORE);
      }
    }
  }
  
  public boolean isIdle(ThreadInfo ti) {
    
    String threadStack = ti.getContent();
    Pattern soaThreadWaitingPattern = Pattern.compile("java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject");
    
    boolean threadWaitingForAbstractQueuedSynchronizer = soaThreadWaitingPattern.matcher(threadStack).find();
            
    if (threadWaitingForAbstractQueuedSynchronizer           
          && (  
              (ti.getState() == ThreadState.PARKING) 
              || (ti.getState() == ThreadState.TIMED_WAIT) 
             )
        ) {
      int stackDepth = threadStack.split("\n").length;
      return (stackDepth <= ThreadLogicConstants.ACTIVETHREAD_STACKDEPTH);
    }
    
    return false;
  }
  public String getCustomizedOverview() {
    StringBuffer statData = new StringBuffer();
    statData.append("<tr bgcolor=\"#dddddd\"><td><font face=System "
        + ">Number of B2B Executor Threads </td><td><b><font face=System>");
    statData.append(this.b2bExecutorThreads);

    statData.append("</b></td></tr>\n\n<tr bgcolor=\"#eeeeee\"><td><font face=System "
        + ">Number of BPEL Invoke Threads </td><td><b><font face=System>");
    statData.append(this.bpelInvokeThreads);

    statData.append("</b></td></tr>\n\n<tr bgcolor=\"#dddddd\"><td><font face=System "
        + ">Number of BPEL Engine Threads </td><td><b><font face=System>");
    statData.append(this.bpelEngineThreads);

    statData.append("</b></td></tr>\n\n<tr bgcolor=\"#eeeeee\"><td><font face=System "
        + ">Number of SOA JMS Consumer Threads </td><td><b><font face=System>");
    statData.append(this.soaJMSConsumerThreads);

    statData.append("</b></td></tr>\n\n");
    return statData.toString();
  } 
  
}
