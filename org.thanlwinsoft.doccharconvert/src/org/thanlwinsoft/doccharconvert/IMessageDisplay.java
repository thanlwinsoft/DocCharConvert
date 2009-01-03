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

/**
 * @author keith
 * abstracts out dialog display for different GUI implementations
 */
public interface IMessageDisplay
{
    /**
     * @author keith
     * Option enumeration
     */
    public enum Option { /**
     * yes
     */
    YES, /**
     * no
     */
    NO, /**
     * yes to all
     */
    YES_ALL, /**
     * no to all
     */
    NO_ALL };
    /**
     * @param message
     * @param title
     */
    public void showWarningMessage(String message, String title);
    /**
     * @param message
     * @param title
     * @return option chosen
     */
    public Option showYesNoMessage(String message, String title);
}
