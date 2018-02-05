/*
 * The MIT License
 *
 * Copyright 2018 ss.
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
package com.salesforce.report;

import com.sforce.soap.apex.CodeCoverageResult;

/**
 * Test coverage element.
 * @author ss
 */
public class CoverageElement {
    /** Coverage percent. */
    private float coveragePercent;
    /** Coverage lines. */
    private int coverageLines;
    /** Total lines in class. */
    private int totalLines;
    /** Class name. */
    private String className;
    /**
     * Constructor for JAXB.
     */
    public CoverageElement() {
    }
    /**
     * Constructor.
     * @param ccr code coverage result.
     */
    public CoverageElement(final CodeCoverageResult ccr) {
        this.totalLines = ccr.getNumLocations();
        this.coverageLines = ccr.getNumLocations()
                - ccr.getNumLocationsNotCovered();
        if (ccr.getNumLocations() == 0) {
            coveragePercent = 100f;
        } else {
            coveragePercent = (((float) this.coverageLines)
                    / ((float) ccr.getNumLocations())) * 100;
        }
        this.className = ccr.getName();
    }
    /**
     * Represent as HTML row.
     * @param coveragePercentLimit coverage percent limit.
     * @return HTML row.
     */
    public String toHTMLRow(Integer coveragePercentLimit) {
        StringBuilder sb = new StringBuilder();
        String percentClass = getCoveragePercent() >= coveragePercentLimit
                ? "coverage-high" : "coverage-low";
        sb.append("<tr>");
        sb.append("<td>").append(className).append("</td>");
        /*sb.append("<td class=\"state-col\">")
                .append(fail != null ? "&#9746;" : "&#9745;")
                .append("</td>");*/
        sb.append("<td class=\"lines-col ").append(percentClass)
                .append("\">")
                .append(coverageLines)
                .append(" / ").append(totalLines)
                .append("</td>");
        sb.append("<td class=\"percent-col ").append(percentClass)
                .append("\">")
                .append(String.format("%.1f", getCoveragePercent()))
                .append("</td>");
        sb.append("</tr>");
        return sb.toString();
    }
// ============================= SET & GET ====================================
    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }
    /**
     * @return the coverageLines
     */
    public int getCoverageLines() {
        return coverageLines;
    }
    /**
     * @return the totalLines
     */
    public int getTotalLines() {
        return totalLines;
    }
    /**
     * @return the coveragePercent
     */
    public float getCoveragePercent() {
        return coveragePercent;
    }
    /**
     * @param coveragePercent the coveragePercent to set
     */
    public void setCoveragePercent(float coveragePercent) {
        this.coveragePercent = coveragePercent;
    }
    /**
     * @param coverageLines the coverageLines to set
     */
    public void setCoverageLines(int coverageLines) {
        this.coverageLines = coverageLines;
    }
    /**
     * @param totalLines the totalLines to set
     */
    public void setTotalLines(int totalLines) {
        this.totalLines = totalLines;
    }
    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }
}
