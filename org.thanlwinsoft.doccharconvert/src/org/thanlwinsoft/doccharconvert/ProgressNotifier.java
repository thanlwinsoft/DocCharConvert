/**
 * 
 */
package org.thanlwinsoft.doccharconvert;

import java.io.File;

/**
 * @author keith
 *
 */
public class ProgressNotifier 
{
    boolean cancelled = false;
    int progress = 0;
    int workLength = 0;
    String taskName = null;
    public ProgressNotifier(){}
    public void beginTask(String name, int totalWork) 
    { 
        taskName = name; workLength = totalWork; 
    }
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    public boolean isCancelled() { return cancelled; }
    public void worked(int work) { progress = work; }
    public void subTask(String task) { taskName = task; };
    public void done() {};
    public void setFileStatus(File file, String status) {}
}
