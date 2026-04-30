package com.nexacro.uiadapter.controller;

import com.nexacro.uiadapter.domain.Board;
import com.nexacro.uiadapter.jakarta.core.annotation.ParamDataSet;
import com.nexacro.uiadapter.jakarta.core.data.NexacroResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Excel export sample.
 *
 * <p><b>API limitation:</b> The {@code uiadapter-jakarta-excel} module
 * only exposes {@link com.nexacro.uiadapter.jakarta.excel.extend.NexacroExcelTransformer}
 * with a single method {@code transform(String path, HttpServletResponse response)} —
 * it consumes a Nexacro xlsx <b>template file path</b> on disk, not a
 * {@code List<Board>} or {@link com.nexacro.java.xapi.data.DataSet}. There is no
 * "list-to-bytes" path in the public API. Building xlsx from a list would
 * require an extra dependency (Apache POI etc.) which is out of scope for
 * this self-contained runner.
 *
 * <p>This endpoint therefore accepts the inbound {@code ds_board} so the
 * client integration can be wired, then returns a structured error code so
 * the limitation is observable. Replace this stub once a list-to-xlsx path
 * is chosen (POI direct, or a Nexacro xlsx template under
 * {@code resources/} fed to {@code NexacroExcelTransformer}).
 */
@Controller
public class ExcelExportController {

    @RequestMapping("/exportBoardExcel.do")
    public NexacroResult exportBoardExcel(
            @ParamDataSet(name = "ds_board", required = false) List<Board> rows) {
        NexacroResult result = new NexacroResult();
        result.setErrorCode(-1);
        result.setErrorMsg("excel export not implemented — uiadapter-jakarta-excel"
                + " only exposes template-driven NexacroExcelTransformer; list-to-xlsx"
                + " requires an additional library (POI etc.). Inbound rows="
                + (rows == null ? 0 : rows.size()));
        return result;
    }
}
