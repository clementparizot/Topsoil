/*
 * Copyright 2015 CIRDLES.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cirdles.topsoil.app;

import org.cirdles.topsoil.chart.Chart;
import org.cirdles.topsoil.chart.SimpleVariableContext;
import org.cirdles.topsoil.chart.VariableContext;
import static org.cirdles.topsoil.dataset.Dataset.EMPTY_DATASET;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author John Zeringue
 */
public class ScatterplotChartTest {

    private Chart chart;

    @Before
    public void setUpChart() {
        chart = new ScatterplotChart();
    }

    @Test
    public void testGetSettingScope() {
        chart.getSettingScope();
    }

    @Test
    public void testGetVariables() {
        chart.getVariables();
    }

    @Test
    public void testSetData() {
        VariableContext variableContext
                = new SimpleVariableContext(EMPTY_DATASET);

        chart.setData(variableContext);
    }

}
