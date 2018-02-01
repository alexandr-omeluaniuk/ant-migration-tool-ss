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
import com.sforce.soap.apex.RunTestsResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.LogLevel;

/**
 * Test coverage report.
 * @author ss
 */
public class CoverageReport {
    /** Coverage limit. */
    private static final float COVERAGE_LIMIT = 75;
    /** Tests result. */
    private final RunTestsResult result;
    /** Project src directory. */
    private final File srcDir;
    /** Ant task. */
    private final Task task;
    /**
     * Constructor.
     * @param result result.
     * @param srcDir project source directory.
     * @param task ant task.
     */
    public CoverageReport(final RunTestsResult result, final File srcDir,
            final Task task) {
        this.result = result;
        this.srcDir = srcDir;
        this.task = task;
    }
    public void createReport() throws BuildException {
        try {
            StringBuilder sb = new StringBuilder();
            Set<String> classes = getProjectClasses();
            appendStyle(sb);
            appendTable(sb, classes);
            try (FileOutputStream fos = new FileOutputStream(
                    new File("coverage-report.html"))) {
                fos.write(sb.toString().getBytes("UTF-8"));
            }
        } catch (Exception e) {
            throw new BuildException("create coverage report fail!", e);
        }
    }
    private void appendStyle(final StringBuilder sb) throws Exception {
        try (InputStream is = getClass()
                .getResourceAsStream("coverage-report.css")) {
            byte[] buffer = new byte[is.available()];
            is.read(buffer, 0, is.available());
            String css = new String(buffer, 0, buffer.length, "UTF-8");
            sb.append("<style>");
            sb.append(css);
            sb.append("</style>");
        }
    }
    private void appendTable(final StringBuilder sb,
            final Set<String> classes) {
        sb.append("<table>");
            sb.append("<thead>");
                sb.append("<th>").append("Class name").append("</th>");
                sb.append("<th>").append("Coverage lines").append("</th>");
                sb.append("<th>").append("Coverage percent").append("</th>");
            sb.append("</thead>");
            sb.append("</tbody>");
                CodeCoverageResult[] coverageResult = result.getCodeCoverage();
                for (CodeCoverageResult ccr : coverageResult) {
                    task.log("coverage name [" + ccr.getName() + "]",
                            LogLevel.DEBUG.getLevel());
                    if (!classes.contains(ccr.getName())) {
                        continue;
                    }
                    int coveredLines = ccr.getNumLocations()
                            - ccr.getNumLocationsNotCovered();
                    float percent = (((float) coveredLines)
                            / ((float) ccr.getNumLocations())) * 100;
                    String percentClass = percent > COVERAGE_LIMIT
                            ? "coverage-high" : "coverage-low";
                    sb.append("<tr>");
                    sb.append("<td>").append(ccr.getName()).append("</td>");
                    sb.append("<td class=\"lines-col ").append(percentClass)
                            .append("\">")
                            .append(coveredLines)
                            .append(" / ").append(ccr.getNumLocations())
                            .append("</td>");
                    sb.append("<td class=\"percent-col ").append(percentClass)
                            .append("\">")
                            .append(String.format("%.1f", percent))
                            .append("</td>");
                    sb.append("</tr>");
                }
            sb.append("</tbody>");
        sb.append("</table>");
    }
    private Set<String> getProjectClasses() {
        Set<String> classes = new HashSet<>();
        File classesDir = new File(srcDir, "classes");
        if (!classesDir.exists()) {
            throw new BuildException("classes directory not found! Actual path"
                    + " [" + classesDir.getAbsolutePath() + "]");
        }
        for (File f : classesDir.listFiles()) {
            if (f.getName().endsWith(".cls")) {
                task.log("add class [" + f.getName() + "]",
                        LogLevel.DEBUG.getLevel());
                classes.add(f.getName().replace(".cls", ""));
            }
        }
        task.log("classes found [" + classes.size() + "]");
        return classes;
    }
}
