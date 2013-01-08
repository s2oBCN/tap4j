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

package org.tap4j.consumer;

import java.util.HashMap;
import java.util.Map;

public final class ConsumerOptions {

    public enum KEY {
        REQUIRE_PLAN
    }

    private final Map<KEY, Object> props;

    public ConsumerOptions() {
        super();
        this.props = defaults(new HashMap<KEY, Object>());
    }

    private Map<KEY, Object> defaults(Map<KEY, Object> map) {
        return (Map<KEY, Object>) map;
    }

    public <T> void setOption(KEY option, T value) {
        this.props.put(option, value);
    }

    // Since we have many different option types in the props map
    // we give the users this choice to decide on the option type
    // at their own risk.
    @SuppressWarnings("unchecked")
    public <T> T getOption(KEY option) {
        return (T) this.props.get(option);
    }

}
