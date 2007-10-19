/*
Copyright (C) 2007 Keith Stribley http://www.thanlwinsoft.org/

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
------------------------------------------------------------------------------*/

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
