/*
 * Copyright (C) 2016-2019 Alexandre Oliveira <alexandre.oliveira@viglet.com> 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.viglet.turing.config;

public interface IHandlerConfiguration {

    String getTuringURL();
    String getIndex();
    String getConfig();
    String getLocale();
    String getChannel();
    String getMappingsXML();
    String getCDAContextName(); 
    String getCDAFormatName();
    String getCDAServer();
    String getCDAPort();
    String getCDAServer(String site);
    String getCDAPort(String site);
    String getCDAContextName(String site);
    String getCDAFormatName(String site);
    boolean hasSiteName(String site);
    boolean hasContext(String site);
    boolean hasFormat(String site);
	boolean isLive();
   
}
