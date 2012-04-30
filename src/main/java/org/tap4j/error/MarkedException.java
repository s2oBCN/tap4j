/*
 * The MIT License
 *
 * Copyright (c) <2012> <Bruno P. Kinoshita>
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
package org.tap4j.error;

/**
 * 
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 0.1
 */
public class MarkedException extends TapException {
	private static final long serialVersionUID = -8056558453785235593L;
	private String context;
    private Mark contextMark;
    private String problem;
    private Mark problemMark;
    private String note;
	
	protected MarkedException(String context, Mark contextMark, String problem, 
			Mark problemMark, String note) {
		this(context, contextMark, problem, problemMark, note, null);
	}
	
	protected MarkedException(String context, Mark contextMark, String problem, 
			Mark problemMark, String note, Throwable cause) {
		super(context + "; " + problem, cause);
		this.context = context;
        this.contextMark = contextMark;
        this.problem = problem;
        this.problemMark = problemMark;
        this.note = note;
	}
	
	protected MarkedException(String context, Mark contextMark, String problem, Mark problemMark) {
        this(context, contextMark, problem, problemMark, null, null);
    }

    protected MarkedException(String context, Mark contextMark, String problem,
            Mark problemMark, Throwable cause) {
        this(context, contextMark, problem, problemMark, null, cause);
    }
    
    public String getContext() {
        return context;
    }

    public Mark getContextMark() {
        return contextMark;
    }

    public String getProblem() {
        return problem;
    }

    public Mark getProblemMark() {
        return problemMark;
    }
    
    @Override
    public String toString() {
        StringBuilder lines = new StringBuilder();
        if (context != null) {
            lines.append(context);
            lines.append("\n");
        }
        if (contextMark != null
                && (problem == null || problemMark == null
                        || (contextMark.getName().equals(problemMark.getName()))
                        || (contextMark.getLine() != problemMark.getLine()) || (contextMark
                        .getColumn() != problemMark.getColumn()))) {
            lines.append(contextMark.toString());
            lines.append("\n");
        }
        if (problem != null) {
            lines.append(problem);
            lines.append("\n");
        }
        if (problemMark != null) {
            lines.append(problemMark.toString());
            lines.append("\n");
        }
        if (note != null) {
            lines.append(note);
            lines.append("\n");
        }
        return lines.toString();
    }
    
}
