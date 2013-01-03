/*
 * The MIT License
 * 
 * Copyright (c) 2010 tap4j team (see AUTHORS)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.tap4j.events;

import org.tap4j.error.Mark;

public class VersionEvent extends Event {

    private final int version;
    
    /**
     * @param startMark
     * @param endMark
     */
    public VersionEvent(int version, Mark startMark, Mark endMark) {
        super(startMark, endMark);
        this.version = version;
    }
    
    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /* (non-Javadoc)
     * @see org.tap4j.events.Event#is(org.tap4j.events.Event.ID)
     */
    @Override
    public boolean is(ID id) {
        return ID.Version == id;
    }
    
    /* (non-Javadoc)
     * @see org.tap4j.events.Event#getArguments()
     */
    @Override
    protected String getArguments() {
        return "version=" + getVersion();
    }

}
