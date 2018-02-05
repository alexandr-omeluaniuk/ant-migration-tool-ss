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

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * XML version of the coverage report.
 * @author ss
 */
@XmlRootElement
public class XMLCoverageReport {
    /** Coverage information for classes. */
    private List<CoverageElement> classes;
    /** Coverage information for methods. */
    private List<TestElement> methods;
    /** Total coverage percent. */
    private float totalPercent;
    /** Total coverage lines. */
    private int totalCoverageLines;
    /** Total lines. */
    private int totalLines;
    /** Number of successful tests. */
    private int success;
    /** Number of failed tests. */
    private int fail;
    /**
     * @return the classes
     */
    public List<CoverageElement> getClasses() {
        return classes;
    }
    /**
     * @param classes the classes to set
     */
    public void setClasses(List<CoverageElement> classes) {
        this.classes = classes;
    }
    /**
     * @return the methods
     */
    public List<TestElement> getMethods() {
        return methods;
    }
    /**
     * @param methods the methods to set
     */
    public void setMethods(List<TestElement> methods) {
        this.methods = methods;
    }
    /**
     * @return the totalPercent
     */
    public float getTotalPercent() {
        return totalPercent;
    }
    /**
     * @param totalPercent the totalPercent to set
     */
    public void setTotalPercent(float totalPercent) {
        this.totalPercent = totalPercent;
    }
    /**
     * @return the success
     */
    public int getSuccess() {
        return success;
    }
    /**
     * @param success the success to set
     */
    public void setSuccess(int success) {
        this.success = success;
    }
    /**
     * @return the fail
     */
    public int getFail() {
        return fail;
    }
    /**
     * @param fail the fail to set
     */
    public void setFail(int fail) {
        this.fail = fail;
    }
    /**
     * @return the totalCoverageLines
     */
    public int getTotalCoverageLines() {
        return totalCoverageLines;
    }
    /**
     * @param totalCoverageLines the totalCoverageLines to set
     */
    public void setTotalCoverageLines(int totalCoverageLines) {
        this.totalCoverageLines = totalCoverageLines;
    }
    /**
     * @return the totalLines
     */
    public int getTotalLines() {
        return totalLines;
    }
    /**
     * @param totalLines the totalLines to set
     */
    public void setTotalLines(int totalLines) {
        this.totalLines = totalLines;
    }
}
