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

import com.salesforce.ant.TestTask;
import com.sforce.soap.apex.CodeCoverageResult;
import com.sforce.soap.apex.RunTestFailure;
import com.sforce.soap.apex.RunTestSuccess;
import com.sforce.soap.apex.RunTestsResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.LogLevel;

/**
 * XML coverage report producer.
 * @author ss
 */
public class XMLCoverageReportProducer {
    /** Tests result. */
    private final RunTestsResult result;
    /** Ant task. */
    private final TestTask task;
    /**
     * Constructor.
     * @param result result.
     * @param task ant task.
     */
    public XMLCoverageReportProducer(final RunTestsResult result,
            final TestTask task) {
        this.task = task;
        this.result = result;
    }
    /**
     * Create report.
     * @return report.
     * @throws BuildException error.
     */
    public XMLCoverageReport createReport() throws BuildException {
        try {
            JAXBContext jaxbContext = JAXBContext
                    .newInstance(XMLCoverageReport.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            XMLCoverageReport report = createReportProcess();
            File folder = new File(TestTask.REPORT_FOLDER_NAME);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            jaxbMarshaller.marshal(report,
                    new File(folder, "coverage-report.xml"));
            return report;
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }
    /**
     * Create report.
     * @return report.
     * @throws Exception error.
     */
    private XMLCoverageReport createReportProcess() throws Exception {
        XMLCoverageReport report = new XMLCoverageReport();
        Set<String> classes = task.getProjectClassesAndTriggers();
        // classes
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
        int totalCoverageLines = 0;
        for (CoverageElement el : elements) {
            totalLines += el.getTotalLines();
            totalCoverageLines += el.getCoverageLines();
        }
        float percent = (((float) totalCoverageLines)
                / ((float) totalLines)) * 100;
        report.setClasses(elements);
        report.setFail(result.getFailures().length);
        report.setSuccess(result.getSuccesses().length);
        report.setTotalCoverageLines(totalCoverageLines);
        report.setTotalLines(totalLines);
        report.setTotalPercent(percent);
        // methods
        RunTestFailure[] failTests = result.getFailures();
        Map<String, List<TestElement>> testMap = new HashMap();
        for (RunTestFailure fail : failTests) {
            TestElement el = new TestElement(fail);
            if (!testMap.containsKey(fail.getName())) {
                testMap.put(fail.getName(), new ArrayList<>());
            }
            testMap.get(fail.getName()).add(el);
        }
        RunTestSuccess[] successTests = result.getSuccesses();
        for (RunTestSuccess success : successTests) {
            TestElement el = new TestElement(success);
            if (!testMap.containsKey(success.getName())) {
                testMap.put(success.getName(), new ArrayList<>());
            }
            testMap.get(success.getName()).add(el);
        }
        List<TestElement> tElements = new ArrayList<>();
        testMap.values().forEach((tl) -> {
            tElements.addAll(tl);
        });
        report.setMethods(new ArrayList<>(tElements));
        return report;
    }
}
