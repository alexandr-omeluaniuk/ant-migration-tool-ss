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
import com.sforce.soap.apex.RunTestFailure;
import com.sforce.soap.apex.RunTestSuccess;
import com.sforce.soap.apex.RunTestsResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.LogLevel;

/**
 * Test coverage report.
 * @author ss
 */
public class CoverageReport {
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
            Set<String> classes = getProjectClassesAndTriggers();
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
        RunTestFailure[] failTests = result.getFailures();
        Map<String, RunTestFailure> failMap = new HashMap<>();
        for (RunTestFailure fail : failTests) {
            failMap.put(fail.getName(), fail);
        }
        RunTestSuccess[] successTests = result.getSuccesses();
        Map<String, RunTestSuccess> successMap = new HashMap<>();
        for (RunTestSuccess success : successTests) {
            successMap.put(success.getName(), success);
        }
        CodeCoverageResult[] coverageResult = result.getCodeCoverage();
        List<CoverageElement> elements = new ArrayList<>();
        for (CodeCoverageResult ccr : coverageResult) {
            task.log("coverage name [" + ccr.getName() + "]",
                    LogLevel.DEBUG.getLevel());
            if (!classes.contains(ccr.getName())) {
                continue;
            }
            CoverageElement element = new CoverageElement(ccr);
            elements.add(element);
        }
        Collections.sort(elements, (CoverageElement o1, CoverageElement o2) -> {
            return o1.getClassName().compareTo(o2.getClassName());
        });
        int totalLines = 0;
        int totalCoveregeLines = 0;
        StringBuilder table = new StringBuilder();
        table.append("<table>");
            table.append("<thead>");
                table.append("<th>").append("Class name").append("</th>");
                table.append("<th>").append("Coverage lines").append("</th>");
                table.append("<th>").append("Coverage percent").append("</th>");
            table.append("</thead>");
            table.append("</tbody>");
            for (CoverageElement el : elements) {
                table.append(el.toHTMLRow());
                totalLines += el.getTotalLines();
                totalCoveregeLines += el.getCoverageLines();
            }
            table.append("</tbody>");
        table.append("</table>");
        // total table
        float percent = (((float) totalCoveregeLines)
                    / ((float) totalLines)) * 100;
        StringBuilder totalTable = new StringBuilder();
        totalTable.append("<table class=\"total-table\">");
            totalTable.append("</tbody>");
            totalTable.append("<tr>");
                totalTable.append("<td>").append("<b>Total</b> (fail: ")
                        .append(failTests.length)
                        .append(", success: ").append(successTests.length)
                        .append(")").append("</td>");
                totalTable.append("<td class=\"total-col\">")
                        .append(totalCoveregeLines).append("/")
                        .append(totalLines).append("</td>");
                totalTable.append("<td class=\"total-col\">")
                        .append(String.format("%.1f", percent))
                        .append("</td>");
            totalTable.append("</tr>");
            totalTable.append("</tbody>");
        totalTable.append("</table>");
        sb.append(totalTable);
        sb.append(table);
    }
    private Set<String> getProjectClassesAndTriggers() {
        Set<String> classes = new HashSet<>();
        File classesDir = new File(srcDir, "classes");
        if (classesDir.exists()) {
            for (File f : classesDir.listFiles()) {
                if (f.getName().endsWith(".cls")) {
                    task.log("add class [" + f.getName() + "]",
                            LogLevel.DEBUG.getLevel());
                    classes.add(f.getName().replace(".cls", ""));
                }
            }
        }
        File triggersDir = new File(srcDir, "triggers");
        if (triggersDir.exists()) {
            for (File f : triggersDir.listFiles()) {
                if (f.getName().endsWith(".trigger")) {
                    task.log("add trigger [" + f.getName() + "]",
                            LogLevel.DEBUG.getLevel());
                    classes.add(f.getName().replace(".trigger", ""));
                }
            }
        }
        task.log("classes found [" + classes.size() + "]");
        return classes;
    }
}
